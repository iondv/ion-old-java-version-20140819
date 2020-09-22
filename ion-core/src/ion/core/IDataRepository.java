package ion.core;

import ion.core.logging.IChangeLogger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface IDataRepository {
	
	void setValidators(List<IInputValidator> validators);

	long GetCount(String classname) throws IonException;

	long GetCount(String classname, ListOptions options) throws IonException;

	long GetCount(IItem dummy) throws IonException;
	
	long GetCount(IItem dummy, ListOptions options) throws IonException;
		
	Collection<IItem> GetList(String classname) throws IonException;
	
	Collection<IItem> GetList(String classname, ListOptions options) throws IonException;

	Collection<IItem> GetList(IItem dummy) throws IonException;
	
	Collection<IItem> GetList(IItem dummy, ListOptions options) throws IonException;

	Iterator<IItem> GetIterator(String classname) throws IonException;

	Iterator<IItem> GetIterator(String classname, ListOptions options) throws IonException;

	Iterator<IItem> GetIterator(IItem dummy) throws IonException;
	
	Iterator<IItem> GetIterator(IItem dummy, ListOptions options) throws IonException;	
	
	IItem GetItem(String classname, String id) throws IonException;
	
	IItem GetItem(Object dummy) throws IonException;
	
	IItem GetItem(String classname) throws IonException;
	
	IItem CreateItem(String classname, Map<String, Object> data) throws IonException;

	IItem SaveItem(IItem item) throws IonException;
		
	IItem EditItem(String classname, String id, Map<String, Object> data) throws IonException;
	
	boolean DeleteItem(String classname, String id) throws IonException;
	
	IItem CreateItem(String classname, Map<String, Object> data, IChangeLogger changeLogger) throws IonException;

	IItem SaveItem(IItem item, IChangeLogger changeLogger) throws IonException;
		
	IItem EditItem(String classname, String id, Map<String, Object> data, IChangeLogger changeLogger) throws IonException;
	
	boolean DeleteItem(String classname, String id, IChangeLogger changeLogger) throws IonException;
	
	void Put(IItem master, String collection, IItem detail) throws IonException;

	void Put(IItem master, String collection, IItem detail, IChangeLogger changeLogger) throws IonException;
	
	void Eject(IItem master, String collection, IItem detail) throws IonException;

	void Eject(IItem master, String collection, IItem detail, IChangeLogger changeLogger) throws IonException;
	
	Collection<IItem> GetAssociationsList(IItem master, String collection, ListOptions options) throws IonException;
	
	Collection<IItem> GetAssociationsList(IItem master, String collection) throws IonException;
	
	Iterator<IItem> GetAssociationsIterator(IItem master, String collection, ListOptions options) throws IonException;
	
	Iterator<IItem> GetAssociationsIterator(IItem master, String collection) throws IonException;
	
	long GetAssociationsCount(IItem master, String collection, ListOptions options) throws IonException;
	
	long GetAssociationsCount(IItem master, String collection) throws IonException;
	
	Collection<IItem> FetchList(String query, Map<String, Object> parameters) throws IonException;
	
	Iterator<IItem> FetchIterator(String query, Map<String, Object> parameters) throws IonException;
}
