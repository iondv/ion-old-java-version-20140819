package ion.core;

public interface IProperty {
	IPropertyMeta Meta();
	
	String getName();
	
	MetaPropertyType getType();

	String getCaption();
	
	Boolean getReadOnly();
	
	Boolean getIndexed();

	Boolean getUnique();
	
	Boolean getNullable();
		
	IItem getItem();
	
	Object getValue() throws IonException;

	String getString();
		
	void setValue(Object value) throws IonException;	
}
