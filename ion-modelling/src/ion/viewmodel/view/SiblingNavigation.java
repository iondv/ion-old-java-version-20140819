package ion.viewmodel.view;

public class SiblingNavigation {
	
	private String[] fixBy;
	
	private String[] navigateBy;
	
	
	public SiblingNavigation(String[] fix, String[] nav){
		this.fixBy = fix;
		this.navigateBy = nav;
	}
	
	public String[] getFixBy(){
		return fixBy;
	}
	
	public String[] getNavigateBy(){
		return navigateBy;
	}
}
