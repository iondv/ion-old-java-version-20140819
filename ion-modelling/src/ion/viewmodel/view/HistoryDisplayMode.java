package ion.viewmodel.view;

public enum HistoryDisplayMode {
	BYCLASS(0),
	HIDE(1);
	
  private final int v;

  private HistoryDisplayMode(final int code) {
     v = code;
  }

  public int getValue() { return v; }
    
  public static HistoryDisplayMode fromInt(int v){
    switch (v){
	    case 0:return BYCLASS;
	    case 1:return HIDE;
   	}
    return null;
  }    
}
