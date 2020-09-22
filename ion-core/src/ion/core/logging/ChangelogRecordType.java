package ion.core.logging;

public enum ChangelogRecordType {
	CREATION("create"), 
	UPDATE("update"),
	DELETION("delete"),
	PUT("put"),
	EJECT("eject");
	
	private final String v;
	
	private ChangelogRecordType(String code){
		v = code;
	}
	
	public String getValue() { return v; }
    
    public static ChangelogRecordType fromString(String code){
    	switch (code){
	    	case "create":return CREATION;
	    	case "update":return UPDATE;
	    	case "delete":return DELETION;
	    	case "put":return PUT;
	    	case "eject":return EJECT;
    	}
    	return null;
    }    	
}
