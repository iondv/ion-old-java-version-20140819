package ion.framework.meta.plain;

public class StoredValidatorMeta {
	public String name;
	public String caption;
	public Boolean assignByContainer;
	public String validationExpression;
	public String mask;
	
	public StoredValidatorMeta() {
		
	}

	public StoredValidatorMeta(String name, String caption, Boolean assignByContainer,
			String validationExpression, String mask) {
		this.name = name;
		this.caption = caption;
		this.assignByContainer = assignByContainer;
		this.validationExpression = validationExpression;
		this.mask = mask;
	}

}