package liquibase.diff;

public class Difference {
    private String message;
    private String field;
    private Object referenceValue;
    private Object comparedValue;

    public Difference(String field, Object referenceValue, Object comparedValue) {
        this(null, field, referenceValue, comparedValue);
    }

    public Difference(String message, String field, Object referenceValue, Object comparedValue) {
        if (message == null) {
            message = field+" changed from '"+referenceValue+"' to '"+comparedValue+"'";
        }
        this.message = message;
        this.field = field;
        this.referenceValue = referenceValue;
        this.comparedValue = comparedValue;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public Object getReferenceValue() {
        return referenceValue;
    }

    public Object getComparedValue() {
        return comparedValue;
    }

    
    public String toString() {
        return message;
    }
}
