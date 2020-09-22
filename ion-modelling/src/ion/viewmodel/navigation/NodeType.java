package ion.viewmodel.navigation;

public enum NodeType {
	GROUP(0),
	CLASS(1),
	CONTAINER(2),
	HYPERLINK(3);
	
  private final int v;

  private NodeType(final int code) {
      v = code;
  }

  public int getValue() { return v; }
  
  public static NodeType fromInt(int v){
  	switch (v){
    	case 0:return GROUP;
    	case 1:return CLASS;
    	case 2:return CONTAINER;
    	case 3:return HYPERLINK;
 	}
  	return null;
  }    
}