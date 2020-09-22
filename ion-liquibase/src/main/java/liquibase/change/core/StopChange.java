package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RuntimeStatement;
import liquibase.util.StringUtils;

@DatabaseChange(name="stop", description = "Stops Liquibase execution with a message. Mainly useful for debugging and stepping through a changelog", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.9")
public class StopChange extends AbstractChange {

    private String message ="Stop command in changelog file";

    
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    @DatabaseChangeProperty(description = "Message to output when execution stops", exampleValue = "What just happened???")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new RuntimeStatement() {
            
            public Sql[] generate(Database database) {
                throw new StopChangeException(getMessage());
            }
        }};

    }

    
    public String getConfirmationMessage() {
        return "Changelog Execution Stopped";
    }

    public static class StopChangeException extends RuntimeException {
        public StopChangeException(String message) {
            super(message);
        }
    }

    
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
