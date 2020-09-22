package ion.viewmodel.view;

import java.util.Collection;

public abstract class Field implements IField {

	protected String caption;
	
	protected FieldType type;
	
	protected String property;
	
	protected Integer order_number;
	
	protected Boolean required = false;
	
	protected String visibilityExpression;
	
	protected String enablementExpression;
	
	protected String obligationExpression;
	
	protected Boolean readonly;
	
	protected String hint;

	protected Collection<String> validators;
	
	protected Collection<FieldAction> actions;

	public Field(String caption, FieldType type, String property, Integer order_number, String hint, Boolean required, String visexpr, String eexpr, String obligexpr, Boolean readonly, Collection<String> valid_list, Collection<FieldAction> actions) {
		this.caption = caption;
		this.type = type;
		this.property = property;
		this.hint = hint;
		this.order_number = order_number;
		this.required = required;
		this.visibilityExpression = visexpr;
		this.enablementExpression = eexpr;
		this.obligationExpression = obligexpr;
		this.readonly = readonly;
		this.validators = valid_list;
		this.actions = actions;
	}
	
	public Field(String caption, FieldType type, String property, Integer order_number, String hint, String visexpr, String eexpr, Boolean readonly) {
		this(caption,type,property,order_number,hint,false,visexpr,eexpr,null,readonly, null, null);
	}		

	public Field(String caption, FieldType type, String property, Integer order_number, String hint, String visexpr, Boolean readonly) {
		this(caption,type,property,order_number,hint,visexpr,null,readonly);
	}		
	
	public Field(String caption, FieldType type, String property, Integer order_number, String hint, Boolean readonly) {
		this(caption,type,property,order_number, hint, null, readonly);
	}		
	

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public FieldType getType() {
		return type;
	}
	
	@Override
	public String getProperty() {
		return property;
	}
	
	@Override
	public Integer getOrderNumber() {
		return (order_number == null)?0:order_number;
	}
	
	@Override
	public Boolean getRequired(){
		return required;
	}
	
	@Override
	public String getHint(){
		return hint;
	}
	
	@Override
	public String getVisibilityExpression(){
		return visibilityExpression;
	}
	
	@Override
	public String getEnablementExpression(){
		return enablementExpression;
	}
	
	@Override
	public String getObligationExpression(){
		return obligationExpression;
	}
	
	@Override
	public Boolean isReadOnly(){
		return readonly;
	}
	
	@Override
	public Collection<String> getValidators() {
		return validators;
	}
	
	@Override
	public Collection<FieldAction> getActions() {
		return actions;
	}

	public void setValidators(Collection<String> validators) {
		this.validators = validators;
	}
	
	public void setVisibilityExpression(String expr){
		visibilityExpression = expr;
	}
	
	public void setEnablementExpression(String expr){
		enablementExpression = expr;
	}
	
	public void setObligationExpression(String expr){
		obligationExpression = expr;
	}	
	
	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}
	
	public void setType(FieldType t){
		this.type = t;
	}
	
	@Override
	public int compareTo(IField field) {
		return this.getOrderNumber() - field.getOrderNumber();
	}
		
	public boolean equals(Object o){
		return (o instanceof IField)?this.getProperty().equals(((IField)o).getProperty()):false;
	}
}
