package ion.core.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ion.core.IMetaRepository;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IUserTypeMeta;
import ion.core.IonException;

public class MetaRepositoryMock implements IMetaRepository {

	private Map<String, IStructMeta> structures;
	private Map<String, Map<String, IPropertyMeta>> properties;
	private Map<String,String> ancestors;
		
	public MetaRepositoryMock() {
		super();
		this.structures = new HashMap<String, IStructMeta>();
		this.properties = new HashMap<String, Map<String, IPropertyMeta>>();
		this.ancestors = new HashMap<String, String>();
	}
	
	public void addStruct(IStructMeta struct, Map<String, IPropertyMeta> props) throws IonException {
		structures.put(struct.getName(), struct);
		properties.put(struct.getName(), props);
	}
	
	@Override
	public IStructMeta Get(String name) throws IonException {return structures.get(name);}

	@Override
	public IStructMeta Ancestor(String name) throws IonException {
		String ancestor = ancestors.get(name);
		if(ancestor!=null){
			return structures.get(ancestor);
		}
		return null;
	}
	
	public void setAncestor(String name, String ancestorName){
		ancestors.put(name, ancestorName);
	}
	
	public Map<String, IStructMeta> getStructures(){
		return structures;
	}

	@Override
	public Collection<IStructMeta> List() throws IonException {return structures.values();}

	@Override
	public Collection<IStructMeta> List(String ancestor) throws IonException {
		Collection<IStructMeta> result = new ArrayList<IStructMeta>();
		for(Entry<String, IStructMeta> e: structures.entrySet()){
			IStructMeta anc = e.getValue().checkAncestor(ancestor);
			if(anc != null && !anc.getName().equals(e.getValue().getName()))
				result.add(e.getValue());
		}
		return result;
	}

	@Override
	public Collection<IStructMeta> List(String ancestor, Boolean direct) throws IonException {return List(ancestor);}

	@Override
	public Map<String, IPropertyMeta> PropertyMetas(String name) throws IonException {return properties.get(name);}

	@Override
	public IUserTypeMeta GetUserType(String name) throws IonException {
		// TODO Auto-generated method stub
		return null;
	}
}
