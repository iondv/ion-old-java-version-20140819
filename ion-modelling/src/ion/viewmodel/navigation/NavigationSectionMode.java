package ion.viewmodel.navigation;

public enum NavigationSectionMode {
	MENU(0),
	TOC(1),
	COMBO(2),
	HCOMBO(3);
	
  private final int v;

  private NavigationSectionMode(final int code) {
      v = code;
  }

  public int getValue() { return v; }
  
  public static NavigationSectionMode fromInt(int v){
  	switch (v){
    	case 0:return MENU;
    	case 1:return TOC;
    	case 2:return COMBO;
    	case 3:return HCOMBO;
 	}
  	return null;
  }  
}
