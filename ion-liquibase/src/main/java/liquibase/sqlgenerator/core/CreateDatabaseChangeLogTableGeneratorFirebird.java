package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangeLogTableGeneratorFirebird extends CreateDatabaseChangeLogTableGenerator {
    
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
    }
}
