package liquibase.precondition.core;

public class TableIsEmptyPrecondition extends RowCountPrecondition {

    public TableIsEmptyPrecondition() {
        this.setExpectedRows(0);
    }

    
    protected String getFailureMessage(int result) {
        return "Table "+getTableName()+" is not empty. Contains "+result+" rows";
    }

    
    public String getName() {
        return "tableIsEmpty";
    }

}
