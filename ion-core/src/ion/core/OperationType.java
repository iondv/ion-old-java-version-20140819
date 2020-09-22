package ion.core;

public enum OperationType {

	AND(0), 
	OR(1), 
	NOT(2),
	MIN(3),
	MAX(4);
	
  private final int v;

  private OperationType(final int code) {
      v = code;
  }

  public int getValue() { return v; }	
  
  public static OperationType fromInt(int v){
  	switch (v){
  		case 0:return AND;
  		case 1:return OR;
  		case 2:return NOT;
  		case 3:return MIN;
  		case 4:return MAX;
  	}
  	return null;
  }
  
  public static String getString(int v){
  	switch (v){
  		case 0:return "and";
  		case 1:return "or";
  		case 2:return "not";
  		case 3:return "min";
  		case 4:return "max";
  	}
  	return null;
  }
}
