package ion.framework.dao.hbn;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.BooleanType;
import org.hibernate.type.ClassType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.FilterOption;
import ion.core.IAuthContext;
import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IDataRepository;
import ion.core.IInputValidator;
import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.IReferenceProperty;
import ion.core.IReferencePropertyMeta;
import ion.core.Operation;
import ion.core.OperationType;
import ion.core.Sorting;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.MetaPropertyType;
import ion.core.data.Item;
import ion.core.logging.ChangelogRecordType;
import ion.core.logging.IChangeLogger;
import ion.core.meta.ClassMeta;
import ion.core.meta.ReferencePropertyMeta;
import ion.core.meta.UserTypeMeta;
import ion.core.IMetaRepository;
import ion.framework.dao.DaoUtils;
import ion.framework.dao.IPostProcessed;
import ion.framework.dao.IPreProcessed;

public class HbnDataRepository implements IDataRepository {
	
	protected IMetaRepository metaRepository;
	
	//private IAuthContext authContext;
	
	private List<IInputValidator> validators;
	
	private SessionFactory sessionFactory;	
	
	private String packageName = "";
	
	private int maxSelectionListSize = 100;
	
	private int	collectionSizeLimit = 200;	
	
	public class HbnResultTransformer implements ResultTransformer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		HbnDataRepository _rep;
		
		public HbnResultTransformer(HbnDataRepository r) throws ClassNotFoundException{
			_rep = r;
		}

		public Object transformTuple(Object[] tuple, String[] aliases) {
			IItem r = null;
			try{
				Object tmp = tuple[0];
				if (tuple[0] instanceof HibernateProxy)
					tmp = ((HibernateProxy)tuple[0]).getHibernateLazyInitializer().getImplementation();
				r = _rep.wrapPOJO(tmp);
			} catch (Exception e){
				e.printStackTrace();
			}
			return r;
		}

		@SuppressWarnings("rawtypes")
		public List transformList(List collection) {
			return collection;
		}		
	}
	
	protected class QParam {
		public String Name;
		public Type Type;
		public Object Value;
		
		public QParam(String n, Object v, Type t){
			Name = n;
			Value = v;
			Type = t;
		}
	}
	
	protected class IonDataItem extends Item {
		public IonDataItem(String id, Object base, IStructMeta item_class,
				IDataRepository repository) {
			super(id, base, item_class, repository);
		}
				
		@Override
		public Object Get(String name) {
			Object v = super.Get(name);
			if (v instanceof HibernateProxy)
				return ((HibernateProxy) v).getHibernateLazyInitializer().getImplementation();
			return v;
		}			
	}
		
	public void setMetaRepository(IMetaRepository meta){
		metaRepository = meta;		
	}
	
	public void setSessionFactory(SessionFactory f){
		sessionFactory = f;
	}
	
	public void setDomainPackage(String packagename){
		packageName = packagename+".";
	}
	
	public String getDomainPackage(){
		return packageName.substring(0, packageName.length() - 1);
	}
	
	public int getCollectionSizeLimit() {
		return collectionSizeLimit;
	}

	public void setCollectionSizeLimit(int collectionSizeLimit) {
		this.collectionSizeLimit = collectionSizeLimit;
	}	
	
	protected Session curSession(){
		return sessionFactory.getCurrentSession();
	}
	
	protected Object idToKey(String classname, String id) throws IonException{
		if (id == null)
			return null;
		IClassMeta cm = (IClassMeta)metaRepository.Get(classname);
		
		// TODO Обработка композитных ключей
		
		switch (cm.PropertyMeta(cm.KeyProperties()[0]).Type()){
			case BOOLEAN:return (id == "1")?true:false;
			case DATETIME:return new Date(Long.parseLong(id));
			case REAL:
			case DECIMAL:return Float.parseFloat(id);
			case INT:
			case SET:return Integer.parseInt(id);
			default:return id;
		}
	}
	
	protected String keyToId(String classname, Object key) throws IonException{
		if (key == null)
			return null;
		IClassMeta cm = (IClassMeta)metaRepository.Get(classname);
		if (cm != null){
			// TODO Обработка композитных ключей
			switch (cm.PropertyMeta(cm.KeyProperties()[0]).Type()){
				case BOOLEAN:return ((Boolean)key)?"1":"0";
				case DATETIME:return new Long(((Date)key).getTime()).toString();
				default:return key.toString();
			}
		}
		return key.toString();
	}
	
	
	protected Object getInstance(String classname, String id) throws IonException{
		try {
			Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(packageName+classname);
			Object instance = curSession().load(c, (Serializable)idToKey(classname, id));
			return instance;
		} catch (Exception e){
		// TODO здесь костыль для СМЭВ-оффлайн	
			
			//throw new IonException(e);
			return null;
		}
	}
	
	protected IonDataItem wrapPOJO(Object o, String id, IStructMeta meta){
		return new IonDataItem(id, o, meta, this);
	}
	
	protected IonDataItem wrapPOJO(Object o, String id, String classname) throws IonException{
		IStructMeta cm = metaRepository.Get(classname);
		if (cm == null)
			throw new IonException("Не найден класс "+classname+"!");
		return wrapPOJO(o,(cm instanceof IClassMeta)?keyToId(classname, id):id,cm);
	} 	

	protected IonDataItem wrapPOJO(Object o, String id) throws IonException{
		return wrapPOJO(o, id, o.getClass().getSimpleName());
	} 
		
	protected IonDataItem wrapPOJO(Object o) throws IonException {
		try {
			IonDataItem r = wrapPOJO(o, 
					curSession().getIdentifier(o).toString()
			);
			return r;
		} catch (Exception e){
			throw new IonException(e);
		}
	}
