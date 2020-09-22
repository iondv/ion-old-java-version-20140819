package ion.core;

public enum SortingMode {
	ASC(0), 
	DESC(1), 
	RANDOM(2);
	
	
    private final int v;

    private SortingMode(final int code) {
        v = code;
    }

    public int getValue() { return v; }		
    
    public static SortingMode fromInt(int v){
    	switch (v){
    		case 0:return ASC;
    		case 1:return DESC;
    		case 2:return RANDOM;
    	}
    	return ASC;
    }
}
