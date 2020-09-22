package ion.core;

import java.util.Map;

public interface IItem {
	String getItemId();
	
	String getClassName();
	
	IStructMeta getMetaClass();
	
	Object Get(String name);
	
	void Set(String name, Object value);
	
	IProperty Property(String name) throws IonException;
	
	Map<String, IProperty> getProperties() throws IonException;
	
	String toString();
}
