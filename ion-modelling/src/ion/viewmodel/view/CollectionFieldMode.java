package ion.viewmodel.view;

public enum CollectionFieldMode {
	LIST(0),
	LINK(1),
	LINKS(2),
	TABLE(3),
	HASHTAGS(4);
	
    private final int v;

    private CollectionFieldMode(final int code) {
        v = code;
    }

    public int getValue() { return v; }
    
    public static CollectionFieldMode fromInt(int v){
    	switch (v){
	    	case 0:return LIST;
	    	case 1:return LINK;
	    	case 2:return LINKS;
	    	case 3:return TABLE;
	    	case 4:return HASHTAGS;
    	}
    	return null;
    }    
}