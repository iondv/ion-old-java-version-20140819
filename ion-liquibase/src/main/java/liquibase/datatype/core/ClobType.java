package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;

@DataTypeInfo(name="clob", aliases = {"longvarchar", "text", "longtext", "java.sql.Types.LONGVARCHAR", "java.sql.Types.CLOB"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class ClobType extends LiquibaseDataType {

    private String originalDefinition;

    
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        }
        String val = String.valueOf(value);
        // postgres type character varying gets identified as a char type
        // simple sanity check to avoid double quoting a value
        if (val.startsWith("'")) {
            return val;
        } else {
            return "'"+database.escapeStringForDatabase(val)+"'";
        }
    }

    
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(this.originalDefinition);

        if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("BLOB SUB_TYPE TEXT");
        } else if (database instanceof SybaseASADatabase) {
            return new DatabaseDataType("LONG VARCHAR");
        } else if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType("NVARCHAR", "MAX");
        } else if (database instanceof MySQLDatabase) {
            if (originalDefinition.toLowerCase().startsWith("text")) {
                return new DatabaseDataType("TEXT");
            } else {
                return new DatabaseDataType("LONGTEXT");
            }
        } else if (database instanceof H2Database || database instanceof HsqlDatabase) {
            if (originalDefinition.toLowerCase().startsWith("longvarchar") || originalDefinition.startsWith("java.sql.Types.LONGVARCHAR")) {
                return new DatabaseDataType("LONGVARCHAR");
            } else {
                return new DatabaseDataType("CLOB");
            }
        } else if (database instanceof PostgresDatabase || database instanceof SQLiteDatabase || database instanceof SybaseDatabase) {
            return new DatabaseDataType("TEXT");
        } else if (database instanceof OracleDatabase) {
            return new DatabaseDataType("CLOB");
        }
        return super.toDatabaseDataType(database);
    }

    
    public void finishInitialization(String originalDefinition) {
        super.finishInitialization(originalDefinition);
        this.originalDefinition = originalDefinition;
    }

    //sqlite
    //        } else if (columnTypeString.equals("TEXT") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uuid") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uniqueidentifier") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).equals("uniqueidentifier") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).equals("datetime") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("timestamp") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("char") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("clob") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("text")) {
//            type = new CustomType("TEXT",0,0);

}
