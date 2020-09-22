package ion.core;

import java.util.Collection;
import java.util.Map;

public interface IMetaRepository {
	IStructMeta Get(String name) throws IonException;
	
	Collection<IStructMeta> List() throws IonException;
	
	Collection<IStructMeta> List(String ancestor) throws IonException;	

	Collection<IStructMeta> List(String ancestor, Boolean direct) throws IonException;	
	
	IStructMeta Ancestor(String name) throws IonException;
	
	Map<String, IPropertyMeta> PropertyMetas(String name) throws IonException;

	IUserTypeMeta GetUserType(String name) throws IonException;
}
