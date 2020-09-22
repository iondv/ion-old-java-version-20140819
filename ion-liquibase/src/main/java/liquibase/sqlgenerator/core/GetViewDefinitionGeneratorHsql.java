package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.structure.core.Schema;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorHsql extends GetViewDefinitionGenerator {
    
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof HsqlDatabase;
    }

    
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = database.correctSchema(new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()));

        return new Sql[] {
                    new UnparsedSql("SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '" + statement.getViewName() + "' AND TABLE_SCHEMA='" + schema.getSchemaName() + "'")
            };
    }
}