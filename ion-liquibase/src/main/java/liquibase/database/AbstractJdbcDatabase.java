package liquibase.database;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.changelog.*;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.*;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 * AbstractJdbcDatabase is extended by all supported databases as a facade to the underlying database.
 * The physical connection can be retrieved from the AbstractJdbcDatabase implementation, as well as any
 * database-specific characteristics such as the datatype for "boolean" fields.
 */
public abstract class AbstractJdbcDatabase implements Database {

    private static final Pattern startsWithNumberPattern = Pattern.compile("^[0-9].*");

    private DatabaseConnection connection;
    protected String defaultCatalogName;
    protected String defaultSchemaName;

    protected String currentDateTimeFunction;

    /**
     * The sequence name will be substituted into the string e.g. NEXTVAL('%s')
     */
    protected String sequenceNextValueFunction;
    protected String sequenceCurrentValueFunction;
    protected String quotingStartCharacter = "\"";
    protected String quotingEndCharacter = "\"";

    // List of Database native functions.
    protected List<DatabaseFunction> dateFunctions = new ArrayList<DatabaseFunction>();

    protected List<String> unmodifiableDataTypes = new ArrayList<String>();

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private String databaseChangeLogTableName = System.getProperty("liquibase.databaseChangeLogTableName") == null ? "DatabaseChangeLog".toUpperCase() : System.getProperty("liquibase.databaseChangeLogTableName");
    private String databaseChangeLogLockTableName = System.getProperty("liquibase.databaseChangeLogLockTableName") == null ? "DatabaseChangeLogLock".toUpperCase() : System.getProperty("liquibase.databaseChangeLogLockTableName");
    private String liquibaseTablespaceName = System.getProperty("liquibase.tablespaceName");
    private String liquibaseSchemaName = System.getProperty("liquibase.schemaName");
    private String liquibaseCatalogName = System.getProperty("liquibase.catalogName");

    private Boolean previousAutoCommit;

    private boolean canCacheLiquibaseTableInfo = false;

    protected BigInteger defaultAutoIncrementStartWith = BigInteger.ONE;
    protected BigInteger defaultAutoIncrementBy = BigInteger.ONE;
    // most databases either lowercase or uppercase unuqoted objects such as table and column names.
    protected Boolean unquotedObjectsAreUppercased = null;
    // whether object names should be quoted
    protected ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.LEGACY;

    private final Set<String> reservedWords = new HashSet<String>();

    protected Boolean caseSensitive;
    private boolean outputDefaultSchema = true;
    private boolean outputDefaultCatalog = true;

