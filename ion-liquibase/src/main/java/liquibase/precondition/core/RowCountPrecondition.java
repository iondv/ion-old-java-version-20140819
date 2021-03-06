package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutorService;
import liquibase.precondition.Precondition;
import liquibase.statement.core.TableRowCountStatement;
import liquibase.util.StringUtils;

public class RowCountPrecondition implements Precondition {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private Integer expectedRows;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getExpectedRows() {
        return expectedRows;
    }

    public void setExpectedRows(Integer expectedRows) {
        this.expectedRows = expectedRows;
    }

    
    public Warnings warn(Database database) {
        return new Warnings();
    }

    
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", tableName);
        validationErrors.checkRequiredField("expectedRows", expectedRows);

        return validationErrors;
    }

    
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            TableRowCountStatement statement = new TableRowCountStatement(catalogName, schemaName, tableName);

            int result = ExecutorService.getInstance().getExecutor(database).queryForInt(statement);
            if (result != expectedRows) {
                throw new PreconditionFailedException(getFailureMessage(result), changeLog, this);
            }

        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    protected String getFailureMessage(int result) {
        return "Table "+tableName+" is not empty. Contains "+result+" rows";
    }

    
    public String getName() {
        return "rowCount";
    }

}
