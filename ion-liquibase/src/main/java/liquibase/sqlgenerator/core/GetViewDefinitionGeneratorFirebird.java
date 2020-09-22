package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorFirebird extends GetViewDefinitionGenerator {
    
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
    }

    
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("select rdb$view_source from rdb$relations where upper(rdb$relation_name)='" + statement.getViewName() + "'")
        };
    }
}