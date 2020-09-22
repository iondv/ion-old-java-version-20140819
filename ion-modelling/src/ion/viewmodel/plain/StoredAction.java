package ion.viewmodel.plain;

public class StoredAction {
	
	public String id;
	public String caption;
	public String visibilityCondition = null;
	public String enableCondition = null;
	public boolean needSelectedItem = false;
	public boolean signBefore = false;
	public boolean signAfter = false;
	
	public StoredAction() {};
	
	public StoredAction(String id, String caption, String visibilityCondition, String enableCondition, boolean needSelectedItem, boolean signBefore, boolean signAfter) {
		this.id = id;
		this.caption = caption;
		this.visibilityCondition = visibilityCondition;
		this.enableCondition = enableCondition;
		this.needSelectedItem = needSelectedItem;
		this.signBefore = signBefore;
		this.signAfter = signAfter;
	}
}
