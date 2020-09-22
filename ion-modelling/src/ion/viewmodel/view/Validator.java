package ion.viewmodel.view;

public class Validator {
	public String name;
	public String caption;
	public Boolean assignByContainer;
	public String validationExpression;
	public String mask;

	public Validator() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Validator(String name, String caption, Boolean assignByContainer, String validationExpression, String mask) {
		this.name = name;
		this.caption = caption;
		this.assignByContainer = assignByContainer;
		this.validationExpression = validationExpression;
		this.mask = mask;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getValidationExpression() {
		return validationExpression;
	}
	public void setValidationExpression(String validationExpression) {
		this.validationExpression = validationExpression;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public Boolean getAssignByContainer() {
		return assignByContainer;
	}
	public void setAssignByContainer(Boolean assignByContainer) {
		this.assignByContainer = assignByContainer;
	}
	
}