/*
	public void setAuthContext(IAuthContext authContext) {
		this.authContext = authContext;
	}
*/	
	public IAuthContext getAuthContext(IAuthContext authContext) {
		return authContext;
	}		
	
	public Collection<IItem> GetList(String classname) throws IonException {
		return GetList(GetItem(classname),new ListOptions());
	}
	
	public Collection<IItem> GetList(String classname, String data) throws IonException {
		if(data != null && !data.isEmpty()){
			IStructMeta sm = metaRepository.Get(classname);
			if(sm != null) {
				Gson gs = new GsonBuilder().serializeNulls().create();
				try {
					Class<?> cl = Class.forName(packageName + classname);
					ArrayList<JsonObject> jl = gs.fromJson(data, new TypeToken<ArrayList<JsonObject>>(){}.getType());
					Collection<IItem> result = new ArrayList<IItem>();
					for(JsonObject jo:jl)
						result.add(wrapPOJO(gs.fromJson(jo, cl), null, classname));
					return result;
				} catch (JsonSyntaxException e) {
					throw new IonException("Passed string is not a JSON!");
				} catch (ClassNotFoundException e) {
					throw new IonException("Class with name "+classname+" not found!");
				}
			}
		}
		return new ArrayList<IItem>();
	}
	
	public Collection<IItem> GetList(String classname, ListOptions options) throws IonException {
		return GetList(GetItem(classname),options);
	}

	public Collection<IItem> GetList(IItem dummy) throws IonException {
		return GetList(dummy,new ListOptions());
	}
	
	public Iterator<IItem> GetIterator(String classname) throws IonException {
		return GetIterator(GetItem(classname),new ListOptions());
	}

	public Iterator<IItem> GetIterator(String classname, ListOptions options) throws IonException {
		return GetIterator(GetItem(classname),options);
	}

	public Iterator<IItem> GetIterator(IItem dummy) throws IonException {
		return GetIterator(dummy,new ListOptions());
	}
	
	protected Type typeCast(IPropertyMeta p) throws IonException{
		switch (p.Type()){
			case BOOLEAN:return new BooleanType();
			case DATETIME:return new TimestampType();
			case REAL:
			case DECIMAL:return new LongType();
			case SET:
			case INT:return new IntegerType();
			case REFERENCE:{
				IClassMeta cm = ((IReferencePropertyMeta)p).ReferencedClass();
				
			//TODO Обработка композитных ключей	
				return typeCast(cm.PropertyMeta(cm.KeyProperties()[0]));
			}
			case COLLECTION:return null;
			default:return new StringType();
		}
	}
	
	protected String conditionOperation(String suffix, Condition c){
		switch (c.Type()){
			case EMPTY:return " is null";
			case EQUAL:return " = :qp" + suffix;
			case IN:return " in (:qp" + suffix +")";
			case LESS:return " < :qp" + suffix;
			case LESS_OR_EQUAL:return " > :qp" + suffix;
			case LIKE:return " like :qp" + suffix;
			case MORE:return " > :qp" + suffix;
			case MORE_OR_EQUAL:return " >= :qp" + suffix;
			case NOT_EMPTY:return " is not null";
			case NOT_EQUAL:return " <> :qp" + suffix;
			default:return "";
		}
	}
	
	private String sortingModeType(Sorting s){
		switch (s.Mode()){
			case ASC:return "asc";
			case DESC:return "desc";
			case RANDOM:return "random";
			default:return "";
		}
	}
	
	protected Object parseConditionValue(IProperty p) throws IonException{
		if (p.getType().equals(MetaPropertyType.REFERENCE)){
			if (((IReferenceProperty)p).getReferedItem() != null)
				return ((IReferenceProperty)p).getReferedItem().getItemId();
		}
		return p.getValue();
	}	
	
	protected IPropertyMeta getKeyPM(IClassMeta cm) throws IonException{
		if (cm.getAncestor() == null){
			// TODO Обработка композитных ключей
			return cm.PropertyMeta(cm.KeyProperties()[0]);
		}
		return getKeyPM((IClassMeta)cm.getAncestor());
	}
	
	@SuppressWarnings("rawtypes")
	protected Object parseConditionValue(IPropertyMeta p, Object v) throws IonException{
		if (v instanceof Iterable || v.getClass().isArray()){
			List<Object> result = new LinkedList<Object>();
			Iterable iter = null;
			if (v.getClass().isArray())
				iter = Arrays.asList((Object[])v);
			else
				iter = (Iterable)v;
			for (Object el: iter)
				result.add(parseConditionValue(p, el));
			return result;
		}
		
		if (p == null){
			if (v instanceof Class)
				return v;
			if (v instanceof String)
				try {
					return Class.forName(packageName+(String)v);
				} catch (ClassNotFoundException e) {
					throw new IonException(e);
				}
			return v;
		}
		
		switch (p.Type()){
			case REFERENCE:{
				if (v instanceof IItem)
					return parseConditionValue(p,((IItem)v).getItemId());
				if (v instanceof String)
					return parseConditionValue(getKeyPM(((ReferencePropertyMeta)p).ReferencedClass()),v);
			}break;
			case BOOLEAN:{
				if (!(v instanceof Boolean) && !v.getClass().getSimpleName().equals("boolean"))
					return Boolean.parseBoolean(v.toString());
			}break;
			case INT:{
				if (p.Size() == null || p.Size() <= 11){
					if (!(v instanceof Integer || v.getClass().getSimpleName().equals("int")))
							return Integer.parseInt(v.toString());
				} else {
					if (!(v instanceof Long || v.getClass().getSimpleName().equals("long")))
							return Long.parseLong(v.toString());					
				}				
			}break;
			case DECIMAL:
			case REAL:{
				if (!(v instanceof BigDecimal || v instanceof Float || v instanceof Double || v.getClass().getSimpleName().equals("float")))
					return Double.parseDouble(v.toString());									
			}break;
			default:return v;
		}
		return v;
	}

	private String mappedName(String pn){
		if (pn.equals("class"))
			return "type(e)";
		return "e." + StringUtils.uncapitalize(pn);
	}
	
	protected String buildConditions(IItem dummy, ListOptions options, Collection<QParam> params) throws IonException {
		StringBuilder query = new StringBuilder("");
		Integer i = 1;
		for (Map.Entry<String,IProperty> pair: dummy.getProperties().entrySet()){
			if ((pair.getValue().getType() != MetaPropertyType.COLLECTION && pair.getValue().getType() != MetaPropertyType.STRUCT) && pair.getValue().getValue() != null){
				if (i > 1) query.append(" and ");
				query.append(mappedName(pair.getValue().getName())+" = :qp"+i.toString());
				params.add(new QParam("qp"+i.toString(),parseConditionValue(pair.getValue().Meta(),pair.getValue().getValue()),typeCast(pair.getValue().Meta())));
				i++;
			}
		}
		
		for (FilterOption c : options.Filter()){
			if (i > 1) query.append(" and ");
			processFilterOption(c,String.valueOf(i),query,dummy,params);
			i++;
		}
		return query.toString();
	}
	
	protected void processFilterOption(FilterOption fo, String paramSuffix, StringBuilder query, IItem dummy, Collection<QParam> params) throws IonException{
		if(fo instanceof Condition){
			Condition cnd = (Condition) fo;
			if ((cnd.Property() != null) && !(cnd.Property().trim().isEmpty())){
				query.append(mappedName(cnd.Property()) + conditionOperation(paramSuffix, cnd));
				Object v;
				Type dataType;
				if (cnd.Property().equals("class")){
					v = parseConditionValue(null, cnd.Value());
					dataType = new ClassType();
				} else { 
					//IPropertyMeta pm = dummy.Property(cnd.Property()).Meta();
					//Object oo = cnd.Value();
					v = parseConditionValue(dummy.Property(cnd.Property()).Meta(), cnd.Value());
					dataType = typeCast(dummy.Property(cnd.Property()).Meta());
				}
					
				params.add(new QParam("qp"+paramSuffix, v, dataType));
			}
		}else if(fo instanceof Operation){
			Operation op = (Operation) fo;
			String oper = OperationType.getString(op.getType().getValue());
			if (op.getType().equals(OperationType.NOT)){
				query.append("not ( ");
				oper = "and";
			} else {
				query.append("( ");
			}
			
			int ii = 1;
			for(FilterOption f: op.getOperands()){
				if (ii > 1) query.append(" "+oper+" ");
				processFilterOption(f,paramSuffix+"_"+ii,query,dummy,params);
				ii++;
			}
			query.append(" )");
		}
	}
	
	protected String buildSorting(IItem dummy, ListOptions options){
		String sorting = "";
		int i = 1;
		for (Sorting s : options.Sorting()){
			if(i > 1) sorting +=", ";
			if ((s.Property() != null) && !(s.Property().trim().isEmpty())){
				sorting += mappedName(s.Property())+" "+sortingModeType(s);
			}
			i++;
		}		
		return sorting;
	}
		
	private String buildQuery(IItem dummy, ListOptions options,Collection<QParam> params) throws IonException{
		String hql = "from "+dummy.getClassName() + " e";
		String query = buildConditions(dummy, options, params);
		//String sorting = buildSorting(dummy, options);
		
		/*
		Map<String, Integer> rp = metaRepository.Get(dummy.getClassName()).RolePermissions();
		if (!rp.isEmpty()){
			String role_filter = "";
			for (Map.Entry<String, Integer> rpp: rp.entrySet()){
				if (DACPermission.parseInt(rpp.getValue()).contains(DACPermission.READ)){
					if (role_filter.isEmpty())
						role_filter += query.isEmpty()?"":" and (";
					else
						role_filter += " or ";
					role_filter += mappedName(rpp.getKey()) + "= :user";
				}
			}
			
			if (!role_filter.isEmpty()){
				query += role_filter+(query.isEmpty()?"":")");
				params.add(new QParam("user",authContext.CurrentUser().UID(),new StringType()));
			}
		}*/
		
		if (!query.isEmpty())
			hql += " where "+query;
		
		//if (sorting != "") hql += " order by "+sorting;
		return hql;
		
	}
	
	@SuppressWarnings("unchecked")
	protected void setupParams(Query q, Collection<QParam> params){
		for (QParam p: params){
			if (p.Value instanceof Collection){
				q.setParameterList(p.Name, (Collection<Object>)p.Value);
			} else {	
				q.setParameter(p.Name, p.Value,p.Type);
			}
		}		
	}

	@SuppressWarnings("unchecked")
	public Collection<IItem> GetList(IItem dummy, ListOptions options) throws IonException {
		try {		
			Query q = null;
			Query q1 = null;
			Collection<QParam> params = new ArrayList<QParam>();
			
			String hql = buildQuery(dummy, options, params);
			String sorting = buildSorting(dummy, options);
			q = curSession().createQuery(hql + ((!sorting.isEmpty())?" order by " + sorting:""));
			
			if (options.PageSize() != null){
				if (options.TotalCount() == null) 
					q1 = curSession().createQuery("select count(*) "+hql);
				
				q.setMaxResults(options.PageSize());
				if (options.Page() != null)
					q.setFirstResult((options.Page()-1)*options.PageSize());
			}		
				
			setupParams(q, params);
			q.setResultTransformer(new HbnResultTransformer(this));
			Collection<IItem> result = q.list();

			if (q1 != null){
				setupParams(q1, params);
				options.SetTotalCount((Long)q1.uniqueResult());
			} else if (options.PageSize() == null) {
				options.SetTotalCount((long)result.size());
			}
			return result;
		} catch (ClassNotFoundException e){
			throw new IonException(e);
		}
	}
	
	public Iterator<IItem> GetIterator(IItem dummy, ListOptions options) throws IonException {
		return GetList(dummy,options).iterator();
	}	

	public IItem GetItem(String classname, String id) throws IonException {
		IStructMeta cm = metaRepository.Get(classname);
		if (cm != null){
			if (cm instanceof IClassMeta){
				Object o = null;
				o = getInstance(classname, id);
				if (o != null)
					return wrapPOJO(o, id, classname);
			} else {
				Gson gs = new GsonBuilder().serializeNulls().create();
				try {
					Object base = null;
					if (id == null || id.isEmpty())
						base = Thread.currentThread().getContextClassLoader().loadClass(packageName + classname).newInstance();
					else
						base = gs.fromJson(id, Thread.currentThread().getContextClassLoader().loadClass(packageName + classname));
					return wrapPOJO(base, "", classname);
				} catch (JsonSyntaxException e) {
					throw new IonException("Passed string is not a JSON!");
				} catch (ClassNotFoundException e) {
					throw new IonException("Class with name "+classname+" not found!");
				} catch (InstantiationException e) {
					throw new IonException("Class with name "+classname+" can not be instantiated!");
				} catch (IllegalAccessException e) {
					throw new IonException("Class with name "+classname+" can not be instantiated!");
				}								
			}
		}
		return null;
	}
	
	public IItem GetItem(Object dummy) throws IonException{
		String cn = Hibernate.getClass(dummy).getSimpleName();
		IStructMeta sm = metaRepository.Get(cn);
		if (!(sm instanceof ClassMeta) && sm != null)
			return wrapPOJO(dummy, "", sm.getName());	
		
		try {		
			String id = curSession().getIdentifier(dummy).toString();
			if (id != null)
				return GetItem(dummy.getClass().getSimpleName(),id);
			
			Iterator<IItem> result = GetIterator(wrapPOJO(dummy));
			IItem r = null;
			while (result.hasNext()){
				r = result.next();
				break;
			}			
			return r;
		} catch (Exception e){
			throw new IonException(e);
		}
	}	

	public IItem GetItem(String classname) throws IonException {
		Object o = null;
		IItem i = null;
		try {
			o = Thread.currentThread().getContextClassLoader().loadClass(packageName+classname).newInstance();
			if (o != null)
				i = wrapPOJO(o, null, classname);
		} catch (Exception e) {
			throw new IonException(e);	
		}
		return i;
	}
	
	private Object parseValue(IPropertyMeta p, Object value) throws IonException{
		if (value != null && p.Type().equals(MetaPropertyType.REFERENCE)){
			Object obj = null;
			if (value instanceof IonDataItem)
				obj = ((IonDataItem) value).Base();
			else {
				if (value instanceof IItem)
					obj = getInstance(((IItem)value).getClassName(),((IItem)value).getItemId());
				else 
					obj = getInstance(((ReferencePropertyMeta)p).ReferencedClass().getName(), value.toString());
			}
			
			if (obj == null && !p.Nullable())
				throw new IonException("Неверное значение передано в ссылочный атрибут " + p.Caption() + "!");
			return obj;
		}
		
		if (value instanceof String){
			switch (p.Type()){
				case CUSTOM:value = DaoUtils.cast((String)value,((UserTypeMeta)p).getBaseType());break;
				default:value = DaoUtils.cast((String)value,p.Type());break;
			}
		}
		
		if(p.Type().equals(MetaPropertyType.COLLECTION)){
			Gson gs = new GsonBuilder().serializeNulls().create();
			if(value instanceof IItem[]){
				int count = ((IItem[])value).length;
				Object[] results = new Object[count];
				for(int i = 0; i < count; i++)
					results[i] = ((Item)Array.get(value, i)).Base();
				String ret = gs.toJson(results);
				return ret;
			}				
		}
		return value;
	}
	
	private  Object parseCreationValue(IPropertyMeta p, Object value) throws IonException{
		if (value == null){
			if (p.AutoAssigned() && p.Type().equals(MetaPropertyType.DATETIME)){
				return new Date();
			}
			if (p.AutoAssigned() && p.Type().equals(MetaPropertyType.GUID)){
				UUID guid = UUID.randomUUID();
				return guid.toString();
			}
		}
		return parseValue(p,value);
	}
	
	protected ChangelogRecordType getChangelogRecordType(ChangelogRecordType action, IItem item, Map<String, Object> updates){
		return action;
	}

	public IItem CreateItem(String classname, Map<String, Object> data, IChangeLogger changeLogger) throws IonException {
		try {
			IonDataItem i = (IonDataItem)GetItem(classname);
			processValidators(i,data);
			Map<String, Object> updates = new HashMap<String, Object>();
			// TODO: Zдесь не пишется структура в новый объект, т.к. индексы входящих
			// данных (через точку, вложенность) не соответствуют однозначно имени
			// атрибута структуры.
			for (Map.Entry<String, IProperty> p: i.getProperties().entrySet())
				if (data.containsKey(p.getKey())){
					IPropertyMeta pm = p.getValue().Meta();
					Object v = parseCreationValue(pm, data.get(p.getKey()));
					i.Set(p.getKey(), v);
					if (data.containsKey(p.getKey()) && v != null){
						if(pm.Type() == MetaPropertyType.REFERENCE)
							updates.put(p.getKey(), GetItem(v).getItemId());
						else
							updates.put(p.getKey(), v);
					}
				}
			
			if(i.getMetaClass() instanceof IClassMeta){
				IClassMeta cm = (IClassMeta) i.getMetaClass();
				// TODO Обработка композитных ключей
				IPropertyMeta pm = cm.PropertyMeta(cm.KeyProperties()[0]);
				if (pm.Type() == MetaPropertyType.GUID && pm.AutoAssigned()){
					if (i.Get(cm.KeyProperties()[0]) == null){
						i.Set(cm.KeyProperties()[0], UUID.randomUUID().toString());
					}
				}
				
				if(cm.CreationTracker()!=null && !cm.CreationTracker().trim().isEmpty()){
					Date d = new Date();
					i.Set(cm.CreationTracker(), d);
					updates.put(cm.CreationTracker(), d);
				}
			} else
				return i;
			
			Boolean save = true;
			if (i.Base() instanceof IPreProcessed)
				save = ((IPreProcessed)i.Base()).BeforePersist();
			if (save){
				curSession().save(i.Base());
				curSession().flush();
				if (i.Base() instanceof IPostProcessed)
					((IPostProcessed)i.Base()).AfterPersist();
			}
			IItem r = wrapPOJO(i.Base()); 
			if (changeLogger != null)
				changeLogger.LogChange(getChangelogRecordType(ChangelogRecordType.CREATION, r, updates), classname, r.getItemId(),updates);
			
			return r;
		} catch (Exception e){
			throw new IonException(e);
		}
	}
	
	private void trackUpdate(IItem i, Map<String, Object> updates){
		if (i.getMetaClass() instanceof IClassMeta){
			String tracker = ((IClassMeta)i.getMetaClass()).ChangeTracker();
			if(tracker != null && !tracker.trim().isEmpty()){
				Date d = new Date();
				i.Set(tracker, d);
				updates.put(tracker, d);
			}		
		}
	}

	public IItem SaveItem(IItem item, IChangeLogger changeLogger) throws IonException {
		try {
			Object o = null;
			Item oi = null;
			Map<String, Object> updates = new HashMap<String, Object>();
			
			if (changeLogger != null || !(item instanceof Item)){
				if (item.getItemId() != null)
					o = getInstance(item.getClassName(), item.getItemId());
				
				if (o == null)
					oi = (Item)GetItem(item.getClassName());
				else
					oi = wrapPOJO(o);
					
					
				for (Map.Entry<String,IProperty> pair: item.getProperties().entrySet()) {
					IPropertyMeta pm = item.Property(pair.getKey()).Meta();
				   Object v = parseValue(pm, pair.getValue().getValue());
				    if (o == null || v != oi.Get(pair.getKey())){
				    	if(pm.Type() == MetaPropertyType.REFERENCE)
				    		updates.put(pair.getKey().toString(), GetItem(v).getItemId());
				    	else
				        updates.put(pair.getKey().toString(), v.toString());
				    }
				    oi.Set(pair.getKey(),v);
				}	
			} else oi = (Item)item;
			
			trackUpdate(oi, updates);
			
		    curSession().saveOrUpdate(oi.Base());
		    curSession().flush();
		    
			if (changeLogger != null && updates.size() > 0){
				changeLogger.LogChange(getChangelogRecordType(ChangelogRecordType.UPDATE, oi, updates), item.getClassName(), item.getItemId(), updates);
			}
			
			return item;
		} catch (Exception e){
			throw new IonException(e);
		}			
	}

	public IItem EditItem(String classname, String id, Map<String, Object> data, IChangeLogger changeLogger) throws IonException {
		Map<String, Object> updates = new HashMap<String, Object>();
		try {
			IonDataItem i = (IonDataItem)GetItem(classname, id);
			processValidators(i,data);
			if (i != null && !data.isEmpty()){
				for (Map.Entry<String,Object> pair: data.entrySet()) {
					IPropertyMeta pm = i.Property(pair.getKey()).Meta();
					Object v = parseValue(pm, pair.getValue());
					i.Set(pair.getKey(), v);
					if(pm.Type() == MetaPropertyType.REFERENCE)
						updates.put(pair.getKey(), GetItem(v).getItemId());
					else
						updates.put(pair.getKey(), v);
				}
				trackUpdate(i, updates);
				
				Boolean save = true;
				if (i.Base() instanceof IPreProcessed)
					save = ((IPreProcessed)i.Base()).BeforeUpdate();
				if (save){
					curSession().update(i.Base());
					if (i.Base() instanceof IPostProcessed)
						((IPostProcessed)i.Base()).AfterUpdate();
					curSession().flush();
					if (changeLogger != null)
						changeLogger.LogChange(getChangelogRecordType(ChangelogRecordType.UPDATE, i, updates), i.getClassName(), i.getItemId(), updates);
				}		
			}			
			return i;
		} catch (Exception e){
			throw new IonException(e);
		}
	}

	public boolean DeleteItem(String classname, String id, IChangeLogger changeLogger) throws IonException{
		try {
			Object o = getInstance(classname, id);
			Boolean delete = true;
			if (o instanceof IPreProcessed)
				delete = ((IPreProcessed)o).BeforeDelete();
			if (delete){
				curSession().delete(o);
				if (o instanceof IPostProcessed)
					((IPostProcessed)o).AfterDelete();
				if (changeLogger != null)
					changeLogger.LogChange(classname, id);
				curSession().flush();
			}
			return true;
		} catch (Exception e){
			throw new IonException(e);
		}				
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<IItem> FetchList(String query, Map<String, Object> parameters) throws IonException {
		if (query != null && !query.isEmpty())
		try {				
			Query q = curSession().createQuery(query);
			for (Map.Entry<String, Object> pair: parameters.entrySet()){
				q.setParameter(pair.getKey(), pair.getValue());
			}
			
			q.setResultTransformer(new HbnResultTransformer(this));
			q.setMaxResults(maxSelectionListSize);
			
			return q.list();
		} catch (Exception e){
			throw new IonException(e);
		}			
		return null;
	}

	public long GetCount(String classname) throws IonException {
		return GetCount(GetItem(classname));
	}

	public long GetCount(String classname, ListOptions options)
			throws IonException {
		return GetCount(GetItem(classname),options);
	}

	public long GetCount(IItem dummy) throws IonException {
		return GetCount(dummy,new ListOptions());
	}

	public long GetCount(IItem dummy, ListOptions options) throws IonException {
		try {		
			Query q = null;
			Collection<QParam> params = new ArrayList<QParam>();
			String hql = buildQuery(dummy, options, params);		
			q = curSession().createQuery("select count(*) "+hql);
			setupParams(q, params);
			long r = ((Long)q.uniqueResult()).longValue();
			return r;
		} catch (Exception e){
			throw new IonException(e);
		}			
	}

	@Override
	public IItem CreateItem(String classname, Map<String, Object> data)
			throws IonException {
		return CreateItem(classname, data, null);
	}

	@Override
	public IItem SaveItem(IItem item) throws IonException {
		return SaveItem(item, null);
	}

	@Override
	public IItem EditItem(String classname, String id, Map<String, Object> data)
			throws IonException {
		return EditItem(classname, id, data, null);
	}

	@Override
	public boolean DeleteItem(String classname, String id) throws IonException {
		return DeleteItem(classname, id, null);
	}

	@Override
	public void setValidators(List<IInputValidator> validators) {
		this.validators = validators;
	}	
	
	private String generateExceptionMessage(Map<String,String> messages){
		String result = "";
		int count = 0;
		for(Entry<String,String> message : messages.entrySet()){
			if(count>0) result +=", ";
			result += message.getKey()+": "+message.getValue();
			count++;
		}
		return result;
	}
	
	private void processValidators(IItem i, Map<String, Object> data) throws IonException{
		if (validators != null){
			Map<String,String> validationExceptions = new HashMap<String, String>();
			for(IInputValidator validator :validators){
				if(validator.validate(i, data)!=null){
					validationExceptions.putAll(validator.validate(i, data));
				};
			}
			if(validationExceptions.size()>0){
				String message = generateExceptionMessage(validationExceptions);
				throw new IonException(message);
			}
		}
	}

	public int getMaxSelectionListSize() {
		return maxSelectionListSize;
	}

	public void setMaxSelectionListSize(int maxSelectionListSize) {
		this.maxSelectionListSize = maxSelectionListSize;
	}
	
	public void Put(IItem master, String collection, IItem detail) throws IonException {
		Put(master, collection, detail, null);
	}
	
	@SuppressWarnings("unchecked")
	public void Put(IItem master, String collection, IItem detail, IChangeLogger changeLogger) throws IonException {
		IPropertyMeta pm = master.getMetaClass().PropertyMeta(collection);
		if(pm instanceof ICollectionPropertyMeta){
			String br = ((ICollectionPropertyMeta)pm).BackReference();
			if(br != null && !br.isEmpty()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put(br, master);
				EditItem(((ICollectionPropertyMeta)pm).ItemsClass().getName(), detail.getItemId(), data, changeLogger);
			} else if ((master instanceof Item) && (detail instanceof Item)){
				((Collection<Object>)master.Get(collection)).add(((Item)detail).Base());
				curSession().update(((Item)master).Base());
				curSession().flush();
				if (changeLogger != null){
					Map<String, Object> updates = new HashMap<String, Object>();
					updates.put(collection, ((Item)detail).Base());
					changeLogger.LogChange(getChangelogRecordType(ChangelogRecordType.PUT, master, updates), master.getClassName(), master.getItemId(), updates);
				}
			} else
				throw new IonException("Putting item to collection failed!");
		} else 
			throw new IonException("Invalid name of collection specified!");
	}

	@Override
	public void Eject(IItem master, String collection, IItem detail)
																																	throws IonException {
		Eject(master, collection, detail);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void Eject(IItem master, String collection, IItem detail,
										IChangeLogger changeLogger) throws IonException {
		IPropertyMeta pm = master.getMetaClass().PropertyMeta(collection);
		if(pm instanceof ICollectionPropertyMeta){
			String br = ((ICollectionPropertyMeta)pm).BackReference();
			if(br != null && !br.isEmpty()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put(br, null);
				EditItem(((ICollectionPropertyMeta)pm).ItemsClass().getName(), detail.getItemId(), data, changeLogger);
			} else if ((master instanceof Item) && (detail instanceof Item)){
				if (((Collection<Object>)master.Get(collection)).contains(((Item)detail).Base())){
					((Collection<Object>)master.Get(collection)).remove(((Item)detail).Base());
  				curSession().update(((Item)master).Base());
  				curSession().flush();
  				if (changeLogger != null){
  					Map<String, Object> updates = new HashMap<String, Object>();
  					updates.put(collection, ((Item)detail).Base());
  					changeLogger.LogChange(getChangelogRecordType(ChangelogRecordType.EJECT, master, updates), master.getClassName(), master.getItemId(), updates);
  				}
				}
			} else
				throw new IonException("Ejection item from collection failed!");
		} else 
			throw new IonException("Invalid name of collection specified!");		
	}
	
	/*
	 * private Collection<FilterOption> getConditions(ICollectionPropertyMeta
	 * m){ Collection<FilterOption> conditions = new ArrayList<FilterOption>();
	 * try { String binding = m.Binding(); if ((binding == null ||
	 * binding.isEmpty()) && container.getMetaClass() instanceof IClassMeta)
	 * binding = ((IClassMeta)container.getMetaClass()).KeyProperty();
	 * 
	 * Object bindingValue = container.Get(binding); if (bindingValue == null)
	 * return null; conditions.add(new Condition(m.BackReference(),
	 * ConditionType.EQUAL,bindingValue)); } catch (Exception e) { return null;
	 * } return conditions; }
	 */
	
	private Collection<IItem> initStructCollection(String className, String data) throws IonException {
		return GetList(className, data);
	}

	private Collection<IItem> initAnnotatedCollection(Collection<Object> coll,
																										Integer lim)
																																throws IonException {
		Collection<IItem> result = new ArrayList<IItem>();
		for (Object i : coll) {
			if (lim == null || result.size() < lim)
				result.add(GetItem(i));
			else
				break;
		}
		return result;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IItem> GetAssociationsList(IItem master, String collection,
																							 ListOptions options)
																																	 throws IonException {
		ICollectionPropertyMeta m = (ICollectionPropertyMeta) master.getMetaClass().PropertyMeta(collection);
		if (m.ItemsClass() instanceof IClassMeta) {
			Object p = ((IonDataItem)master).Get(collection);
			if (p != null && p instanceof Collection<?>) {
				return initAnnotatedCollection((Collection<Object>) p,
																												collectionSizeLimit);
			} else {
				if (m.BackReference() != null && !m.BackReference().isEmpty()){
					try {
						options.Filter().add(new Condition(m.BackReference(), ConditionType.EQUAL, master.getItemId()));
						return GetList(m.ItemsClass().getName(), options);
					} catch (Exception e){
						throw new IonException(e);
					}
				}
			}
		} else {
			return initStructCollection(m.ItemsClass().getName(), Item.GetObjectProperty(((Item)master).Base(), collection).toString());
		}
		return null;
	}

	@Override
	public Collection<IItem> GetAssociationsList(IItem master, String collection)
																																							 throws IonException {
		return GetAssociationsList(master, collection, new ListOptions());
	}

	@Override
	public Iterator<IItem> GetAssociationsIterator(IItem master,
																								 String collection,
																								 ListOptions options)
																																		 throws IonException {
		return GetAssociationsList(master, collection, options).iterator();
	}

	@Override
	public Iterator<IItem> GetAssociationsIterator(IItem master, String collection)
																																								 throws IonException {
		return GetAssociationsIterator(master, collection, new ListOptions());
	}

	@SuppressWarnings("unchecked")
	@Override
	public long GetAssociationsCount(IItem master, String collection,
																	ListOptions options) throws IonException {
		ICollectionPropertyMeta m = (ICollectionPropertyMeta) master.getMetaClass().PropertyMeta(collection);
		if (m.ItemsClass() instanceof IClassMeta) {
			Object p = ((IonDataItem)master).Get(collection);
			if (p != null && p instanceof Collection<?>) {
				return ((Collection<Object>) p).size();
			} else {
				if (m.BackReference() != null && !m.BackReference().isEmpty()){
					try {
						options.Filter().add(new Condition(m.BackReference(), ConditionType.EQUAL, master.getItemId()));
						return GetCount(m.ItemsClass().getName(), options);
					} catch (Exception e){
						throw new IonException(e);
					}
				}
			}
		} else {
			Collection<IItem> tmp = initStructCollection(m.ItemsClass().getName(), Item.GetObjectProperty(((Item)master).Base(), collection).toString());
			return tmp.size();
		}
		return 0;
	}

	@Override
	public long GetAssociationsCount(IItem master, String collection)
																																	throws IonException {
		return GetAssociationsCount(master, collection, new ListOptions());
	}

	@Override
	public Iterator<IItem> FetchIterator(String query,
																			 Map<String, Object> parameters)
																																			throws IonException {
		return FetchList(query, parameters).iterator();
	}
}
