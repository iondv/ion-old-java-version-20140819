package ion.framework.changelog;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.ResultTransformer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.LinkedTreeMap;

import ion.core.IAuthContext;
import ion.core.IonException;
import ion.core.logging.ChangelogRecordType;
import ion.core.logging.IChangeLogger;
import ion.core.logging.IChangelogRecord;
import ion.framework.changelog.domain.StoredChangelogRecord;

public class IonChangelog implements IChangeLogger {
	
	public class ChangelogRecord implements IChangelogRecord {
		
		private StoredChangelogRecord record;
		
		private Map<String, Object> attrvalues;
		
		public ChangelogRecord(StoredChangelogRecord r){
			record = r;
		}

		@Override
		public ChangelogRecordType getType() {
			return ChangelogRecordType.fromString(record.type);
		}

		@Override
		public String getActor() {
			return record.actor;
		}

		@Override
		public Date getTime() {
			return record.time;
		}

		@Override
		public String getObjectClass() {
			return record.objectClass;
		}

		@Override
		public String getObjectId() {
			return record.objectId;
		}
		
		private Object processJsonElement(JsonElement obj){
			if (obj.isJsonNull())
				return null;
			else if (obj.isJsonObject()){
				Map<String, Object> result = new LinkedTreeMap<String, Object>();
				Set<Entry<String, JsonElement>> map = obj.getAsJsonObject().entrySet();
				for (Entry<String, JsonElement> entry: map)
					result.put(entry.getKey(), processJsonElement(entry.getValue()));			
				return result;
			} else if (obj.isJsonPrimitive()){
				JsonPrimitive p = obj.getAsJsonPrimitive();
				if (p.isNumber()){
					Long l = p.getAsLong();
					Double d = p.getAsDouble();
					Object r = 0;
					if (l.doubleValue() == d.doubleValue())
						r = l;
					else
						r = d;
					return r;
				} else if (p.isBoolean())
					return p.getAsBoolean();
				else if (p.isString())
					return p.getAsString();
			} else if (obj.isJsonArray()){
				List<Object> result = new LinkedList<Object>(); 
				Iterator<JsonElement> i = obj.getAsJsonArray().iterator();
				while (i.hasNext()){
					result.add(processJsonElement(i.next()));
				}
				return result.toArray();
			}
			return null;
		}		

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Object> getAttributeUpdates() {
			if (attrvalues == null){
				JsonParser parser = new JsonParser();
				attrvalues = (Map<String, Object>)processJsonElement(parser.parse(record.attrUpdates));
			}
			return attrvalues;
		}
	}
	
	public class ChangelogResultTransformer implements ResultTransformer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public ChangelogResultTransformer(){
		}

		public Object transformTuple(Object[] tuple, String[] aliases) {
			return new ChangelogRecord((StoredChangelogRecord)tuple[0]);
		}

		@SuppressWarnings("rawtypes")
		public List transformList(List collection) {
			return collection;
		}		
	}	
	
	private SessionFactory sessionFactory;
	
	private Gson serializer;
	
	private IAuthContext authContext;
	
	private boolean forceTimestamp = false;
	
	public IonChangelog(){
		serializer = new GsonBuilder().registerTypeHierarchyAdapter(Date.class, new JsonSerializer<Date>() {

			@Override
			public JsonElement serialize(Date src, Type typeOfSrc,
					JsonSerializationContext context) {
				JsonObject r = new JsonObject();
				r.add("timestamp", new JsonPrimitive(src.getTime()));
				return r;
			}
		}).serializeNulls().create();
	}
	
	public void setForceTimestamp(boolean forceTimestamp) {
		this.forceTimestamp = forceTimestamp;
	}

	public IAuthContext getAuthContext() {
		return authContext;
	}

	public void setAuthContext(IAuthContext authContext) {
		this.authContext = authContext;
	}

	@Override
	public void LogChange(String objectClass, String objectId)  throws IonException {
		try {
			StoredChangelogRecord chl = new StoredChangelogRecord(authContext.CurrentUser().getUid(), objectClass, objectId);
			if (forceTimestamp)
				chl.time = new Date();
			
			curSession().save(chl);
			curSession().flush();
		} catch (Exception e){
			throw new IonException(e);
		}		
	}

	@Override
	public void LogChange(ChangelogRecordType type,
			String objectClass, String objectId, Map<String, Object> updates) throws IonException {
		try {		
			StoredChangelogRecord chl = new StoredChangelogRecord(type.getValue(),authContext.CurrentUser().getUid(), objectClass, objectId, serializer.toJson(updates));
			if (forceTimestamp)
				chl.time = new Date();
			curSession().save(chl);
			curSession().flush();
		} catch (Exception e){
			throw new IonException(e);
		}
	}
	
	private List<IChangelogRecord> getChangesList(Date since, Date till) throws IonException {
		Query q = null;
		String hql = "from StoredChangelogRecord where time >= :s"+((till != null)?" and time < :t":"")+" order by id, time";		
		try {		
			q = curSession().createQuery(hql);
			q.setTimestamp("s", since);
			if (till != null)
				q.setTimestamp("t", till);
			q.setResultTransformer(new ChangelogResultTransformer());
			@SuppressWarnings("unchecked")
			List<IChangelogRecord> result = q.list();
			return result;
		} catch (Exception e){
			throw new IonException(e);
		}
	}

	@Override
	public Iterator<IChangelogRecord> getChanges(Date since, Date till) throws IonException {
		return getChangesList(since, till).iterator();
	}

	@Override
	public Iterator<IChangelogRecord> getChanges(Date since) throws IonException {
		return getChanges(since, null);
	}

	@Override
	public IChangelogRecord[] getChangesArray(Date since, Date till) throws IonException {
		List<IChangelogRecord> l = getChangesList(since, till);
		return l.toArray(new IChangelogRecord[l.size()]);
	}

	@Override
	public IChangelogRecord[] getChangesArray(Date since) throws IonException {
		return getChangesArray(since, null);
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	protected Session curSession(){
		return sessionFactory.getCurrentSession();
	}
/*
	@Override
	public void LogChange(ChangelogRecordType type,
												String masterClass, String masterId, String collection,
												Set<LoggedItemInfo> collectionItems)
																														throws IonException {
		if (type.equals(ChangelogRecordType.PUT) || type.equals(ChangelogRecordType.EJECT)){
  		try {		
  			HashMap<String, Object> updates = new HashMap<String, Object>();
  			updates.put(collection, collectionItems);
  			StoredChangelogRecord chl = new StoredChangelogRecord(type.getValue(),authContext.CurrentUser().getUid(), 
  			    			                                            masterClass, masterId,serializer.toJson(updates));
  			if (forceTimestamp)
  				chl.time = new Date();
  			curSession().save(chl);
  			curSession().flush();
  		} catch (Exception e){
  			throw new IonException(e);
  		}	
		} else
			throw new IonException("Inapropriate changelog type specified for log method!");
	}
*/	
}
