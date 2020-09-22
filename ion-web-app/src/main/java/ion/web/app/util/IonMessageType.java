package ion.web.app.util;

public enum IonMessageType {
	ERROR(0),
	INFO(1),
	WARNING(2);
	
  private final int v;

  private IonMessageType(final int code) {
      v = code;
  }

  public int getValue() { return v; }	
  
  public static IonMessageType fromInt(int v){
  	switch (v){
  		case 0:return ERROR;
  		case 1:return INFO;
  		case 2:return WARNING;
  	}
  	return null;
  }
}
