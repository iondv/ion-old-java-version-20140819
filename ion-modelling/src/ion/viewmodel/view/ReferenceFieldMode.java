package ion.viewmodel.view;

public enum ReferenceFieldMode {
	STRING(0),
	LINK(1),
	INFO(2),
	HIERARCHY(3);
	
    private final int v;

    private ReferenceFieldMode(final int code) {
        v = code;
    }

    public int getValue() { return v; }
    
    public static ReferenceFieldMode fromInt(int v){
    	switch (v){
	    	case 0:return STRING;
	    	case 1:return LINK;
	    	case 2:return INFO;
	    	case 3:return HIERARCHY;
   	}
    	return null;
    }    
}