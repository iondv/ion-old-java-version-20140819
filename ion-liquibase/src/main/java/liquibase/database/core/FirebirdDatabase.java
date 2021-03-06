package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.structure.DatabaseObject;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Table;

/**
 * Firebird database implementation.
 * SQL Syntax ref: http://www.ibphoenix.com/main.nfs?a=ibphoenix&page=ibp_60_sqlref
 */
public class FirebirdDatabase extends AbstractJdbcDatabase {

    public FirebirdDatabase() {
        super.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
        super.sequenceNextValueFunction="NEXT VALUE FOR %s";
    }

    
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("Firebird");
    }

    
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:firebirdsql")) {
            return "org.firebirdsql.jdbc.FBDriver";
        }
        return null;
    }

    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    
    public String getShortName() {
        return "firebird";
    }

    
    public Integer getDefaultPort() {
        return 3050;
    }

    
    protected String getDefaultDatabaseProductName() {
        return "Firebird";
    }

    
    public boolean supportsSequences() {
        return true;
    }

    
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    
    public boolean supportsTablespaces() {
        return false;
    }


    
    public boolean supportsDDLInTransaction() {
        return false;
    }

    
    public boolean isSystemObject(DatabaseObject example) {
        if (example instanceof Table && example.getName().startsWith("RDB$")) {
            return true;
        }
        return super.isSystemObject(example);    //To change body of overridden methods use File | Settings | File Templates.
    }

    
    public boolean supportsAutoIncrement() {
        return false;
    }

    
    public boolean supportsSchemas() {
        return false;
    }

    
    public boolean supportsCatalogs() {
        return false;
    }

    
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }
        return objectName.toUpperCase().trim();
    }

    
    public boolean createsIndexesForForeignKeys() {
        return true;
    }
}
