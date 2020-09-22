package ion.offline.net;

public enum DataChangeType {
	CREATE ("create"),
	UPDATE ("update"),
	DELETE ("delete"),
	PUT ("put");
	
    private final String v;

    private DataChangeType(final String code) {
        v = code;
    }

    public String getValue() { return v; }	
    
    public static DataChangeType fromString(String v){
    	if (v.equals("create"))
    		return CREATE;
    	else if (v.equals("update"))
    		return UPDATE;
    	else if (v.equals("delete"))
    		return DELETE;
    	else if (v.equals("put"))
    		return PUT;
    	return null;
    }
}
