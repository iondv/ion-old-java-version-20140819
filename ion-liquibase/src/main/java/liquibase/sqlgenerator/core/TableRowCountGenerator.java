package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.TableRowCountStatement;

public class TableRowCountGenerator extends AbstractSqlGenerator<TableRowCountStatement> {

    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    
    public boolean supports(TableRowCountStatement statement, Database database) {
        return true;
    }

    
    public ValidationErrors validate(TableRowCountStatement dropColumnStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropColumnStatement.getTableName());
        return validationErrors;
    }

    protected String generateCountSql(TableRowCountStatement statement, Database database) {
        return "SELECT COUNT(*) FROM "+database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }

    
    public Sql[] generateSql(TableRowCountStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] { new UnparsedSql(generateCountSql(statement, database)) };
    }


}
