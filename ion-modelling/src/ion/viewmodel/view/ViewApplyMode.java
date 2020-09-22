package ion.viewmodel.view;

public enum ViewApplyMode {
	HIDE(0),
	OVERRIDE(1);
	
	private final int v;

	private ViewApplyMode(int v) {
		this.v = v;
	}
	
	public int getValue() { return v; }
	
    public static ViewApplyMode fromInt(int v){
    	switch (v){
    		case 0:return HIDE;
    		case 1:return OVERRIDE;
    	}
    	return null;
    }
	
}
