package ion.core;

public enum HistoryMode {
	NONE(0), 
	OCCASIONAL(1), 
	HOURLY(2),
	DAILY(3),
	WEEKLY(4),
	MONTHLY(5),
	ANNUAL(6);
	
  private final int v;

  private HistoryMode(final int code) {
      v = code;
  }

  public int getValue() { return v; }	
  
  public static HistoryMode fromInt(int v){
  	switch (v){
  		case 0:return NONE;
  		case 1:return OCCASIONAL;
  		case 2:return HOURLY;
  		case 3:return DAILY;
  		case 4:return WEEKLY;
  		case 5:return MONTHLY;
  		case 6:return ANNUAL;
  	}
  	return null;
  }
}
