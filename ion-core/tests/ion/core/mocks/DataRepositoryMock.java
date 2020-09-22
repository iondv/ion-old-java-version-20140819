package ion.core.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ion.core.IDataRepository;
import ion.core.IInputValidator;
import ion.core.IItem;
import ion.core.IMetaRepository;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.logging.IChangeLogger;

public class DataRepositoryMock implements IDataRepository {
	
	protected Map<String, Collection<IItem>> data;
	protected Map<String, Class<?>> domain;
	protected IMetaRepository metaRepository;
	protected int idCounter;
	
	public DataRepositoryMock(Map<String, Class<?>> domain, IMetaRepository metaRepository) {
		this.domain = domain;
		this.metaRepository = metaRepository;
		this.idCounter = 0;
		this.data = new HashMap<String, Collection<IItem>>();
		for(Entry<String, Class<?>> c:domain.entrySet())
			data.put(c.getKey(), new ArrayList<IItem>());
	}

	@Override
	public long GetCount(String classname) throws IonException {return data.get(classname).size();}

	@Override
	public long GetCount(String classname, ListOptions options)	throws IonException {return GetCount(classname);}

	@Override
	public long GetCount(IItem dummy) throws IonException {return GetCount(dummy.getClassName());}

	@Override
	public long GetCount(IItem dummy, ListOptions options) throws IonException {return GetCount(dummy);}

	@Override
	public Collection<IItem> GetList(String classname) throws IonException {return data.get(classname);}

	@Override
	public Collection<IItem> GetList(String classname, ListOptions options)	throws IonException {return GetList(classname);}

	@Override
	public Collection<IItem> GetList(IItem dummy) throws IonException {return GetList(dummy.getClassName());}

	@Override
	public Collection<IItem> GetList(IItem dummy, ListOptions options)	throws IonException {return GetList(dummy);}

	@Override
	public Iterator<IItem> GetIterator(String classname) throws IonException {return GetList(classname).iterator();}

	@Override
	public Iterator<IItem> GetIterator(String classname, ListOptions options) throws IonException {return GetIterator(classname);}

	@Override
	public Iterator<IItem> GetIterator(IItem dummy) throws IonException {return GetList(dummy).iterator();}

	@Override
	public Iterator<IItem> GetIterator(IItem dummy, ListOptions options) throws IonException {return GetIterator(dummy);}

	@Override
	public IItem GetItem(String classname, String id) throws IonException {
		for(IItem i: data.get(classname)){
			if(i.getItemId().equals(id))
				return i;
		}
		return null;
	}

	@Override
	public IItem GetItem(Object dummy) throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IItem GetItem(String classname) throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IItem CreateItem(String classname, Map<String, Object> data)	throws IonException {		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IItem SaveItem(IItem item) throws IonException {data.get(item).add(item); return item;}

	@Override
	public IItem EditItem(String classname, String id, Map<String, Object> data) throws IonException {
		IItem i = GetItem(classname, id);
		for(Entry<String, Object> e: data.entrySet()){
			i.Set(e.getKey(), e.getValue());
		}
		return i;
	}

	@Override
	public boolean DeleteItem(String classname, String id) throws IonException {return data.get(classname).remove(GetItem(classname, id));}

	@Override
	public IItem CreateItem(String classname, Map<String, Object> data,	IChangeLogger changeLogger) throws IonException {return CreateItem(classname, data);}

	@Override
	public IItem SaveItem(IItem item, IChangeLogger changeLogger) throws IonException {return SaveItem(item);}

	@Override
	public IItem EditItem(String classname, String id, Map<String, Object> data, IChangeLogger changeLogger) throws IonException {return EditItem(classname, id, data);}

	@Override
	public boolean DeleteItem(String classname, String id, IChangeLogger changeLogger) throws IonException {return DeleteItem(classname, id);}

	@Override
	public void setValidators(List<IInputValidator> validators) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Put(IItem master, String collection, IItem detail)
																																	 throws IonException {
		// TODO Auto-generated method stub
	}

	@Override
	public void Put(IItem master, String collection, IItem detail,
										 IChangeLogger changeLogger) throws IonException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void Eject(IItem master, String collection, IItem detail)
																																		 throws IonException {
		// TODO Auto-generated method stub
	}

	@Override
	public void Eject(IItem master, String collection, IItem detail,
											 IChangeLogger changeLogger) throws IonException {
		// TODO Auto-generated method stub
	}

	@Override
	public Collection<IItem> GetAssociationsList(IItem master, String collection,
																							 ListOptions options)
																																	 throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IItem> GetAssociationsList(IItem master, String collection)
																																							 throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<IItem> GetAssociationsIterator(IItem master,
																								 String collection,
																								 ListOptions options)
																																		 throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<IItem> GetAssociationsIterator(IItem master, String collection)
																																								 throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long GetAssociationsCount(IItem master, String collection,
																	ListOptions options) throws IonException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long GetAssociationsCount(IItem master, String collection)
																																	throws IonException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<IItem> FetchList(String query,
																		 Map<String, Object> parameters)
																																		throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<IItem> FetchIterator(String query,
																			 Map<String, Object> parameters)
																																			throws IonException {
		// TODO Auto-generated method stub
		return null;
	}

}
