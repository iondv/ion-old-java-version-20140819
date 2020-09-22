package ion.core;

public interface IPropertyMeta {
	
	String Name();
	
	String Caption();
	
	MetaPropertyType Type();
	
	Short Size();
	
	Short Decimals();
	
	Boolean ReadOnly();
	 
	Boolean Nullable();
	
	Boolean Unique();
	
	Boolean AutoAssigned();

	Boolean Indexed();
		
	Object DefaultValue();
	
	String Hint();
	
	ISelectionProvider Selection() throws IonException;
	
	void SetSelection(ISelectionProvider provider);
	
	Integer OrderNumber();
	
	Boolean IndexSearch();
	
	String Formula();
}
