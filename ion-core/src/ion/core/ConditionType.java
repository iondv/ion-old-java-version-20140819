package ion.core;

public enum ConditionType {
	EQUAL(0), 
	NOT_EQUAL(1), 
	EMPTY(2), 
	NOT_EMPTY(3), 
	LIKE(4), 
	LESS(5), 
	MORE(6), 
	LESS_OR_EQUAL(7), 
	MORE_OR_EQUAL(8), 
	IN(9),
	CONTAINS(10);
	
    private final int v;

    private ConditionType(final int code) {
        v = code;
    }

    public int getValue() { return v; }	
    
    public static ConditionType fromInt(int v){
    	switch (v){
    		case 0:return EQUAL;
    		case 1:return NOT_EQUAL;
    		case 2:return EMPTY;
    		case 3:return NOT_EMPTY;
    		case 4:return LIKE;
    		case 5:return LESS;
    		case 6:return MORE;
    		case 7:return LESS_OR_EQUAL;
    		case 8:return MORE_OR_EQUAL;
    		case 9:return IN;
    		case 10:return CONTAINS;
    	}
    	return null;
    }
}
