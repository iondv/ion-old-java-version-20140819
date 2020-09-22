package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.core.Table;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StandardLockService implements LockService {

    private Database database;

    private boolean hasChangeLogLock = false;

    private long changeLogLockWaitTime = 1000 * 60 * 5;  //default to 5 mins
    private long changeLogLocRecheckTime = 1000 * 10;  //default to every 10 seconds

    public static final String LOCK_WAIT_TIME_SYSTEM_PROPERTY = "liquibase.changeLogLockWaitTimeInMinutes";

    private boolean hasDatabaseChangeLogLockTable = false;
    private boolean isDatabaseChangeLogLockTableInitialized = false;

    {
        try {
            changeLogLockWaitTime = 1000 * 60 * Long.parseLong(System.getProperty(LOCK_WAIT_TIME_SYSTEM_PROPERTY));
            LogFactory.getLogger().info("lockWaitTime change to: " + changeLogLockWaitTime);
        } catch (NumberFormatException e) {
            // was non or not valid configuration, we will keep the standard value
        }
    }

    public StandardLockService() {
    }

    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    
    public boolean supports(Database database) {
        return true;
    }

    
    public void setDatabase(Database database) {
        this.database = database;
    }

    
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }

    
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
        this.changeLogLocRecheckTime = changeLogLocRecheckTime;
    }

    
    public void init() throws DatabaseException {

        boolean createdTable = false;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (!hasDatabaseChangeLogLockTable && !hasDatabaseChangeLogLockTable()) {

            executor.comment("Create Database Lock Table");
            executor.execute(new CreateDatabaseChangeLogLockTableStatement());
            database.commit();
            LogFactory.getLogger().debug("Created database lock table with name: " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));
            this.hasDatabaseChangeLogLockTable = true;
            createdTable = true;
        }

        if (!isDatabaseChangeLogLockTableInitialized(createdTable)) {
            executor.comment("Initialize Database Lock Table");
            executor.execute(new InitializeDatabaseChangeLogLockTableStatement());
            database.commit();
        }

        if (database instanceof DerbyDatabase && ((DerbyDatabase) database).supportsBooleanDataType()) { //check if the changelog table is of an old smallint vs. boolean format
            String lockTable = database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
            Object obj = executor.queryForObject(new RawSqlStatement("select min(locked) as test from " + lockTable + " fetch first row only"), Object.class);
            if (!(obj instanceof Boolean)) { //wrong type, need to recreate table
                executor.execute(new DropTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName(), false));
                executor.execute(new CreateDatabaseChangeLogLockTableStatement());
                executor.execute(new InitializeDatabaseChangeLogLockTableStatement());
            }
        }

    }


    public boolean isDatabaseChangeLogLockTableInitialized(final boolean tableJustCreated) throws DatabaseException {
        boolean initialized;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            initialized = executor.queryForInt(new RawSqlStatement("select count(*) from " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()))) > 0;
        } catch (LiquibaseException e) {
            if (executor.updatesDatabase()) {
                throw new UnexpectedLiquibaseException(e);
            } else {
                //probably didn't actually create the table yet.

                initialized = !tableJustCreated;
            }
        }
        return initialized;
    }

    
    public boolean hasChangeLogLock() {
        return hasChangeLogLock;
    }

    public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
        boolean hasTable = false;
        try {
            hasTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogLockTable(database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return hasTable;
    }


    
    public void waitForLock() throws LockException {

        boolean locked = false;
        long timeToGiveUp = new Date().getTime() + changeLogLockWaitTime;
        while (!locked && new Date().getTime() < timeToGiveUp) {
            locked = acquireLock();
            if (!locked) {
                LogFactory.getLogger().info("Waiting for changelog lock....");
                try {
                    Thread.sleep(changeLogLocRecheckTime);
                } catch (InterruptedException e) {
                    ;
                }
            }
        }

        if (!locked) {
            DatabaseChangeLogLock[] locks = listLocks();
            String lockedBy;
            if (locks.length > 0) {
                DatabaseChangeLogLock lock = locks[0];
                lockedBy = lock.getLockedBy() + " since " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lock.getLockGranted());
            } else {
                lockedBy = "UNKNOWN";
            }
            throw new LockException("Could not acquire change log lock.  Currently locked by " + lockedBy);
        }
    }

    
    public boolean acquireLock() throws LockException {
        if (hasChangeLogLock) {
            return true;
        }

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        try {
            database.rollback();
            this.init();

            Boolean locked = (Boolean) ExecutorService.getInstance().getExecutor(database).queryForObject(new SelectFromDatabaseChangeLogLockStatement("LOCKED"), Boolean.class);

            if (locked) {
                return false;
            } else {

                executor.comment("Lock Database");
                int rowsUpdated = executor.update(new LockDatabaseChangeLogStatement());
                if (rowsUpdated > 1) {
                    throw new LockException("Did not update change log lock correctly");
                }
                if (rowsUpdated == 0)
                {
                    // another node was faster
                    return false;
                }
                database.commit();
                LogFactory.getLogger().info("Successfully acquired change log lock");

                hasChangeLogLock = true;

                database.setCanCacheLiquibaseTableInfo(true);
                return true;
            }
        } catch (Exception e) {
            throw new LockException(e);
        } finally {
            try {
                database.rollback();
            } catch (DatabaseException e) {
                ;
            }
        }

    }

    
    public void releaseLock() throws LockException {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            if (this.hasDatabaseChangeLogLockTable()) {
                executor.comment("Release Database Lock");
                database.rollback();
                int updatedRows = executor.update(new UnlockDatabaseChangeLogStatement());
                if (updatedRows != 1) {
                    throw new LockException("Did not update change log lock correctly.\n\n" + updatedRows + " rows were updated instead of the expected 1 row using executor " + executor.getClass().getName()+" there are "+executor.queryForInt(new RawSqlStatement("select count(*) from "+database.getDatabaseChangeLogLockTableName()))+" rows in the table");
                }
                database.commit();
            }
        } catch (Exception e) {
            throw new LockException(e);
        } finally {
            try {
                hasChangeLogLock = false;

                database.setCanCacheLiquibaseTableInfo(false);

                LogFactory.getLogger().info("Successfully released change log lock");
                database.rollback();
            } catch (DatabaseException e) {
                ;
            }
        }
    }

    
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        try {
            if (!this.hasDatabaseChangeLogLockTable()) {
                return new DatabaseChangeLogLock[0];
            }

            List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
            SqlStatement sqlStatement = new SelectFromDatabaseChangeLogLockStatement("ID", "LOCKED", "LOCKGRANTED", "LOCKEDBY");
            List<Map<String, ?>> rows = ExecutorService.getInstance().getExecutor(database).queryForList(sqlStatement);
            for (Map columnMap : rows) {
                Object lockedValue = columnMap.get("LOCKED");
                Boolean locked;
                if (lockedValue instanceof Number) {
                    locked = ((Number) lockedValue).intValue() == 1;
                } else {
                    locked = (Boolean) lockedValue;
                }
                if (locked != null && locked) {
                    allLocks.add(new DatabaseChangeLogLock(((Number) columnMap.get("ID")).intValue(), (Date) columnMap.get("LOCKGRANTED"), (String) columnMap.get("LOCKEDBY")));
                }
            }
            return allLocks.toArray(new DatabaseChangeLogLock[allLocks.size()]);
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    
    public void forceReleaseLock() throws LockException, DatabaseException {
        this.init();
        releaseLock();
        /*try {
            releaseLock();
        } catch (LockException e) {
            // ignore ?
            LogFactory.getLogger().info("Ignored exception in forceReleaseLock: " + e.getMessage());
        }*/
    }

    
    public void reset() {
        hasChangeLogLock = false;
    }

    
    public void destroy() throws DatabaseException {
        try {
            if (SnapshotGeneratorFactory.getInstance().has(new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()), database)) {
                ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName(), false));
            }
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }
}
