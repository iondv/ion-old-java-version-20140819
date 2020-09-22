package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertOrUpdateGenerator;
import liquibase.statement.core.InsertOrUpdateStatement;

import java.util.regex.Matcher;

public class InsertOrUpdateGeneratorH2 extends InsertOrUpdateGenerator {
    
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
        return database instanceof H2Database;
    }

    
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String insertStatement = super.getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain);
        return insertStatement.replaceAll("(?i)insert into (.+) (values .+)", "MERGE INTO $1 KEY(" + Matcher.quoteReplacement(insertOrUpdateStatement.getPrimaryKey()) + ") $2");
    }

    
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) {
        return "";
    }

    
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return "";
    }

    
    protected String getElse(Database database) {
        return "";
    }

}