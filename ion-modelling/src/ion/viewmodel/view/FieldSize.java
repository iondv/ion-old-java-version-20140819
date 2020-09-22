package ion.viewmodel.view;

public enum FieldSize {
	TINY(0),
	SHORT(1),
	MEDIUM(2),
	LONG(3),
	BIG(4);
	
    private final int v;

    private FieldSize(final int code) {
        v = code;
    }

    public int getValue() { return v; }
    
    public static FieldSize fromInt(int v){
    	switch (v){
	    	case 0:return TINY;
	    	case 1:return SHORT;
	    	case 2:return MEDIUM;
	    	case 3:return LONG;
	    	case 4:return BIG;
    	}
    	return null;
    }    
}