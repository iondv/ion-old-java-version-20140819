package ion.core;

import java.util.Collection;
import java.util.Map;

public interface IStructMeta {
	String getName();
	
	String getCaption();
	
	String Semantic();	
	
	String Version();
	
	IStructMeta getAncestor() throws IonException;
	
	public IStructMeta checkAncestor(String name)  throws IonException;
	
	Collection<IStructMeta> Descendants(Boolean direct) throws IonException;
	
	Collection<IStructMeta> Descendants() throws IonException;	
		
	IPropertyMeta PropertyMeta(String name) throws IonException;
	
	Map <String, IPropertyMeta> PropertyMetas() throws IonException;
}