    public String getName() {
        return toString();
    }

    
    public boolean requiresPassword() {
        return true;
    }

    
    public boolean requiresUsername() {
        return true;
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    // ------- DATABASE INFORMATION METHODS ---- //

    
    public DatabaseConnection getConnection() {
        return connection;
    }

    
    public void setConnection(final DatabaseConnection conn) {
        LogFactory.getLogger().debug("Connected to " + conn.getConnectionUserName() + "@" + conn.getURL());
        this.connection = conn;
        try {
            boolean autoCommit = conn.getAutoCommit();
            if (autoCommit == getAutoCommitMode()) {
                // Don't adjust the auto-commit mode if it's already what the database wants it to be.
                LogFactory.getLogger().debug("Not adjusting the auto commit mode; it is already " + autoCommit);
            } else {
                // Store the previous auto-commit mode, because the connection needs to be restored to it when this
                // AbstractDatabase type is closed. This is important for systems which use connection pools.
                previousAutoCommit = autoCommit;

                LogFactory.getLogger().debug("Setting auto commit to " + getAutoCommitMode() + " from " + autoCommit);
                connection.setAutoCommit(getAutoCommitMode());

            }
        } catch (DatabaseException e) {
            LogFactory.getLogger().warning("Cannot set auto commit to " + getAutoCommitMode() + " on connection");
        }

        this.connection.attached(this);
    }

    /**
     * Auto-commit mode to run in
     */
    
    public boolean getAutoCommitMode() {
        return !supportsDDLInTransaction();
    }

    
    public void addReservedWords(Collection<String> words) {
        reservedWords.addAll(words);
    }

    /**
     * By default databases should support DDL within a transaction.
     */
    
    public boolean supportsDDLInTransaction() {
        return true;
    }

    /**
     * Returns the name of the database product according to the underlying database.
     */
    
    public String getDatabaseProductName() {
        if (connection == null) {
            return getDefaultDatabaseProductName();
        }

        try {
            return connection.getDatabaseProductName();
        } catch (DatabaseException e) {
            throw new RuntimeException("Cannot get database name");
        }
    }

    protected abstract String getDefaultDatabaseProductName();


    
    public String getDatabaseProductVersion() throws DatabaseException {
        if (connection == null) {
            return null;
        }

        try {
            return connection.getDatabaseProductVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    
    public int getDatabaseMajorVersion() throws DatabaseException {
        if (connection == null) {
            return -1;
        }
        try {
            return connection.getDatabaseMajorVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    
    public int getDatabaseMinorVersion() throws DatabaseException {
        if (connection == null) {
            return -1;
        }
        try {
            return connection.getDatabaseMinorVersion();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    
    public String getDefaultCatalogName() {
        if (defaultCatalogName == null) {
            if (defaultSchemaName != null && !this.supportsSchemas()) {
                return defaultSchemaName;
            }

            if (connection != null) {
                try {
                    defaultCatalogName = getConnectionCatalogName();
                } catch (DatabaseException e) {
                    LogFactory.getLogger().info("Error getting default catalog", e);
                }
            }
        }
        return defaultCatalogName;
    }

    protected String getConnectionCatalogName() throws DatabaseException {
        return connection.getCatalog();
    }

    public CatalogAndSchema correctSchema(final String catalog, final String schema) {
        return correctSchema(new CatalogAndSchema(catalog, schema));
    }

    
    public CatalogAndSchema correctSchema(final CatalogAndSchema schema) {
        if (schema == null) {
            return new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName());
        }
        String catalogName = StringUtils.trimToNull(schema.getCatalogName());
        String schemaName = StringUtils.trimToNull(schema.getSchemaName());

        if (supportsCatalogs() && supportsSchemas()) {
            if (catalogName == null) {
                catalogName = getDefaultCatalogName();
            } else {
                catalogName = correctObjectName(catalogName, Catalog.class);
            }

            if (schemaName == null) {
                schemaName = getDefaultSchemaName();
            } else {
                schemaName = correctObjectName(schemaName, Schema.class);
            }
        } else if (!supportsCatalogs() && !supportsSchemas()) {
            return new CatalogAndSchema(null, null);
        } else if (supportsCatalogs()) { //schema is null
            if (catalogName == null) {
                if (schemaName == null) {
                    catalogName = getDefaultCatalogName();
                } else {
                    catalogName = schemaName;
                }
            }
            schemaName = catalogName;
        } else if (supportsSchemas()) {
            if (schemaName == null) {
                if (catalogName == null) {
                    schemaName = getDefaultSchemaName();
                } else {
                    schemaName = catalogName;
                }
            }
            catalogName = schemaName;
        }
        return new CatalogAndSchema(catalogName, schemaName);

    }

    
    public String correctObjectName(final String objectName, final Class<? extends DatabaseObject> objectType) {
        if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS || unquotedObjectsAreUppercased == null
                || objectName == null || (objectName.startsWith(quotingStartCharacter) && objectName.endsWith(
                quotingEndCharacter))) {
            return objectName;
        } else if (Boolean.TRUE.equals(unquotedObjectsAreUppercased)) {
            return objectName.toUpperCase();
        } else {
            return objectName.toLowerCase();
        }
    }

    
    public CatalogAndSchema getDefaultSchema() {
        return new CatalogAndSchema(getDefaultCatalogName(), getDefaultSchemaName());

    }

    
    public String getDefaultSchemaName() {

        if (!supportsSchemas()) {
            return getDefaultCatalogName();
        }

        if (defaultSchemaName == null && connection != null) {
            defaultSchemaName = getConnectionSchemaName();
        }


        return defaultSchemaName;
    }

    /**
     * Overwrite this method to get the default schema name for the connection.
     *
     * @return
     */
    protected String getConnectionSchemaName() {
        if (connection == null) {
            return null;
        }
        try {
            return ExecutorService.getInstance().getExecutor(this).queryForObject(new RawCallStatement("call current_schema"), String.class);

        } catch (Exception e) {
            LogFactory.getLogger().info("Error getting default schema", e);
        }
        return null;
    }

    
    public void setDefaultCatalogName(final String defaultCatalogName) {
        this.defaultCatalogName = correctObjectName(defaultCatalogName, Catalog.class);
    }

    
    public void setDefaultSchemaName(final String schemaName) {
        this.defaultSchemaName = correctObjectName(schemaName, Schema.class);
    }

    /**
     * Returns system (undroppable) views.
     */
    protected Set<String> getSystemTables() {
        return new HashSet<String>();
    }


    /**
     * Returns system (undroppable) views.
     */
    protected Set<String> getSystemViews() {
        return new HashSet<String>();
    }

    // ------- DATABASE FEATURE INFORMATION METHODS ---- //

    /**
     * Does the database type support sequence.
     */
    
    public boolean supportsSequences() {
        return true;
    }

    
    public boolean supportsAutoIncrement() {
        return true;
    }

    // ------- DATABASE-SPECIFIC SQL METHODS ---- //

    
    public void setCurrentDateTimeFunction(final String function) {
        if (function != null) {
            this.currentDateTimeFunction = function;
            this.dateFunctions.add(new DatabaseFunction(function));
        }
    }

    /**
     * Return a date literal with the same value as a string formatted using ISO 8601.
     * <p/>
     * Note: many databases accept date literals in ISO8601 format with the 'T' replaced with
     * a space. Only databases which do not accept these strings should need to override this
     * method.
     * <p/>
     * Implementation restriction:
     * Currently, only the following subsets of ISO8601 are supported:
     * yyyy-MM-dd
     * hh:mm:ss
     * yyyy-MM-ddThh:mm:ss
     */
    
    public String getDateLiteral(final String isoDate) {
        if (isDateOnly(isoDate) || isTimeOnly(isoDate)) {
            return "'" + isoDate + "'";
        } else if (isDateTime(isoDate)) {
//            StringBuffer val = new StringBuffer();
//            val.append("'");
//            val.append(isoDate.substring(0, 10));
//            val.append(" ");
////noinspection MagicNumber
//            val.append(isoDate.substring(11));
//            val.append("'");
//            return val.toString();
            return "'" + isoDate.replace('T', ' ') + "'";
        } else {
            return "BAD_DATE_FORMAT:" + isoDate;
        }
    }


    
    public String getDateTimeLiteral(final java.sql.Timestamp date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    
    public String getDateLiteral(final java.sql.Date date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    
    public String getTimeLiteral(final java.sql.Time date) {
        return getDateLiteral(new ISODateFormat().format(date).replaceFirst("^'", "").replaceFirst("'$", ""));
    }

    
    public String getDateLiteral(final Date date) {
        if (date instanceof java.sql.Date) {
            return getDateLiteral(((java.sql.Date) date));
        } else if (date instanceof java.sql.Time) {
            return getTimeLiteral(((java.sql.Time) date));
        } else if (date instanceof java.sql.Timestamp) {
            return getDateTimeLiteral(((java.sql.Timestamp) date));
        } else {
            throw new RuntimeException("Unexpected type: " + date.getClass().getName());
        }
    }

    
    public Date parseDate(final String dateAsString) throws DateParseException {
        try {
            if (dateAsString.indexOf(" ") > 0) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateAsString);
            } else if (dateAsString.indexOf("T") > 0) {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateAsString);
            } else {
                if (dateAsString.indexOf(":") > 0) {
                    return new SimpleDateFormat("HH:mm:ss").parse(dateAsString);
                } else {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(dateAsString);
                }
            }
        } catch (ParseException e) {
            throw new DateParseException(dateAsString);
        }
    }

    protected boolean isDateOnly(final String isoDate) {
        return isoDate.length() == "yyyy-MM-dd".length();
    }

    protected boolean isDateTime(final String isoDate) {
        return isoDate.length() >= "yyyy-MM-ddThh:mm:ss".length();
    }

    protected boolean isTimeOnly(final String isoDate) {
        return isoDate.length() == "hh:mm:ss".length();
    }


    /**
     * Returns database-specific line comment string.
     */
    
    public String getLineComment() {
        return "--";
    }

    /**
     * Returns database-specific auto-increment DDL clause.
     */
    
    public String getAutoIncrementClause(final BigInteger startWith, final BigInteger incrementBy) {
        if (!supportsAutoIncrement()) {
            return "";
        }

        // generate an SQL:2003 standard compliant auto increment clause by default

        String autoIncrementClause = getAutoIncrementClause();

        boolean generateStartWith = generateAutoIncrementStartWith(startWith);
        boolean generateIncrementBy = generateAutoIncrementBy(incrementBy);

        if (generateStartWith || generateIncrementBy) {
            autoIncrementClause += getAutoIncrementOpening();

            if (generateStartWith) {
                autoIncrementClause += String.format(getAutoIncrementStartWithClause(), (startWith == null) ? defaultAutoIncrementStartWith : startWith);
            }

            if (generateIncrementBy) {
                if (generateStartWith) {
                    autoIncrementClause += ", ";
                }

                autoIncrementClause += String.format(getAutoIncrementByClause(), (incrementBy == null) ? defaultAutoIncrementBy : incrementBy);
            }

            autoIncrementClause += getAutoIncrementClosing();
        }

        return autoIncrementClause;
    }

    protected String getAutoIncrementClause() {
        return "GENERATED BY DEFAULT AS IDENTITY";
    }

    protected boolean generateAutoIncrementStartWith(final BigInteger startWith) {
        return startWith != null
                && !startWith.equals(defaultAutoIncrementStartWith);
    }

    protected boolean generateAutoIncrementBy(final BigInteger incrementBy) {
        return incrementBy != null
                && !incrementBy.equals(defaultAutoIncrementBy);
    }

    protected String getAutoIncrementOpening() {
        return " (";
    }

    protected String getAutoIncrementClosing() {
        return ")";
    }

    protected String getAutoIncrementStartWithClause() {
        return "START WITH %d";
    }

    protected String getAutoIncrementByClause() {
        return "INCREMENT BY %d";
    }

    
    public String getConcatSql(final String... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" || ");
        }

        return returnString.toString().replaceFirst(" \\|\\| $", "");
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogTableName()
     */
    
    public String getDatabaseChangeLogTableName() {
        return databaseChangeLogTableName;
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogLockTableName()
     */
    
    public String getDatabaseChangeLogLockTableName() {
        return databaseChangeLogLockTableName;
    }

    /**
     * @see liquibase.database.Database#getLiquibaseTablespaceName()
     */
    
    public String getLiquibaseTablespaceName() {
        return liquibaseTablespaceName;
    }

    /**
     * @see liquibase.database.Database#setDatabaseChangeLogTableName(java.lang.String)
     */
    
    public void setDatabaseChangeLogTableName(final String tableName) {
        this.databaseChangeLogTableName = tableName;
    }

    /**
     * @see liquibase.database.Database#setDatabaseChangeLogLockTableName(java.lang.String)
     */
    
    public void setDatabaseChangeLogLockTableName(final String tableName) {
        this.databaseChangeLogLockTableName = tableName;
    }

    /**
     * @see liquibase.database.Database#setLiquibaseTablespaceName(java.lang.String)
     */
    
    public void setLiquibaseTablespaceName(final String tablespace) {
        this.liquibaseTablespaceName = tablespace;
    }

    protected boolean canCreateChangeLogTable() throws DatabaseException {
        return ((StandardChangeLogHistoryService) ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this)).canCreateChangeLogTable();
    }

    
    public void setCanCacheLiquibaseTableInfo(final boolean canCacheLiquibaseTableInfo) {
        this.canCacheLiquibaseTableInfo = canCacheLiquibaseTableInfo;
    }

    
    public String getLiquibaseCatalogName() {
        return liquibaseCatalogName == null ? getDefaultCatalogName() : liquibaseCatalogName;
    }

    
    public void setLiquibaseCatalogName(final String catalogName) {
        this.liquibaseCatalogName = catalogName;
    }

    
    public String getLiquibaseSchemaName() {
        return liquibaseSchemaName == null ? getDefaultSchemaName() : liquibaseSchemaName;
    }

    
    public void setLiquibaseSchemaName(final String schemaName) {
        this.liquibaseSchemaName = schemaName;
    }

    
    public boolean isCaseSensitive() {
    	if (caseSensitive == null) {
            if (connection != null && connection instanceof JdbcConnection) {
                try {
                	caseSensitive = ((JdbcConnection) connection).getUnderlyingConnection().getMetaData().supportsMixedCaseIdentifiers();
                } catch (SQLException e) {
                    LogFactory.getLogger().warning("Cannot determine case sensitivity from JDBC driver", e);
                }
            }
        }

    	if (caseSensitive == null) {
            return false;
    	} else {
    		return caseSensitive.booleanValue();
    	}
    }

    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    
    public boolean isReservedWord(final String string) {
        return reservedWords.contains(string.toUpperCase());
    }

    /*
    * Check if given string starts with numeric values that may cause problems and should be escaped.
    */
    protected boolean startsWithNumeric(final String objectName) {
        return startsWithNumberPattern.matcher(objectName).matches();
    }

// ------- DATABASE OBJECT DROPPING METHODS ---- //

    /**
     * Drops all objects owned by the connected user.
     */
    
    public void dropDatabaseObjects(final CatalogAndSchema schemaToDrop) throws LiquibaseException {
        ObjectQuotingStrategy currentStrategy = this.getObjectQuotingStrategy();
        this.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        try {
            DatabaseSnapshot snapshot;
            try {
	            final SnapshotControl snapshotControl = new SnapshotControl(this);
	            final Set<Class<? extends DatabaseObject>> typesToInclude = snapshotControl.getTypesToInclude();

	            //We do not need to remove indexes and primary/unique keys explicitly. They should be removed
	            //as part of tables.
	            typesToInclude.remove(Index.class);
	            typesToInclude.remove(PrimaryKey.class);
	            typesToInclude.remove(UniqueConstraint.class);

	            if (supportsForeignKeyDisable()) {
		            //We do not remove ForeignKey because they will be disabled and removed as parts of tables.
		            typesToInclude.remove(ForeignKey.class);
	            }

	            final long createSnapshotStarted = System.currentTimeMillis();
	            snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemaToDrop, this, snapshotControl);
	            LogFactory.getLogger().debug(String.format("Database snapshot generated in %d ms. Snapshot includes: %s", System.currentTimeMillis() - createSnapshotStarted, typesToInclude));
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }

	        final long changeSetStarted = System.currentTimeMillis();
	        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(new EmptyDatabaseSnapshot(this), snapshot, new CompareControl(snapshot.getSnapshotControl().getTypesToInclude()));
            List<ChangeSet> changeSets = new DiffToChangeLog(diffResult, new DiffOutputControl(true, true, false)).generateChangeSets();
	        LogFactory.getLogger().debug(String.format("ChangeSet to Remove Database Objects generated in %d ms.", System.currentTimeMillis() - changeSetStarted));

            final boolean reEnableFK = supportsForeignKeyDisable() && disableForeignKeyChecks();
            try {
                for (ChangeSet changeSet : changeSets) {
                    for (Change change : changeSet.getChanges()) {
                        SqlStatement[] sqlStatements = change.generateStatements(this);
                        for (SqlStatement statement : sqlStatements) {
                            ExecutorService.getInstance().getExecutor(this).execute(statement);
                        }

                    }
                }
            } finally {
                if (reEnableFK) {
                    enableForeignKeyChecks();
                }
            }

            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).destroy();
            LockServiceFactory.getInstance().getLockService(this).destroy();

        } finally {
            this.setObjectQuotingStrategy(currentStrategy);
            this.commit();
        }
    }

    
    public boolean supportsDropTableCascadeConstraints() {
        return (this instanceof SQLiteDatabase
                || this instanceof SybaseDatabase
                || this instanceof SybaseASADatabase
                || this instanceof PostgresDatabase
                || this instanceof OracleDatabase
        );
    }

    
    public boolean isSystemObject(final DatabaseObject example) {
        if (example == null) {
            return false;
        }
        if (example.getSchema() != null && example.getSchema().getName() != null && example.getSchema().getName().equalsIgnoreCase("information_schema")) {
            return true;
        }
        if (example instanceof Table && getSystemTables().contains(example.getName())) {
            return true;
        }

        if (example instanceof View && getSystemViews().contains(example.getName())) {
            return true;
        }

        return false;
    }

    public boolean isSystemView(CatalogAndSchema schema, final String viewName) {
        schema = correctSchema(schema);
        if ("information_schema".equalsIgnoreCase(schema.getSchemaName())) {
            return true;
        } else if (getSystemViews().contains(viewName)) {
            return true;
        }
        return false;
    }

    
    public boolean isLiquibaseObject(final DatabaseObject object) {
        if (object instanceof Table) {
            Schema liquibaseSchema = new Schema(getLiquibaseCatalogName(), getLiquibaseSchemaName());
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, new Table().setName(getDatabaseChangeLogTableName()).setSchema(liquibaseSchema), this)) {
                return true;
            }
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, new Table().setName(getDatabaseChangeLogLockTableName()).setSchema(liquibaseSchema), this)) {
                return true;
            }
            return false;
        } else if (object instanceof Column) {
            return isLiquibaseObject(((Column) object).getRelation());
        } else if (object instanceof Index) {
            return isLiquibaseObject(((Index) object).getTable());
        } else if (object instanceof PrimaryKey) {
            return isLiquibaseObject(((PrimaryKey) object).getTable());
        }
        return false;
    }

    /**
     * Tags the database changelog with the given string.
     */
    
    public void tag(final String tagString) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).tag(tagString);
    }

    
    public boolean doesTagExist(final String tag) throws DatabaseException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).tagExists(tag);
    }

    
    public String toString() {
        if (getConnection() == null) {
            return getShortName() + " Database";
        }

        return getConnection().getConnectionUserName() + " @ " + getConnection().getURL() + (getDefaultSchemaName() == null ? "" : " (Default Schema: " + getDefaultSchemaName() + ")");
    }


    
    public String getViewDefinition(CatalogAndSchema schema, final String viewName) throws DatabaseException {
        schema = correctSchema(schema);
        String definition = (String) ExecutorService.getInstance().getExecutor(this).queryForObject(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);
        if (definition == null) {
            return null;
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    
    public String escapeTableName(final String catalogName, final String schemaName, final String tableName) {
        return escapeObjectName(catalogName, schemaName, tableName, Table.class);
    }

    
    public String escapeObjectName(String catalogName, String schemaName, final String objectName, final Class<? extends DatabaseObject> objectType) {
//        CatalogAndSchema catalogAndSchema = this.correctSchema(catalogName, schemaName);
//        catalogName = catalogAndSchema.getCatalogName();
//        schemaName = catalogAndSchema.getSchemaName();

        if (supportsSchemas()) {
            catalogName = StringUtils.trimToNull(catalogName);
            schemaName = StringUtils.trimToNull(schemaName);

            if (catalogName == null) {
                catalogName = this.getDefaultCatalogName();
            }
            if (schemaName == null) {
                schemaName = this.getDefaultSchemaName();
            }

            if (!supportsCatalogInObjectName(objectType)) {
                catalogName = null;
            }
            if (catalogName == null && schemaName == null) {
                return escapeObjectName(objectName, objectType);
            } else if (catalogName == null || !this.supportsCatalogInObjectName(objectType)) {
                if (isDefaultSchema(catalogName, schemaName) && !getOutputDefaultSchema()) {
                    return escapeObjectName(objectName, objectType);
                } else {
                    return escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
                }
            } else {
                if (isDefaultSchema(catalogName, schemaName) && !getOutputDefaultSchema() && !getOutputDefaultCatalog()) {
                    return escapeObjectName(objectName, objectType);
                } else if (isDefaultSchema(catalogName, schemaName) && !getOutputDefaultCatalog()) {
                    return escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
                } else {
                    return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(schemaName, Schema.class) + "." + escapeObjectName(objectName, objectType);
                }
            }
        } else if (supportsCatalogs()) {
            catalogName = StringUtils.trimToNull(catalogName);
            schemaName = StringUtils.trimToNull(schemaName);

            if (catalogName != null) {
                if (getOutputDefaultCatalog()) {
                    return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                } else {
                    if (isDefaultCatalog(catalogName)) {
                        return escapeObjectName(objectName, objectType);
                    } else {
                        return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                    }
                }
            } else {
                if (schemaName != null) { //they actually mean catalog name
                    if (getOutputDefaultCatalog()) {
                        return escapeObjectName(schemaName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                    } else {
                        if (isDefaultCatalog(schemaName)) {
                            return escapeObjectName(objectName, objectType);
                        } else {
                            return escapeObjectName(schemaName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                        }
                    }
                } else {
                    catalogName = this.getDefaultCatalogName();

                    if (catalogName == null) {
                        return escapeObjectName(objectName, objectType);
                    } else {
                        if (isDefaultCatalog(catalogName) && getOutputDefaultCatalog()) {
                            return escapeObjectName(catalogName, Catalog.class) + "." + escapeObjectName(objectName, objectType);
                        } else {
                            return escapeObjectName(objectName, objectType);
                        }
                    }
                }
            }

        } else {
            return escapeObjectName(objectName, objectType);
        }
    }

    
    public String escapeObjectName(String objectName, final Class<? extends DatabaseObject> objectType) {
        if (objectName != null) {
            objectName = objectName.trim();
            if (mustQuoteObjectName(objectName, objectType)) {
                return quoteObject(objectName, objectType);
            } else if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
                return quoteObject(objectName, objectType);
            }
            objectName = objectName.trim();
        }
        return objectName;
    }

    protected boolean mustQuoteObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName.contains("(")) {
            return false;
        }
        return objectName.contains("-") || startsWithNumeric(objectName) || isReservedWord(objectName);
    }

    public String quoteObject(final String objectName, final Class<? extends DatabaseObject> objectType) {
        return quotingStartCharacter + objectName + quotingEndCharacter;
    }

    
    public String escapeIndexName(final String catalogName, final String schemaName, final String indexName) {
        return escapeObjectName(catalogName, schemaName, indexName, Index.class);
    }

    
    public String escapeSequenceName(final String catalogName, final String schemaName, final String sequenceName) {
        return escapeObjectName(catalogName, schemaName, sequenceName, Sequence.class);
    }

    
    public String escapeConstraintName(final String constraintName) {
        return escapeObjectName(constraintName, Index.class);
    }

    
    public String escapeColumnName(final String catalogName, final String schemaName, final String tableName, final String columnName) {
        return escapeObjectName(columnName, Column.class);
    }

    
    public String escapeColumnNameList(final String columnNames) {
        StringBuffer sb = new StringBuffer();
        for (String columnName : columnNames.split(",")) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(escapeObjectName(columnName.trim(), Column.class));
        }
        return sb.toString();

    }

    
    public boolean supportsSchemas() {
        return true;
    }

    
    public boolean supportsCatalogs() {
        return true;
    }

    public boolean jdbcCallsCatalogsSchemas() {
        return false;
    }

    
    public boolean supportsCatalogInObjectName(final Class<? extends DatabaseObject> type) {
        return false;
    }

    
    public String generatePrimaryKeyName(final String tableName) {
        return "PK_" + tableName.toUpperCase();
    }

    
    public String escapeViewName(final String catalogName, final String schemaName, final String viewName) {
        return escapeObjectName(catalogName, schemaName, viewName, View.class);
    }

    /**
     * Returns the run status for the given ChangeSet
     */
    
    public ChangeSet.RunStatus getRunStatus(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRunStatus(changeSet);
    }

    
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanChangeSet(changeSet);
    }

    /**
     * Returns the ChangeSets that have been run against the current database.
     */
    
    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanChangeSets();
    }

    
    public Date getRanDate(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).getRanDate(changeSet);
    }

    /**
     * After the change set has been ran against the database this method will update the change log table
     * with the information.
     */
    
    public void markChangeSetExecStatus(final ChangeSet changeSet, final ChangeSet.ExecType execType) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).setExecType(changeSet, execType);
    }

    
    public void removeRanStatus(final ChangeSet changeSet) throws DatabaseException {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).removeFromHistory(changeSet);
    }

    
    public String escapeStringForDatabase(final String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll("'", "''");
    }

    
    public void commit() throws DatabaseException {
        try {
            getConnection().commit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    
    public void rollback() throws DatabaseException {
        try {
            getConnection().rollback();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractJdbcDatabase that = (AbstractJdbcDatabase) o;

        if (connection == null) {
            if (that.connection == null) {
                return this == that;
            } else {
                return false;
            }
        } else {
            return connection.equals(that.connection);
        }
    }

    
    public int hashCode() {
        return (connection != null ? connection.hashCode() : super.hashCode());
    }

    
    public void close() throws DatabaseException {
        DatabaseConnection connection = getConnection();
        if (connection != null) {
            if (previousAutoCommit != null) {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (DatabaseException e) {
                    LogFactory.getLogger().warning("Failed to restore the auto commit to " + previousAutoCommit);

                    throw e;
                }
            }
            connection.close();
        }
    }

    
    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    
    public boolean isAutoCommit() throws DatabaseException {
        try {
            return getConnection().getAutoCommit();
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    
    public void setAutoCommit(final boolean b) throws DatabaseException {
        try {
            getConnection().setAutoCommit(b);
        } catch (DatabaseException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Default implementation, just look for "local" IPs. If the database returns a null URL we return false since we don't know it's safe to run the update.
     *
     * @throws liquibase.exception.DatabaseException
     *
     */
    
    public boolean isSafeToRunUpdate() throws DatabaseException {
        DatabaseConnection connection = getConnection();
        if (connection == null) {
            return true;
        }
        String url = connection.getURL();
        if (url == null) {
            return false;
        }
        return (url.contains("localhost")) || (url.contains("127.0.0.1"));
    }

    
    public void executeStatements(final Change change, final DatabaseChangeLog changeLog, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        SqlStatement[] statements = change.generateStatements(this);

        execute(statements, sqlVisitors);
    }

    /*
     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws DatabaseException if there were problems issuing the statements
     */
    
    public void execute(final SqlStatement[] statements, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        for (SqlStatement statement : statements) {
            if (statement.skipOnUnsupported() && !SqlGeneratorFactory.getInstance().supports(statement, this)) {
                continue;
            }
            LogFactory.getLogger().debug("Executing Statement: " + statement.getClass().getName());
            ExecutorService.getInstance().getExecutor(this).execute(statement, sqlVisitors);
        }
    }


    
    public void saveStatements(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) throws IOException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        SqlStatement[] statements = change.generateStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(sql.getEndDelimiter()).append(StreamUtil.getLineSeparator()).append(StreamUtil.getLineSeparator());
            }
        }
    }

    
    public void executeRollbackStatements(final Change change, final List<SqlVisitor> sqlVisitors) throws LiquibaseException, RollbackImpossibleException {
        SqlStatement[] statements = change.generateRollbackStatements(this);
        List<SqlVisitor> rollbackVisitors = new ArrayList<SqlVisitor>();
        if (sqlVisitors != null) {
            for (SqlVisitor visitor : sqlVisitors) {
                if (visitor.isApplyToRollback()) {
                    rollbackVisitors.add(visitor);
                }
            }
        }
        execute(statements, rollbackVisitors);
    }

    
    public void saveRollbackStatement(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) throws IOException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        SqlStatement[] statements = change.generateRollbackStatements(this);
        for (SqlStatement statement : statements) {
            for (Sql sql : SqlGeneratorFactory.getInstance().generateSql(statement, this)) {
                writer.append(sql.toSql()).append(sql.getEndDelimiter()).append("\n\n");
            }
        }
    }

    
    public List<DatabaseFunction> getDateFunctions() {
        return dateFunctions;
    }

    
    public boolean isFunction(final String string) {
        if (string.endsWith("()")) {
            return true;
        }
        for (DatabaseFunction function : getDateFunctions()) {
            if (function.toString().equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    
    public void resetInternalState() {
        ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(this).reset();
        LockServiceFactory.getInstance().getLockService(this).reset();
    }

    
    public boolean supportsForeignKeyDisable() {
        return false;
    }

    
    public boolean disableForeignKeyChecks() throws DatabaseException {
        throw new DatabaseException("ForeignKeyChecks Management not supported");
    }

    
    public void enableForeignKeyChecks() throws DatabaseException {
        throw new DatabaseException("ForeignKeyChecks Management not supported");
    }

    
    public boolean createsIndexesForForeignKeys() {
        return false;
    }

    
    public int getDataTypeMaxParameters(final String dataTypeName) {
        return 2;
    }

    public CatalogAndSchema getSchemaFromJdbcInfo(final String rawCatalogName, final String rawSchemaName) {
        return this.correctSchema(new CatalogAndSchema(rawCatalogName, rawSchemaName));
    }

    public String getJdbcCatalogName(final CatalogAndSchema schema) {
        return correctObjectName(schema.getCatalogName(), Catalog.class);
    }

    public String getJdbcSchemaName(final CatalogAndSchema schema) {
        return correctObjectName(schema.getSchemaName(), Schema.class);
    }

    public final String getJdbcCatalogName(final Schema schema) {
        if (schema == null) {
            return getJdbcCatalogName(getDefaultSchema());
        } else {
            return getJdbcCatalogName(new CatalogAndSchema(schema.getCatalogName(), schema.getName()));
        }
    }

    public final String getJdbcSchemaName(final Schema schema) {
        if (schema == null) {
            return getJdbcSchemaName(getDefaultSchema());
        } else {
            return getJdbcSchemaName(new CatalogAndSchema(schema.getCatalogName(), schema.getName()));
        }
    }

    
    public boolean dataTypeIsNotModifiable(final String typeName) {
        return unmodifiableDataTypes.contains(typeName.toLowerCase());
    }

    
    public void setObjectQuotingStrategy(final ObjectQuotingStrategy quotingStrategy) {
        this.quotingStrategy = quotingStrategy;
    }

    
    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return this.quotingStrategy;
    }

    
    public String generateDatabaseFunctionValue(final DatabaseFunction databaseFunction) {
        if (databaseFunction.getValue() == null) {
            return null;
        }
        if (isCurrentTimeFunction(databaseFunction.getValue().toLowerCase())) {
            return getCurrentDateTimeFunction();
        } else if (databaseFunction instanceof SequenceNextValueFunction) {
            if (sequenceNextValueFunction == null) {
                throw new RuntimeException(String.format("next value function for a sequence is not configured for database %s",
                        getDefaultDatabaseProductName()));
            }
            return String.format(sequenceNextValueFunction, escapeObjectName(databaseFunction.getValue(), Sequence.class));
        } else if (databaseFunction instanceof SequenceCurrentValueFunction) {
            if (sequenceCurrentValueFunction == null) {
                throw new RuntimeException(String.format("current value function for a sequence is not configured for database %s",
                        getDefaultDatabaseProductName()));
            }
            return String.format(sequenceCurrentValueFunction, escapeObjectName(databaseFunction.getValue(), Sequence.class));
        } else {
            return databaseFunction.getValue();
        }
    }

    private boolean isCurrentTimeFunction(final String functionValue) {
        return functionValue.startsWith("current_timestamp")
                || functionValue.startsWith("current_datetime")
                || getCurrentDateTimeFunction().equalsIgnoreCase(functionValue);
    }

    
    public String getCurrentDateTimeFunction() {
        return currentDateTimeFunction;
    }

 	
    public void setOutputDefaultSchema(final boolean outputDefaultSchema) {
		this.outputDefaultSchema = outputDefaultSchema;

 	}

    
    public boolean isDefaultSchema(final String catalog, final String schema) {
        if (!supportsSchemas()) {
            return true;
        }

        if (!isDefaultCatalog(catalog)) {
            return false;
        }
        return schema == null || schema.equalsIgnoreCase(getDefaultSchemaName());
    }

    
    public boolean isDefaultCatalog(final String catalog) {
        if (!supportsCatalogs()) {
            return true;
        }

        return catalog == null || catalog.equalsIgnoreCase(getDefaultCatalogName());

    }

 	
    public boolean getOutputDefaultSchema() {
 		return outputDefaultSchema;
 	}

    
    public boolean getOutputDefaultCatalog() {
        return outputDefaultCatalog;
    }

    
    public void setOutputDefaultCatalog(final boolean outputDefaultCatalog) {
        this.outputDefaultCatalog = outputDefaultCatalog;
    }

    
    public boolean supportsPrimaryKeyNames() {
        return true;
    }

    
	public String getSystemSchema(){
    	return "information_schema";
    }
}