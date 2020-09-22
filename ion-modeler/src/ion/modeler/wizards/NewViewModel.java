package ion.modeler.wizards;

public class NewViewModel {
	public String type;
	public String className;
	public Integer overrideMode;
	public Integer actions;

	public NewViewModel(Integer actions) {
		type = "";
		className = "";
		overrideMode = null;
		this.actions = actions;
	}
}
