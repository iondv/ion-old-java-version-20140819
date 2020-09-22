package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;

public class OfflineLockService implements LockService {

    private Database database;
    private boolean hasChangeLogLock = false;

    
    public int getPriority() {
        return 5000;
    }

    
    public boolean supports(Database database) {
        return database.getConnection() != null && database.getConnection() instanceof OfflineConnection;
    }

    
    public void init() throws DatabaseException {

    }

    
    public void setDatabase(Database database) {
        this.database = database;
    }

    
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {

    }

    
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {

    }

    
    public boolean hasChangeLogLock() {
        return this.hasChangeLogLock;
    }

    
    public void waitForLock() throws LockException {

    }

    
    public boolean acquireLock() throws LockException {
        this.hasChangeLogLock = true;
        return true;
    }

    
    public void releaseLock() throws LockException {
        this.hasChangeLogLock = false;
    }

    
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        return new DatabaseChangeLogLock[0];
    }

    
    public void forceReleaseLock() throws LockException, DatabaseException {
        this.hasChangeLogLock = false;
    }

    
    public void reset() {
        this.hasChangeLogLock = false;
    }

    
    public void destroy() throws DatabaseException {
        //nothign to do
    }
}
