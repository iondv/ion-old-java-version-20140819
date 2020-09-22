package ion.viewmodel.view;

public class DataField extends Field implements IDataField {
	
	String maskName;

	String mask;
	
	FieldSize size;

	public DataField(String caption, String property, FieldSize size, String maskName, FieldType type, Integer order_number, String hint, String mask, Boolean readonly){
		super(caption, type, property, order_number, hint, readonly);
		this.property = property;
		this.size = size;
		this.maskName = maskName;
		this.mask = mask;
	}
	
	public DataField(String caption, String property, FieldSize size, String maskName, FieldType type, Integer order_number, String hint, Boolean required, String mask, Boolean readonly){
		super(caption, type, property, order_number, hint, readonly);
		this.property = property;
		this.size = size;
		this.maskName = maskName;
		this.required = required;
		this.mask = mask;
	}	
	
	public DataField(String caption, String property, FieldSize size, String maskName, FieldType type, Integer order_number, String hint, Boolean readonly){
		this(caption, property, size, maskName, type, order_number, hint, "", readonly);
	}
	
	public DataField(String caption, String property, FieldType type, FieldSize size, Integer order_number, Boolean required, String hint, Boolean readonly){
		this(caption,property,size,"",type,order_number, hint, readonly);
		this.required = required;
	}	

	public DataField(String caption, String property, FieldType type, FieldSize size, Integer order_number, String hint, Boolean readonly){
		this(caption,property,size,"",type,order_number,hint,readonly);
	}
	
	public DataField(String caption, String property, FieldType type, FieldSize size, Boolean readonly, String hint){
		this(caption,property,type,size,0,hint,readonly);
	}
	
	public DataField(String caption, String property, FieldType type, Boolean readonly, String hint){
		this(caption,property,type,FieldSize.MEDIUM, readonly,hint);
	}			
	
	public DataField(String caption, String property, FieldSize size, String maskName, Integer order_number, Boolean readonly, String hint) {
		this(caption, property, size, maskName, FieldType.TEXT, order_number, hint, readonly);
	}
	
	public DataField(String caption, String property, FieldSize size, Boolean readonly, String hint) {
		this(caption, property, size,"",0, readonly, hint);
	}
	
	public DataField(String caption, String property, FieldSize size, Integer order_number, Boolean readonly, String hint) {
		this(caption, property, size,"", FieldType.TEXT, order_number, hint, readonly);
	}
	
	public DataField(String caption, String property, FieldSize size, Integer order_number, Boolean required, Boolean readonly, String hint) {
		this(caption, property, size,"", FieldType.TEXT, order_number, hint, readonly);
		this.required = required;
	}	

	@Override
	public String getProperty() {
		return property;
	}

	@Override
	public FieldSize getSize() {
		return size;
	}

	@Override
	public String getMaskName() {
		return maskName;
	}
	
	@Override
	public String getMask() {
		return mask;
	}
}
