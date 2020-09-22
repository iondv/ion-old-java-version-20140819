package ion.framework.dao.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.internal.LinkedTreeMap;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.FilterOption;
import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IDataRepository;
import ion.core.IInputValidator;
import ion.core.IItem;
import ion.core.IMetaRepository;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.IReferenceProperty;
import ion.core.IReferencePropertyMeta;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.MetaPropertyType;
import ion.core.Operation;
import ion.core.OperationType;
import ion.core.Sorting;
import ion.core.data.Item;
import ion.core.logging.ChangelogRecordType;
import ion.core.logging.IChangeLogger;
import ion.core.logging.ILogger;
import ion.core.logging.LoggedItemInfo;
import ion.core.meta.CollectionPropertyMeta;
import ion.core.meta.PropertyMeta;
import ion.core.meta.UserTypeMeta;
import ion.framework.dao.DaoUtils;
import ion.framework.dao.jdbc.antlr.SelectionBaseListener;
import ion.framework.dao.jdbc.antlr.SelectionLexer;
import ion.framework.dao.jdbc.antlr.SelectionParser;
import ion.framework.dao.jdbc.antlr.SelectionParser.ConditionsContext;
import ion.framework.dao.jdbc.antlr.SelectionParser.FromClauseContext;
import ion.framework.dao.jdbc.antlr.SelectionParser.QueryContext;
import ion.framework.dao.jdbc.antlr.SelectionParser.SelectAttributeContext;
import ion.framework.dao.jdbc.antlr.SelectionParser.SelectClauseContext;
import ion.framework.dao.jdbc.antlr.SelectionParser.StatementContext;
import ion.framework.dao.jdbc.antlr.SelectionParser.ValuesContext;
import ion.framework.dao.jdbc.antlr.SelectionParser.WhereClauseContext;
import ion.util.sync.SyncUtils;

public class JdbcDataRepository implements IDataRepository {

	//private DataSource dataSource;
	
	protected IJdbcConnectionProvider connectionProvider;
	
	protected IMetaRepository metaRepository;
	protected List<IInputValidator> validators;
	protected ILogger logger;

	protected String tablePrefix = "t";
	protected String fieldPrefix = "f";
	protected String structSeparator = "$";
	
	protected boolean forceCascadeDeletions = false;
	protected boolean showSql = false;

	protected Set<String> cacheClasses;
	protected Set<String> nonCacheClasses;
	protected Long cacheExpireTime;
	protected Integer cacheLimit;
	protected Map<String, LinkedHashMap<String, CachedItem>> cache = new HashMap<String, LinkedHashMap<String, CachedItem>>();

	public class CachedItem {
		IItem item;
		Date insertTime;

		public IItem getItem() {
			return item;
		}

		public void setItem(IItem item) {
			this.item = item;
		}

		public Date getInsertTime() {
			return insertTime;
		}

		public void setInsertTime(Date insertTime) {
			this.insertTime = insertTime;
		}

		public CachedItem(IItem item, Date insertTime) {
			this.item = item;
			this.insertTime = insertTime;
		}
	}
	
	protected class ParamValue {
		public String property;
		
		public Object value;
		
		public ParamValue(String property, Object value){
			this.property = property;
			this.value = value;
		}
	}
	
	public JdbcDataRepository(IJdbcConnectionProvider connectionProvider){
		this.connectionProvider = connectionProvider;
	}

	private void putIntoCache(IItem item) throws IonException {
		if (isCachable(item.getClassName())) {
			if (cache.containsKey(item.getClassName())) {
				checkReachedCacheLimit(item.getClassName());
				cache.get(item.getClassName())
						 .put(item.getItemId(), new CachedItem(item, getInsertTime()));
			} else {
				LinkedHashMap<String, CachedItem> m = new LinkedHashMap<String, JdbcDataRepository.CachedItem>();
				m.put(item.getItemId(), new CachedItem(item, getInsertTime()));
				cache.put(item.getClassName(), m);
			}
		}
	}

	private Date getInsertTime() {
		if (cacheExpireTime != null) {
			return new Date();
		}
		return null;
	}

	private boolean isCachable(String classname) throws IonException {
		IStructMeta actualSm = metaRepository.Get(classname);
		IStructMeta currentSm = actualSm;
		while (currentSm != null) {
			if (cacheClasses != null) {
				if (cacheClasses.contains(currentSm.getName()))
					return true;
			}
			if (nonCacheClasses != null) {
				if (nonCacheClasses.contains(currentSm.getName()))
					return false;
			}
			currentSm = currentSm.getAncestor();
		}
		return false;
	}

	private void checkReachedCacheLimit(String classname) {
		if (cacheExpireTime == null && cacheLimit != null) {
			if (cache.get(classname).size() >= cacheLimit) {
				cache.get(classname).remove(cache.get(classname).entrySet().iterator()
																				 .next().getKey());
			}
		}
	}

	private IItem getFromCache(String classname, String id) {
		if (isCached(classname, id)) {
			return cache.get(classname).get(id).getItem();
		}
		return null;
	}

	private boolean isCached(String classname, String id) {
		if (cache.containsKey(classname)) {
			if (cache.get(classname).containsKey(id)) {
				if (cacheExpireTime != null) {
					Date insertTime = cache.get(classname).get(id).getInsertTime();
					if (insertTime.getTime() + cacheExpireTime > new Date().getTime()) {
						return true;
					} else {
						cache.get(classname).remove(id);
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}

	private void removeFromCache(String classname, String id) {
		if (cache.containsKey(classname))
			if (cache.get(classname).containsKey(id))
				cache.get(classname).remove(id);
	}

	public void setMetaRepository(IMetaRepository metaRepository) {
		this.metaRepository = metaRepository;
	}
/*
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
*/
	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public void setStructSeparator(String structSeparator) {
		this.structSeparator = structSeparator;
	}

	public void setFieldPrefix(String fieldPrefix) {
		this.fieldPrefix = fieldPrefix;
	}

	public void setCacheClasses(Set<String> cacheClasses) {
		this.cacheClasses = cacheClasses;
	}

	public void setNonCacheClasses(Set<String> nonCacheClasses) {
		this.nonCacheClasses = nonCacheClasses;
	}

	public void setCacheExpireTime(Long cacheExpireTime) {
		this.cacheExpireTime = cacheExpireTime;
	}

	public void setCacheLimit(Integer cacheLimit) {
		this.cacheLimit = cacheLimit;
	}

	public void setForceCascadeDeletions(boolean forceCascadeDeletions) {
		this.forceCascadeDeletions = forceCascadeDeletions;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	protected void showSql(String query) {
		if (showSql) {
			try {
				if (logger != null) {
					logger.Out().println(query);
					logger.FlushStream(ILogger.INFO);
				} else
					System.out.println(query);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	public String capitalizeFirstLetter(String propertyName) {
		return propertyName.substring(0, 1).toUpperCase()
				+ propertyName.substring(1);
	}
	
	protected Object getResultObject(ResultSet result, IPropertyMeta property, String columnLabel) throws SQLException, IonException {
		Object ret;
		switch (property.Type()) {
			case BOOLEAN:
				ret = result.getBoolean(columnLabel);
				break;
			case DATETIME:
				ret = result.getTimestamp(columnLabel);
				break;
			case DECIMAL:
				ret = result.getBigDecimal(columnLabel);
				break;
			case INT:
				if (property.Size() == null || property.Size() > 11) {
					ret = result.getLong(columnLabel);
				} else {
					ret = result.getInt(columnLabel);
				}
				break;
			case REAL:
				ret = result.getFloat(columnLabel);
				break;
			case SET:
				ret = result.getShort(columnLabel);
				break;
			case TEXT:
			case FILE:
			case GUID:
			case HTML:
			case IMAGE:
			case PASSWORD:
			case STRUCT:
			case URL:
			case STRING:
				ret = result.getString(columnLabel);
				break;
			case REFERENCE:
				IClassMeta cm = ((IReferencePropertyMeta) property).ReferencedClass();
				// FIXME Обработка составных ключей
				MetaPropertyType refPropType = cm.PropertyMeta(cm.KeyProperties()[0]).Type();
				Short refPropSize = cm.PropertyMeta(cm.KeyProperties()[0]).Size();
				IPropertyMeta surrogate = new PropertyMeta(property.Name(), property.Caption(), refPropType, refPropSize);
				ret = getResultObject(result, surrogate, columnLabel);
				break;
			case COLLECTION:
					return null;
			default:
				ret = result.getObject(columnLabel);
		}
		if(result.wasNull())
			ret = null;
		return ret;
	}
	
	protected Object getResultObject(IStructPropertyMeta struct, ResultSet result, IPropertyMeta property)
			throws SQLException, IonException {
		String columnLabel = property.Name();
		if (!columnLabel.equals("_type")){
			if (struct != null)
				columnLabel = SyncUtils.dbSanitiseName(struct.Name()+structSeparator+property.Name(), fieldPrefix);
			else	
				columnLabel = SyncUtils.dbSanitiseName(property.Name(), fieldPrefix);
		}
		return getResultObject(result, property, columnLabel);
	}
	
	protected Object getResultObject(ResultSet result, IPropertyMeta property)
																																					throws SQLException,
																																					IonException {
		return getResultObject(null, result, property);
	}

	private void processValidators(IItem i, Map<String, Object> data)
																																	 throws IonException {
		if (validators != null) {
			Map<String, String> validationExceptions = new HashMap<String, String>();
			for (IInputValidator validator : validators) {
				if (validator.validate(i, data) != null) {
					validationExceptions.putAll(validator.validate(i, data));
				}
			}
			if (validationExceptions.size() > 0) {
				String message = generateExceptionMessage(validationExceptions);
				throw new IonException(message);
			}
		}
	}

	private String generateExceptionMessage(Map<String, String> messages) {
		String result = "";
		int count = 0;
		for (Entry<String, String> message : messages.entrySet()) {
			if (count > 0)
				result += ", ";
			result += message.getKey() + ": " + message.getValue();
			count++;
		}
		return result;
	}

	protected Object parseValue(IPropertyMeta p, Object value)
																														throws IonException {
		if (value != null && p.Type().equals(MetaPropertyType.REFERENCE)) {
			IClassMeta cm = ((IReferencePropertyMeta) p).ReferencedClass();
			// FIXME Обработка составных ключей
			IPropertyMeta refProp = cm.PropertyMeta(cm.KeyProperties()[0]);
			parseValue(refProp, value);
		}

		if (value instanceof String) {
			switch (p.Type()) {
				case CUSTOM:
					value = DaoUtils.cast((String) value,
																((UserTypeMeta) p).getBaseType());
					break;
				default:
					value = DaoUtils.cast((String) value, p.Type());
					break;
			}
		}
		return value;
	}

	protected Object parseCreationValue(IPropertyMeta p, Object value)
																																		throws IonException {
		if (value == null) {
			if (p.AutoAssigned() && p.Type().equals(MetaPropertyType.DATETIME)) {
				return new Date();
			}
			if (p.AutoAssigned() && p.Type().equals(MetaPropertyType.GUID)) {
				UUID guid = UUID.randomUUID();
				return guid.toString();
			}
		}
		return parseValue(p, value);
	}

	protected void setParameter(PreparedStatement statement, IPropertyMeta pm,
															Object value, int index) {
		try {
			if (value != null) {
				switch (pm.Type()) {
					case BOOLEAN:
						statement.setObject(index, value, Types.BOOLEAN);
						break;
					case COLLECTION:
						break;
					case DATETIME:{
						Timestamp v = null;
						if (value instanceof Date)
							v = new Timestamp(((Date)value).getTime());
						else
							v = new Timestamp((long)value);
						statement.setTimestamp(index, v);							
					}break;
					case DECIMAL:
						statement.setObject(index, value, Types.DECIMAL);
						break;
					case REAL:
						statement.setObject(index, value, Types.REAL);
						break;
					case SET:
						statement.setObject(index, value, Types.SMALLINT);
						break;
					case INT:
						if (pm.Size() == null || pm.Size() > 11)
							statement.setObject(index, value, Types.BIGINT);
						else
							statement.setObject(index,value, Types.INTEGER);
						break;
					case REFERENCE:
						IClassMeta cm = ((IReferencePropertyMeta) pm).ReferencedClass();
						// FIXME Обработка составных ключей
						IPropertyMeta refPM = cm.PropertyMeta(cm.KeyProperties()[0]);
						setParameter(statement, refPM, value, index);
						break;
					default:
						statement.setObject(index, value);
						break;
				}
			} else {
				statement.setNull(index, Types.NULL);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IonException e) {
			e.printStackTrace();
		}
	}

	private void trackUpdate(IItem i, Map<String, Object> updates) {
		if (i.getMetaClass() instanceof IClassMeta) {
			String tracker = ((IClassMeta) i.getMetaClass()).ChangeTracker();
			if (tracker != null && !tracker.trim().isEmpty()) {
				Date d = new Date();
				updates.put(tracker, d);
			}
		}
	}

	@Override
	public void setValidators(List<IInputValidator> validators) {
		this.validators = validators;
	}

	@Override
	public long GetCount(String classname) throws IonException {
		return GetCount(GetItem(classname));
	}

	@Override
	public long GetCount(String classname, ListOptions options)
																														 throws IonException {
		return GetCount(GetItem(classname), options);
	}

	@Override
	public long GetCount(IItem dummy) throws IonException {
		return GetCount(dummy, new ListOptions());
	}

	protected String conditionOperation(Condition c, List<ParamValue> values) {
		switch (c.Type()) {
			case EMPTY:
				return " is null";
			case EQUAL: {
				if (c.Value() == null)
					return " is null";
				return " = " + valueToParam(c.Property(), c.Value(), values);
			}
			case IN:
				return " in " + valueToParam(c.Property(), c.Value(), values);
			case LESS:
				return " < " + valueToParam(c.Property(), c.Value(), values);
			case LESS_OR_EQUAL:
				return " <= " + valueToParam(c.Property(), c.Value(), values);
			case LIKE:
				return " like " + valueToParam(c.Property(), c.Value(), values);
			case MORE:
				return " > " + valueToParam(c.Property(), c.Value(), values);
			case MORE_OR_EQUAL:
				return " >= " + valueToParam(c.Property(), c.Value(), values);
			case NOT_EMPTY:
				return " is not null";
			case NOT_EQUAL: {
				if (c.Value() == null)
					return " is not null";

				return " <> " + valueToParam(c.Property(), c.Value(), values);
			}
			default:
				return "";
		}
	}

	protected IStructMeta getRoot(IStructMeta cm) throws IonException {
		IStructMeta result = cm;
		while (result.getAncestor() != null) {
			result = result.getAncestor();
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	protected String valueToParam(String property, Object value,List<ParamValue> values) {
		values.add(new ParamValue(property, value));
		if (value != null && value.getClass().isArray()) {
			Object[] pvalues = (Object[]) value;
			if (pvalues.length > 0)
				return "(?" + StringUtils.repeat(", ?", pvalues.length - 1) + ")";
			return "(null)";
		} else if (value instanceof Collection) {
			if (!((Collection) value).isEmpty())
				return "(?"
						+ StringUtils.repeat(", ?", ((Collection) value).size() - 1) + ")";
			return "(null)";
		}
		return "?";
	}

	protected String processFilterOption(FilterOption fo, IItem dummy,
																			 IStructMeta root,
																			 List<ParamValue> values)
																																	throws IonException {
		String result = "";
		if (fo instanceof Condition) {
			Condition cnd = (Condition) fo;
			if ((cnd.Property() != null) && !(cnd.Property().trim().isEmpty())) {
				if (cnd.Property().equals("class")) {
					String mainTable = SyncUtils.dbSanitiseName(root.getName(), tablePrefix);
					result += mainTable + "."
							+ "_type" + conditionOperation(cnd, values);
				} else if (cnd.Type() == ConditionType.CONTAINS) {
						if (cnd.ValueConditions() != null && !cnd.ValueConditions().isEmpty()) {
							IClassMeta cm = (IClassMeta)((ICollectionPropertyMeta)dummy.Property(cnd.Property()).Meta()).ItemsClass();
							
							Map<String, String> hooks = formAssocHooks(dummy.getMetaClass(), cnd.Property(), null);
							ListOptions lo = new ListOptions();
							lo.SetPageSize(1);
							lo.SetPage(1);
							return " EXISTS (" + getListQuery(GetItem(cm.getName()), new ListOptions(), 
							                                  new LinkedList<IProperty>(), values, 
							                                  new HashMap<String, String>(), 
							                                  hooks.get("join"), hooks.get("where")) + ")";
						}
				} else {
					String actualTable = getActualTable((IClassMeta) dummy.getMetaClass(),
																							cnd.Property());
					if (actualTable != null) {
						result += SyncUtils.dbSanitiseName(actualTable, tablePrefix) + "."
								+ SyncUtils.dbSanitiseName(cnd.Property(), fieldPrefix)
								+ conditionOperation(cnd, values);
					}
				}
			}
		} else if (fo instanceof Operation) {
			Operation op = (Operation) fo;
			String operationClause = "";
			String oper = OperationType.getString(op.getType().getValue());
			if (op.getType().equals(OperationType.NOT)) {
				operationClause += "not ( ";
				oper = "and";
			} else {
				operationClause += "( ";
			}

			int ii = 1;
			for (FilterOption f : op.getOperands()) {
				if (ii > 1)
					operationClause += " " + oper + " ";
				operationClause += processFilterOption(f, dummy, root, values);
				ii++;
			}
			operationClause += " )";
			result += operationClause;
		}
		return result;
	}

	protected String getActualTable(IStructMeta cm, String property)
																																	throws IonException {
		if (cm.PropertyMetas().containsKey(property))
			return cm.getName();
		if (cm.getAncestor() != null)
			return getActualTable(cm.getAncestor(), property);
		return null;
	}

	protected String getCountQuery(IItem dummy, ListOptions options,
																 List<IProperty> queryPropertyValues,
																 List<ParamValue> values, String joinHook,
																 String whereHook) throws IonException {
		String query = "select count(*) as count from "
				+ SyncUtils.dbSanitiseName(dummy.getClassName(), tablePrefix);
		query += getInnerJoins((IClassMeta) dummy.getMetaClass(), null, joinHook);
		String whereClause = getWhereClause(dummy, options, queryPropertyValues,
																				values, whereHook);

		if (!whereClause.isEmpty()) {
			query += " where " + whereClause;
		}
		return query;
	}

	protected long getItemsCount(IItem dummy, ListOptions options,
															 String joinHook, List<Object> joinHookParams,
															 String whereHook) throws IonException {
		IStructMeta sm = dummy.getMetaClass();
		if (sm instanceof IClassMeta) {
			List<IProperty> queryPropertyValues = new LinkedList<IProperty>();
			List<ParamValue> values = new LinkedList<ParamValue>();
			String query = getCountQuery(dummy, options, queryPropertyValues, values,
																	 joinHook, whereHook);
			Connection c = null;
			try {
				c = connectionProvider.getConnection();
				PreparedStatement statement = c.prepareStatement(query);
				int count = 1;

				if (joinHookParams != null)
					for (Object joinHookParam : joinHookParams) {
						statement.setObject(count, joinHookParam);
						count++;
					}

				setFetchParameters(statement, dummy, queryPropertyValues, values, count);

				ResultSet result = statement.executeQuery();
				if (result.next())
					return result.getLong("count");
			} catch (SQLException e) {
				throw new IonException(e.getMessage());
			}
		}
		return 0;
	}

	@Override
	public long GetCount(IItem dummy, ListOptions options) throws IonException {
		return getItemsCount(dummy, options, null, null, null);
	}

	@Override
	public Collection<IItem> GetList(String classname) throws IonException {
		return GetList(GetItem(classname), new ListOptions());
	}

	@Override
	public Collection<IItem> GetList(String classname, ListOptions options)
																																				 throws IonException {
		return GetList(GetItem(classname), options);
	}

	@Override
	public Collection<IItem> GetList(IItem dummy) throws IonException {
		return GetList(dummy, new ListOptions());
	}

	protected String sortingModeType(Sorting s) {
		switch (s.Mode()) {
			case ASC:
				return "asc";
			case DESC:
				return "desc";
			case RANDOM:
				return "random";
			default:
				return "";
		}
	}

	protected String getWhereClause(IItem dummy, ListOptions options,
																	List<IProperty> queryPropertyValues,
																	List<ParamValue> values, String whereHook)
																																							 throws IonException {
		String whereClause = "";
		int count = 1;
		for (Map.Entry<String, IProperty> pair : dummy.getProperties().entrySet()) {
			if ((pair.getValue().getType() != MetaPropertyType.COLLECTION && pair.getValue()
																																					 .getType() != MetaPropertyType.STRUCT)
					&& pair.getValue().getValue() != null) {
				if (count > 1)
					whereClause += " and ";
				String actualTable = getActualTable(dummy.getMetaClass(), pair.getKey());
				whereClause += SyncUtils.dbSanitiseName(actualTable, tablePrefix) + "."
						+ SyncUtils.dbSanitiseName(pair.getKey(), fieldPrefix) + "= ?";
				queryPropertyValues.add(pair.getValue());
				count++;
			}
		}

		IStructMeta root = getRoot(dummy.getMetaClass());

		if (options != null) {
			for (FilterOption fo : options.Filter()) {
				if (!whereClause.isEmpty())
					whereClause += " and ";
				whereClause += processFilterOption(fo, dummy, root, values);
				count++;
			}
		}

		if (whereHook != null && !whereHook.isEmpty()) {
			if (!whereClause.isEmpty())
				whereClause = whereClause + " and ";
			whereClause = whereClause + whereHook;
		}

		return whereClause;
	}

	protected void prepareListOptions(IItem dummy, ListOptions options)
																																		 throws IonException {
	}

	protected String getListQuery(IItem dummy, ListOptions options,
																List<IProperty> queryPropertyValues,
																List<ParamValue> values, Map<String, String> fields_aliases, 
																String joinHook, String whereHook)
																																			throws IonException {
		prepareListOptions(dummy, options);
		
		IClassMeta cm = (IClassMeta) dummy.getMetaClass();
		
		String select = "select";
		String from = "from";
		String ij = getInnerJoins(cm, null, joinHook);
		
		from += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + ij;		
		select += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + ".*";		
		
		IStructMeta ancestor = cm;
		int i = 0;
		while(ancestor != null){
			if(ancestor != cm)
				select += ", " + SyncUtils.dbSanitiseName(ancestor.getName(), tablePrefix) + ".*";
						
			String[] eager_references_parse_parts = parseEagerReferences(ancestor, "c"+(i++), fields_aliases);
			
			select += eager_references_parse_parts[0];
			from += eager_references_parse_parts[1];
			
			ancestor = ancestor.getAncestor();
		}
		
		String query = select + " " + from;

		String whereClause = getWhereClause(dummy, options, queryPropertyValues,
																				values, whereHook);

		String sorting = "";
		if (options != null) {
			i = 1;
			for (Sorting s : options.Sorting()) {
				if ((s.Property() != null) && !(s.Property().trim().isEmpty())) {
					if (i > 1)
						sorting += ", ";
					String actualTable = getActualTable(dummy.getMetaClass(),
																							s.Property());
					sorting += SyncUtils.dbSanitiseName(actualTable, tablePrefix) + "."
							+ SyncUtils.dbSanitiseName(s.Property(), fieldPrefix) + " "
							+ sortingModeType(s);
					i++;
				}
			}
		}
		if (!whereClause.equals("")) {
			query += " where " + whereClause;
		}
		if (!sorting.equals("")) {
			query += " order by " + sorting;
		}
		if (options.PageSize() != null) {
			query += " limit ";
			if (options.Page() != null)
				query += ((options.Page() - 1) * options.PageSize()) + ", ";
			query += options.PageSize();
		}

		showSql(query);
		return query;
	}

	@SuppressWarnings("rawtypes")
	protected int setFetchParameters(PreparedStatement statement, IItem item,
																	 List<IProperty> queryPropertyValues,
																	 List<ParamValue> values, int start)
																												throws IonException {
		int count = start;

		for (IProperty entry : queryPropertyValues) {
			setParameter(statement, 
			             entry.Meta(), 
			             entry.getValue(), count);
			count++;
		}

		IPropertyMeta discrPM = new PropertyMeta("_type",
																						 MetaPropertyType.STRING,
																						 (short) 200);

		for (ParamValue entry : values) {
			IPropertyMeta pm = null;
			if (entry.property.equals("class"))
				pm = discrPM;
			else {
				pm = item.getMetaClass().PropertyMeta(entry.property);
				if (pm == null)
					throw new IonException("не найден атрибут для фильтрации");
			}

			Object[] pvalues = null;

			if (entry.value != null && entry.value.getClass().isArray()) {
				pvalues = (Object[]) entry.value;
			} else if (entry.value instanceof Collection) {
				pvalues = ((Collection) entry.value).toArray();
			} else {
				pvalues = new Object[1];
				pvalues[0] = entry.value;
			}

			for (Object v : pvalues) {
				setParameter(statement, pm, parseValue(pm, v), count);
				count++;
			}
		}
		return count;
	}

	protected ResultSet performGetList(IItem dummy, ListOptions options, String joinHook, List<Object> joinHookParams, String whereHook, Map<String, String> fields_aliases)
																																								 throws IonException,
																																								 SQLException {
		List<IProperty> queryPropertyValues = new LinkedList<IProperty>();
		List<ParamValue> values = new LinkedList<ParamValue>();
		String query = getListQuery(dummy, options, queryPropertyValues, values, fields_aliases,
																joinHook, whereHook);

		Connection c = connectionProvider.getConnection();
		PreparedStatement statement = c.prepareStatement(query);
		int count = 1;

		if (joinHookParams != null)
			for (Object joinHookParam : joinHookParams) {
				statement.setObject(count, joinHookParam);
				count++;
			}

		setFetchParameters(statement, dummy, queryPropertyValues, values, count);

		ResultSet rs = statement.executeQuery();
		return rs;
	}
	
	private void setAttrOfItem(IStructPropertyMeta spm, IPropertyMeta pm, ResultSet rs, IItem item) throws SQLException, IonException {
		if (pm.Type() == MetaPropertyType.STRUCT){
			IStructMeta sm = ((IStructPropertyMeta)pm).StructClass();
			while (sm != null){
				for (IPropertyMeta pm1: sm.PropertyMetas().values()){
					setAttrOfItem((IStructPropertyMeta)pm,pm1, rs,item);
				}
				sm = sm.getAncestor();
			}
		} else {
			if (spm != null)
				item.Set(spm.Name() + structSeparator + pm.Name(), getResultObject(spm, rs, pm));
			else
				item.Set(pm.Name(), getResultObject(spm, rs, pm));
		}
	}	
	
	private void putAttrToMap(IStructPropertyMeta spm, IPropertyMeta pm, ResultSet rs, Map<String, Object> data, IStructMeta m, Map<String, String> field_aliases) throws SQLException, IonException {
		if (pm.Type() == MetaPropertyType.STRUCT){
			IStructMeta sm = ((IStructPropertyMeta)pm).StructClass();
			while (sm != null){
				for (IPropertyMeta pm1: sm.PropertyMetas().values()){
					putAttrToMap((IStructPropertyMeta)pm, pm1, rs, data, m, field_aliases);
				}
				sm = sm.getAncestor();
			}
		} else {
			if (m != null && field_aliases != null){
				if (spm != null)
					data.put(spm.Name() + structSeparator + pm.Name(), getResultObject(rs, pm, field_aliases.get(m.getName() + "." + spm.Name() + structSeparator + pm.Name())));
				else	
					data.put(pm.Name(), getResultObject(rs, pm, field_aliases.get(m.getName() + "." + pm.Name())));
			} else {
				if (spm != null)
					data.put(spm.Name() + structSeparator + pm.Name(), getResultObject(spm, rs, pm));
				else
					data.put(pm.Name(), getResultObject(spm, rs, pm));
			}
		}
	}
	
	private void putAttrToMap(IStructPropertyMeta spm, IPropertyMeta pm, ResultSet rs, Map<String, Object> data) throws IonException, SQLException{
			putAttrToMap(spm, pm, rs, data, null, null);
	}
	

	private Map<String, IItem> produceItems(ResultSet rs,
																					IItem dummy,
																					Map<String, Collection<Object>> itemsToEnrich, Map<String, String> fields_aliases)
																																												throws SQLException,
																																												IonException {
		Map<String, IItem> result = new LinkedTreeMap<String, IItem>();
		while (rs.next()) {
			IClassMeta actualCm = (IClassMeta) dummy.getMetaClass();
			String type = rs.getString("_type");

			// FIXME Обработка составных ключей
			Object id = getResultObject(rs,
																	actualCm.PropertyMeta(actualCm.KeyProperties()[0]));

			if (type != null && !type.equals(dummy.getClassName())) {
				actualCm = (IClassMeta) metaRepository.Get(type);
				if (!itemsToEnrich.containsKey(type))
					itemsToEnrich.put(type, new LinkedList<Object>());
				itemsToEnrich.get(type).add(id);
			}

			Map<String, Object> base = new HashMap<String, Object>();
			IStructMeta ancestor = dummy.getMetaClass();
			while (ancestor != null) {
				for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas()
																													.entrySet()) {
							IPropertyMeta prop = entry.getValue();
							if(prop.Type() == MetaPropertyType.REFERENCE && ((IReferencePropertyMeta)prop).getEagerLoading()){
								base.put(entry.getValue().Name(), 
								         assembleEagerReferenceItem((IReferencePropertyMeta)prop, rs, fields_aliases));
							} else 
								putAttrToMap(null, prop, rs, base);
				}
				ancestor = ancestor.getAncestor();
				if (id != null)
					result.put(id.toString(), new JdbcItem(id.toString(),
																							 base,
																							 actualCm,
																							 this));
			}
		}
		return result;
	}

	private void produceEnrichedItems(ResultSet rs,
																		IItem dummy,
																		Map<String, Collection<Object>> itemsToEnrich,
																		Map<String, IItem> result)
																															throws SQLException,
																															IonException {
		for (Entry<String, Collection<Object>> enrichEntry : itemsToEnrich.entrySet()) {
			IClassMeta actualCm = (IClassMeta) metaRepository.Get(enrichEntry.getKey());
			Map<String, String> fields_aliases = new HashMap<String, String>();
			rs = performEnrichSelect(enrichEntry.getValue().toArray(), actualCm,
															 dummy.getClassName(), fields_aliases);
			while (rs.next()) {
				IStructMeta ancestor = actualCm;
				// FIXME Обработка составных ключей
				Object id = getResultObject(rs,
																		actualCm.PropertyMeta(actualCm.KeyProperties()[0]));
				IItem item = result.get(id.toString());
				while (ancestor != null && item != null
						&& !ancestor.getName().equals(dummy.getClassName())) {
					for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas().entrySet()){
						IPropertyMeta prop = entry.getValue();
  					if(prop.Type() == MetaPropertyType.REFERENCE && ((IReferencePropertyMeta)prop).getEagerLoading()){
  						item.Set(entry.getValue().Name(), assembleEagerReferenceItem((IReferencePropertyMeta)prop, rs, fields_aliases));
  					} else {
  						setAttrOfItem(null, entry.getValue(), rs, item);
  					}
					}
					ancestor = ancestor.getAncestor();
				}
			}
			IStructMeta ancestor = actualCm;
			while (ancestor != null && !ancestor.getName().equals(dummy.getClassName())) {
				for(Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas().entrySet()){
					if(entry.getValue().Type() == MetaPropertyType.COLLECTION){
						ICollectionPropertyMeta pm = (ICollectionPropertyMeta)entry.getValue();
						if(pm.ItemsClass() instanceof IClassMeta && pm.getEagerLoading()){
							Map<String, Collection<IItem>> collections = uploadCollection((IClassMeta)ancestor, pm, enrichEntry.getValue().toArray());
							for(Entry<String, Collection<IItem>> c_entry:collections.entrySet())
								result.get(c_entry.getKey()).Set(pm.Name(), c_entry.getValue());
						}
					}
				}
				ancestor = ancestor.getAncestor();
			}
		}
	}

	protected Collection<IItem> getItemsList(IItem dummy, ListOptions options,
																					 String joinHook,
																					 List<Object> joinHookParams,
																					 String whereHook)
																														throws IonException {
		Map<String, IItem> result = new LinkedTreeMap<String, IItem>();
		IStructMeta sm = dummy.getMetaClass();
		if (sm instanceof IClassMeta) {
			Map<String, Collection<Object>> itemsToEnrich = new HashMap<String, Collection<Object>>();
			ResultSet rs = null;
			try {
			  Map<String, String> fields_aliases = new HashMap<String, String>();
				rs = performGetList(dummy, options, joinHook, joinHookParams, whereHook, fields_aliases);
				result = produceItems(rs, dummy, itemsToEnrich, fields_aliases);
			} catch (SQLException e) {
				throw new IonException(e);
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (Exception e) {
					}
					try {
						rs.getStatement().close();
					} catch (Exception e) {
					}
				}
			}
			
			Collection<String> ids = result.keySet();
			if(!ids.isEmpty()){
  			IStructMeta ancestor = dummy.getMetaClass();
  			while(ancestor != null){
  				for(Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas().entrySet()){
  					if(entry.getValue().Type() == MetaPropertyType.COLLECTION){
  						ICollectionPropertyMeta pm = (ICollectionPropertyMeta)entry.getValue();
  						if(pm.ItemsClass() instanceof IClassMeta && pm.getEagerLoading()){
  							try {
  								Map<String, Collection<IItem>> collections = uploadCollection((IClassMeta)ancestor, pm, ids.toArray());
  								for(Entry<String, Collection<IItem>> c_entry:collections.entrySet())
  									result.get(c_entry.getKey()).Set(pm.Name(), c_entry.getValue());
  							} catch (SQLException e) {
  								throw new IonException(e);
  							}
  						}
  					}
  				}
  				ancestor = ancestor.getAncestor();
  			}
			}

			if (!itemsToEnrich.isEmpty()) {
				try {
					produceEnrichedItems(rs, dummy, itemsToEnrich, result);					
				} catch (SQLException e) {
					throw new IonException(e);
				} finally {
					if (rs != null) {
						try {
							rs.close();
						} catch (Exception e) {
						}
						try {
							rs.getStatement().close();
						} catch (Exception e) {
						}
					}
				}
			}
		}
		if (options.TotalCount() == null) {
			if (options.PageSize() == null)
				options.SetTotalCount((long) result.size());
			else
				options.SetTotalCount(GetCount(dummy, options));
		}
		return result.values();
	}

	@Override
	public Collection<IItem> GetList(IItem dummy, ListOptions options)
																																		throws IonException {
		return getItemsList(dummy, options, null, null, null);
	}

	protected ResultSet performEnrichSelect(Object[] ids, IClassMeta cm,
																				String limit, Map<String, String> fields_aliases) throws SQLException,
																											 IonException {
		if (ids.length > 0) {
			String select = "select";
			String from = "from";
			String where = "where";
	
			// FIXME Обработка составных ключей
			
			String ij = getInnerJoins(cm, limit, null);			
			from += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + ij;		
			select += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + ".*";
			where += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + "." + SyncUtils.dbSanitiseName(cm.KeyProperties()[0], fieldPrefix) + " in (?"
					+ StringUtils.repeat(",?", ids.length - 1) + ")";

			IStructMeta ancestor = cm;
			int i = 0;
			while(ancestor != null && (limit == null || !ancestor.getName().equals(limit))){
				if(ancestor != cm)
					select += ", " + SyncUtils.dbSanitiseName(ancestor.getName(), tablePrefix) + ".*";
								
				String[] eager_references_parse_parts = parseEagerReferences(ancestor, "c"+(i++), fields_aliases);
				
				select += eager_references_parse_parts[0];
				from += eager_references_parse_parts[1];
				
				ancestor = ancestor.getAncestor();
			}
			
			String query = select + " " + from + " " + where;
			
			showSql(query);
			Connection c = connectionProvider.getConnection();
			PreparedStatement statement = c.prepareStatement(query);
			for (i = 1; i <= ids.length; i++)
				statement.setObject(i, ids[i - 1]);
			return statement.executeQuery();
		}
		return null;
	}

	@Override
	public Iterator<IItem> GetIterator(String classname) throws IonException {
		return GetIterator(GetItem(classname), new ListOptions());
	}

	@Override
	public Iterator<IItem> GetIterator(String classname, ListOptions options)
																																					 throws IonException {
		return GetIterator(GetItem(classname), options);
	}

	@Override
	public Iterator<IItem> GetIterator(IItem dummy) throws IonException {
		return GetIterator(dummy, new ListOptions());
	}

	protected class JdbcListIterator implements Iterator<IItem> {
		private ResultSet rs;
		private IClassMeta cm;
		private JdbcDataRepository rep;
		private Map<String, String> fa;

		public JdbcListIterator(ResultSet rs, IClassMeta cm, JdbcDataRepository r, Map<String, String> fields_aliases) {
			this.rs = rs;
			this.cm = cm;
			this.rep = r;
			this.fa = fields_aliases;
		}

		@Override
		public boolean hasNext() {
			try {
				if (!rs.isLast() && ((rs.getRow() != 0) || rs.isBeforeFirst())) {
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return false;
		}

		@Override
		public IItem next() {
			try {
				if (rs.next()) {
					String type = rs.getString("_type");
					IStructMeta actual = cm;
				// TODO Обоработка составных ключей
					Object id = getResultObject(rs,
																			cm.PropertyMeta(((IClassMeta) actual).KeyProperties()[0]));
					Map<String, Object> base = new HashMap<String, Object>();
					IStructMeta ancestor = cm;
					while (ancestor != null) {
						for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas()
																															.entrySet()) {
							IPropertyMeta prop = entry.getValue();
							if(prop.Type() == MetaPropertyType.REFERENCE && ((IReferencePropertyMeta)prop).getEagerLoading()){
								base.put(entry.getValue().Name(), 
								         assembleEagerReferenceItem((IReferencePropertyMeta)prop, rs, fa));
							} else {
								putAttrToMap(null, entry.getValue(), rs, base);
							}
						}
						ancestor = ancestor.getAncestor();
					}

					if (!type.equals(cm.getName())) {
						actual = metaRepository.Get(type);
						Map<String, Object> enrich = new HashMap<String, Object>();
						ancestor = actual;
						while (ancestor != null && !ancestor.getName().equals(cm.getName())) {
							for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas()
																																.entrySet()) {
								enrich.put(entry.getValue().Name(), entry.getValue());
							}
							ancestor = ancestor.getAncestor();
						}
						performSelect(id, (IClassMeta) actual, enrich, cm.getName());
						base.putAll(enrich);
					}
					return new JdbcItem(id.toString(), base, actual, this.rep);
				}
			} catch (SQLException | IonException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	protected Iterator<IItem> getItemsIterator(IItem dummy, ListOptions options,
																						 String joinHook,
																						 List<Object> joinHookParams,
																						 String whereHook)
																															throws IonException {
		try {
			IStructMeta sm = dummy.getMetaClass();
			if (sm instanceof IClassMeta){
				Map<String, String> fields_aliases = new HashMap<String, String>();
				ResultSet rs = performGetList(dummy, options, joinHook, joinHookParams, whereHook, fields_aliases);
				return new JdbcListIterator(rs,
																		(IClassMeta) sm,
																		this, fields_aliases);
			}
		} catch (Exception e) {
			throw new IonException(e.getMessage());
		}
		return null;
	}

	public Iterator<IItem> GetIterator(IItem dummy, ListOptions options)
																																			throws IonException {
		return getItemsIterator(dummy, options, null, null, null);
	}

	protected String getInnerJoins(IClassMeta cm, String limit)
																														 throws IonException {
		return getInnerJoins(cm, limit, null);
	}

	protected String getInnerJoins(IClassMeta cm, String limit, String joinHook)
																																							throws IonException {
		return getInnerJoins(cm, limit, joinHook, null);
	}
	
	protected String getInnerJoins(IClassMeta cm, String limit, String joinHook, Map<String, String> out_aliases)
																																							throws IonException {
		boolean hasAlias = (out_aliases != null);
		String aliasMask = "c";
		int aliasCounter = 0;
		
		String joins = "";
		String cmAlias = hasAlias?(aliasMask+(aliasCounter++)):(SyncUtils.dbSanitiseName(cm.getName(), tablePrefix));
		if(hasAlias)
			out_aliases.put(cm.getName(), cmAlias);
		// FIXME Обработка составных ключей
		String cmKey = cmAlias + "." + SyncUtils.dbSanitiseName(cm.KeyProperties()[0], fieldPrefix);
		IClassMeta ancestor = (IClassMeta) cm.getAncestor();
		while (ancestor != null
				&& (limit == null || !ancestor.getName().equals(limit))) {
			String table = SyncUtils.dbSanitiseName(ancestor.getName(), tablePrefix);
			String tableAlias = hasAlias?(aliasMask+(aliasCounter++)):(table);
			if(hasAlias)
				out_aliases.put(ancestor.getName(), tableAlias);
			
			// FIXME Обработка составных ключей
			String keyField = SyncUtils.dbSanitiseName(ancestor.KeyProperties()[0], fieldPrefix);
			joins += " inner join " + table + (hasAlias?(" as " + tableAlias):"") + " on " + tableAlias + "." + keyField + "="	+ cmKey;
			cmKey = tableAlias + "." + keyField;
			ancestor = (IClassMeta) ancestor.getAncestor();
		}
		return joins + ((joinHook != null) ? joinHook : "");
	}
	
	protected String getReferencesLeftJoins(IStructMeta cm, String alias_prefix, Map<String, String> out_aliases) throws IonException {
		String p_aliasMask = "p";
		int p_aliasCounter = 0;
		String joins = "";
		
		for(Entry<String, IPropertyMeta> pm_entry : cm.PropertyMetas().entrySet()){
			if(pm_entry.getValue().Type() == MetaPropertyType.REFERENCE){
				IReferencePropertyMeta pm = (IReferencePropertyMeta) pm_entry.getValue();
				if(pm.getEagerLoading()){
					String rp_alias = alias_prefix+p_aliasMask+(p_aliasCounter++);
					IClassMeta rcm = pm.ReferencedClass();
					
					String c_aliasMask = "c";
					int c_aliasCounter = 0;
					IClassMeta i = rcm;
					while(i != null) {
						String i_alias = rp_alias+c_aliasMask+(c_aliasCounter++);
						out_aliases.put(pm.Name()+"."+i.getName(), i_alias);
						joins += getLeftJoinString(i, i_alias, SyncUtils.dbSanitiseName(cm.getName(), tablePrefix)+"."+SyncUtils.dbSanitiseName(pm.Name(), fieldPrefix));
						i = (IClassMeta)i.getAncestor();
					}
					
					Collection<IStructMeta> sms = rcm.Descendants();
					for(IStructMeta j : sms){
						String j_alias = rp_alias+c_aliasMask+(c_aliasCounter++);
						out_aliases.put(pm.Name()+"."+j.getName(), j_alias);
						joins += getLeftJoinString((IClassMeta)j, j_alias, SyncUtils.dbSanitiseName(cm.getName(), tablePrefix)+"."+SyncUtils.dbSanitiseName(pm.Name(), fieldPrefix));
					}
				}
			}
		}
		
		return joins;
	}
	
	private String getLeftJoinString(IClassMeta i, String i_alias, String on_left_part){
		// FIXME Обработка составных ключей
		return " left join " + SyncUtils.dbSanitiseName(i.getName(), tablePrefix) + " as " + i_alias 
				+ " on " + i_alias + "." + SyncUtils.dbSanitiseName(i.KeyProperties()[0], fieldPrefix) + " = " + on_left_part;
	}
	
	
	private Object[] parseEagerField(IStructPropertyMeta spm, String field_aliasMask, 
	                            String reference_class_alias,
	                            String cn,
	                            IPropertyMeta pm,
	                            int field_aliasCounter, 
	                            Map<String, String> fields_aliases) throws IonException{
		int counter = field_aliasCounter;
		String to_select = "";
		if (pm.Type() == MetaPropertyType.STRUCT){
			IStructMeta sm = ((IStructPropertyMeta)pm).StructClass();
			while (sm != null){
				for (IPropertyMeta pm1: sm.PropertyMetas().values()){
					Object[] tmp = parseEagerField((IStructPropertyMeta)pm, field_aliasMask, reference_class_alias, cn, 
					                               pm1, counter, 
					                               fields_aliases);
					counter = (int)tmp[1];
					to_select += (String)tmp[0];
				}
				sm = sm.getAncestor();
			}
		} else {
			String field_alias = field_aliasMask+(counter++);
			String property = ((spm != null)?(spm.Name() + structSeparator):"") + pm.Name();
			
			to_select = ", " + reference_class_alias + "."
				+ SyncUtils.dbSanitiseName(property, fieldPrefix) + " as " + field_alias;
			fields_aliases.put(cn+"."+property, field_alias);
		}
		return new Object[]{to_select, counter};
	}
	
	protected String[] parseEagerReferences(IStructMeta parsed_class, String alias_prefix, Map<String, String> fields_aliases) throws IonException{
		String to_select = "";
		String to_from = ""; 
		String field_aliasMask = "f";
		int field_aliasCounter = fields_aliases.size();			

		Map<String, String> reference_branch_aliases = new HashMap<String, String>();
		to_from += getReferencesLeftJoins(parsed_class, alias_prefix, reference_branch_aliases);			

		for(Entry<String, String> rba_entry : reference_branch_aliases.entrySet()){
			IClassMeta referenced_class = (IClassMeta)metaRepository.Get(rba_entry.getKey().split("\\.")[1]);
			String reference_class_alias = rba_entry.getValue();
			for(Entry<String, IPropertyMeta> field_entry : referenced_class.PropertyMetas().entrySet()){
				if(field_entry.getValue().Type() != MetaPropertyType.COLLECTION ||
						!(((ICollectionPropertyMeta)field_entry.getValue()).ItemsClass() instanceof IClassMeta)){
					Object[] tmp = parseEagerField(null,field_aliasMask, reference_class_alias, rba_entry.getKey(), field_entry.getValue(), field_aliasCounter, fields_aliases);
					field_aliasCounter = (int)tmp[1];
					to_select += (String)tmp[0];
				}
			}
			if(referenced_class.getAncestor() == null){
				String field_alias = field_aliasMask+(field_aliasCounter++);
				to_select += ", " + reference_class_alias + "."+"_type"
							+ " as " + field_alias;
				fields_aliases.put(rba_entry.getKey()+"."+"_type", field_alias);
			}
		}
		return new String[]{to_select, to_from};
	}
	
	private void assembleEagerProperty(IReferencePropertyMeta rp, IPropertyMeta pm, IStructMeta c, ResultSet result_set, Map<String, Object> data, Map<String, String> fields_aliases) throws SQLException, IonException {
		if (pm.Type() == MetaPropertyType.STRUCT){
			IStructMeta sc = ((IStructPropertyMeta)pm).StructClass();
			while (sc != null){
				for (IPropertyMeta pm1: sc.PropertyMetas().values())
					assembleEagerProperty(rp, pm1, sc, result_set, data, fields_aliases);
				sc = sc.getAncestor();
			}
		} else {
			String field_alias = fields_aliases.get(rp.Name()+"."+c.getName()+"."+pm.Name());
			Object field_value = getResultObject(result_set, pm, field_alias);
			data.put(pm.Name(), field_value);
		}
	}
	
	protected IItem assembleEagerReferenceItem(IReferencePropertyMeta reference_property, ResultSet result_set, Map<String, String> fields_aliases) throws SQLException, IonException{
		IClassMeta referenced_class = reference_property.ReferencedClass();
		Map<String, Object> data = new HashMap<String, Object>();
		
		IStructMeta real_referenced_class = referenced_class;
		if(fields_aliases != null && !fields_aliases.isEmpty()){
  		IStructMeta ancestor = referenced_class;
  		IStructMeta root = null;
  		while(ancestor != null) {
  			for(Entry<String, IPropertyMeta> e : ancestor.PropertyMetas().entrySet())
  				assembleEagerProperty(reference_property, e.getValue(), ancestor, result_set, data, fields_aliases);
  			root = ancestor;
  			ancestor = ancestor.getAncestor();
  		}		
  
  		String type_field_alias = fields_aliases.get(reference_property.Name()+"."+root.getName()+"."+"_type");
  		Object type_field_value = getResultObject(result_set, new PropertyMeta("_type", MetaPropertyType.STRING, (short) 200), type_field_alias);
  		
  		if(type_field_value == null)
  			return null;
  		
  		if(!referenced_class.getName().equals(type_field_value.toString())){
  			real_referenced_class = metaRepository.Get(type_field_value.toString());
  			ancestor = real_referenced_class;
  			while (ancestor != null && ancestor.getName() != referenced_class.getName()) {
  				for(Entry<String, IPropertyMeta> e : ancestor.PropertyMetas().entrySet())
    				assembleEagerProperty(reference_property, e.getValue(), ancestor, result_set, data, fields_aliases);  				
  				ancestor = ancestor.getAncestor();
  			}
  		}
		}
		
		return new JdbcItem(getResultObject(result_set, reference_property).toString(), data, real_referenced_class, this);
	}

	protected Map<String, Collection<IItem>> uploadCollection(IClassMeta cm, ICollectionPropertyMeta pm, Object[] ids) throws IonException, SQLException {
		String select = "select";
		String from = "from";
		String where = "where";
		
		IClassMeta i_cm = (IClassMeta)pm.ItemsClass();
		String inner_joins = getInnerJoins(i_cm, null, null);
		String key_field_alias = "k";
		
		if(pm.BackReference() != null && !pm.BackReference().trim().isEmpty()){
			from += " " + SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix);
			where += " " + SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix) + "." + SyncUtils.dbSanitiseName(i_cm.PropertyMeta(pm.BackReference()).Name(), fieldPrefix);
			select += " " + SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix) + "." + SyncUtils.dbSanitiseName(i_cm.PropertyMeta(pm.BackReference()).Name(), fieldPrefix)
					+ " as " + key_field_alias;
		} else if(pm.BackCollection() != null && !pm.BackCollection().trim().isEmpty()) {
			// FIXME Обработка составных ключей			
			from += " " + SyncUtils.RelationshipName(i_cm.getName(), i_cm.PropertyMeta(pm.BackCollection()).Name()) + " as b inner join "
					+ SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix)
					+ " on " + SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix) + "." + SyncUtils.dbSanitiseName(i_cm.KeyProperties()[0], fieldPrefix) + "=b.master";
			where += " b.detail";
			select += " b.detail as " + key_field_alias;
		} else {
			// FIXME Обработка составных ключей
			from += " " + SyncUtils.RelationshipName(cm.getName(), pm.Name()) + " as b inner join "
					+ SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix)
					+ " on " + SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix) + "." + SyncUtils.dbSanitiseName(i_cm.KeyProperties()[0], fieldPrefix) + "=b.detail";
			where += " b.master";
			select += " b.master as " + key_field_alias;
		}
		
		IStructMeta ancestor = i_cm;
		while(ancestor != null){
			select += ", " + SyncUtils.dbSanitiseName(ancestor.getName(), tablePrefix) + ".*";
			ancestor = ancestor.getAncestor();
		}
		
		from += inner_joins;
		where += " in (?" + StringUtils.repeat(",?", ids.length - 1) + ")";
		
		Map<String, String> fields_aliases = new HashMap<String, String>();
		
		Collection<IStructMeta> descendants = i_cm.Descendants();
		int desc_counter = 0;
		int fields_counter = 0;
		String desc_alias_masc = "c";
		String fields_alias_masc = "f";
		for(IStructMeta d : descendants){
			String desc_alias = desc_alias_masc+(desc_counter++);			
			// FIXME Обработка составных ключей
			from += getLeftJoinString((IClassMeta)d, desc_alias, SyncUtils.dbSanitiseName(i_cm.getName(), tablePrefix)+"."+SyncUtils.dbSanitiseName(i_cm.KeyProperties()[0], fieldPrefix));
			for(Entry<String, IPropertyMeta> dp:d.PropertyMetas().entrySet()){
				if(dp.getValue().Type() != MetaPropertyType.COLLECTION || !(((ICollectionPropertyMeta)dp.getValue()).ItemsClass() instanceof IClassMeta)){
					String field_alias = fields_alias_masc+(fields_counter++);
					select += ", " + desc_alias + "." + SyncUtils.dbSanitiseName(dp.getValue().Name(), fieldPrefix) + " as " + field_alias;
					fields_aliases.put(d.getName() + "." + dp.getValue().Name(), field_alias);
				}
			}
		}
		
		String query = select + " " + from + " " + where;
		
		showSql(query);
		
		PreparedStatement statement = null;
		ResultSet rs = null;

		Connection c = null;
		
		Map<String, Collection<IItem>> result = new HashMap<String, Collection<IItem>>();
		
		try {
			c = connectionProvider.getConnection();
			statement = c.prepareStatement(query);
			for (int i = 1; i <= ids.length; i++)
				statement.setObject(i, ids[i - 1]);
			rs = statement.executeQuery();
			
			while (rs.next()) {
				Map<String, Object> base = new HashMap<String, Object>();
				
				ancestor = i_cm;
				while (ancestor != null){
					for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas().entrySet())
						putAttrToMap(null, entry.getValue(), rs, base);
					ancestor = ancestor.getAncestor();
				}
				
				Object type = getResultObject(rs, new PropertyMeta("_type", MetaPropertyType.STRING, (short) 200));
				
				IStructMeta actual = null;
				if(type != null)
					actual = metaRepository.Get(type.toString());
				if(actual == null)
					actual = i_cm;
								
				ancestor = actual;
				while (ancestor != null && !ancestor.getName().equals(i_cm.getName())) {
					for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas()
							.entrySet()) {
						putAttrToMap(null, entry.getValue(), rs, base, ancestor, fields_aliases);
					}
					ancestor = ancestor.getAncestor();
				}
				// FIXME Обработка составных ключей
				Object coll_key = getResultObject(rs, i_cm.PropertyMeta(i_cm.KeyProperties()[0]), key_field_alias);
				IItem item = new JdbcItem(base.get(((IClassMeta)actual).KeyProperties()[0]).toString(), base, actual, this);
				
				if(!result.containsKey(coll_key.toString()))
					result.put(coll_key.toString(), new LinkedList<IItem>());
				result.get(coll_key.toString()).add(item);				
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				statement.close();
			} catch (Exception e) {
			}
		}		
		return result;
	}
	
	protected void performSelect(Object id, IClassMeta cm,
															 Map<String, Object> data, String limit)
																																			throws IonException,
																																			SQLException {
		String select = "select";
		String from = "from";
		String where = "where";
		
		String inner_joins = getInnerJoins(cm, limit, null);
		// FIXME Обработка составных ключей
		from += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + inner_joins;		
		select += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + ".*";
		where += " " + SyncUtils.dbSanitiseName(cm.getName(), tablePrefix) + "." + SyncUtils.dbSanitiseName(cm.KeyProperties()[0], fieldPrefix) + " = ?";
		
		Map<String, String> fields_aliases = new HashMap<String, String>();
		IStructMeta ancestor = cm;
		int i = 0;
		while(ancestor != null && (limit == null || !ancestor.getName().equals(limit))){
			if(ancestor != cm)
				select += ", " + SyncUtils.dbSanitiseName(ancestor.getName(), tablePrefix)+ ".*";
						
			String[] eager_references_parse_parts = parseEagerReferences(ancestor, "c"+(i++), fields_aliases);
			
			select += eager_references_parse_parts[0];
			from += eager_references_parse_parts[1];
			
			ancestor = ancestor.getAncestor();
		}
		
		String query = select + " " + from + " " + where;
		
		showSql(query);

		PreparedStatement statement = null;
		ResultSet rs = null;

		Connection c = null;
		try {
			c = connectionProvider.getConnection();
			statement = c.prepareStatement(query);
			statement.setObject(1, id);
			rs = statement.executeQuery();
			if (rs.next()) {
				Map<String, Object> tmp = new HashMap<String, Object>();
				tmp.putAll(data);
				data.clear();
				for (Map.Entry<String, Object> entry : tmp.entrySet()) {
					IPropertyMeta prop = (IPropertyMeta) entry.getValue();
					if(prop.Type() == MetaPropertyType.REFERENCE && ((IReferencePropertyMeta)prop).getEagerLoading()){											
						data.put(entry.getKey(), assembleEagerReferenceItem((IReferencePropertyMeta)prop, rs, fields_aliases));
					} else {
						putAttrToMap(null, prop, rs, data);
					}
				}
			} else
				data.clear();
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				rs.close();
			} catch (Exception e) {
			}
			try {
				statement.close();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public IItem GetItem(String classname, String id) throws IonException {
		IItem itemFromCache = getFromCache(classname, id);
		if (itemFromCache != null) {
			return itemFromCache;
		} else {
			IItem i = getItemFromDb(classname, id);
			if (i != null)
				putIntoCache(i);
			return i;
		}
	}

	private IItem getItemFromDb(String classname, String id) throws IonException {
		IStructMeta sm = metaRepository.Get(classname);
		IStructMeta actual = sm;
		IItem i = null;
		if (sm != null) {
			if (sm instanceof IClassMeta) {
				Map<String, Object> base = new HashMap<String, Object>();
				base.put("_type", new PropertyMeta("_type",
																					 MetaPropertyType.STRING,
																					 (short) 200));
				IStructMeta ancestor = sm;
				while (ancestor != null) {
					for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas()
																														.entrySet()) {
						base.put(entry.getValue().Name(), entry.getValue());
					}
					ancestor = ancestor.getAncestor();
				}

				try {
					performSelect(id, (IClassMeta) sm, base, null);
				} catch (SQLException e) {
					throw new IonException(e);
				}

				if (!base.isEmpty()) {
					if (base.get("_type") != null) {
						String type = base.get("_type").toString();
						if (!sm.getName().equals(type)) {
							actual = metaRepository.Get(type);
							Map<String, Object> enrich = new HashMap<String, Object>();
							ancestor = actual;
							while (ancestor != null && ancestor.getName() != sm.getName()) {
								for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas()
																																	.entrySet()) {
									enrich.put(entry.getValue().Name(), entry.getValue());
								}
								ancestor = ancestor.getAncestor();
							}

							try {
								performSelect(id, (IClassMeta) actual, enrich, sm.getName());
							} catch (SQLException e) {
								throw new IonException(e);
							}

							base.putAll(enrich);
						}
					}
					base.remove("_type");
					i = new JdbcItem(id, base, actual, this);
				}
			}
		}
		return i;
	}

	@Override
	public IItem GetItem(Object dummy) throws IonException {
		String cn = dummy.getClass().getSimpleName();
		IStructMeta sm = metaRepository.Get(cn);
		IItem i = null;
		if (sm != null) {
			IClassMeta cm = ((IClassMeta) sm);
			// FIXME Обработка составных ключей
			String key = cm.KeyProperties()[0];
			Object id = null;
			try {
				Method idGetter = dummy.getClass()
															 .getMethod("get" + capitalizeFirstLetter(key),
																					new Class[] {});
				id = idGetter.invoke(dummy, new Object[] {});
			} catch (Exception e) {
				throw new IonException(e);
			}

			if (sm instanceof IClassMeta) {
				if (id != null) {
					i = GetItem(cn, id.toString());
				} else {
					Collection<IItem> list = GetList(new Item(null, dummy, sm, this),
																					 new ListOptions(1));
					for (IItem tmp : list)
						i = tmp;
				}
			} else {
				i = new Item(id.toString(), dummy, sm, this);
			}
		}
		return i;
	}

	@Override
	public IItem GetItem(String classname) throws IonException {
		IStructMeta sm = metaRepository.Get(classname);
		IItem i = null;
		if (sm != null) {
			if (sm instanceof IClassMeta) {
				Map<String, Object> base = new HashMap<String, Object>();
				IStructMeta ancestor = sm;
				while (ancestor != null) {
					for (Entry<String, IPropertyMeta> entry : ancestor.PropertyMetas()
																														.entrySet()) {
						base.put(entry.getValue().Name(), null);
					}
					ancestor = ancestor.getAncestor();
				}
				i = new JdbcItem(null, base, sm, this);
			}
		} else
			throw new IonException("Не удалось создать объект. Класс "+classname+" не найден!");
		return i;
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

	protected Object performInsert(Connection c, IClassMeta meta,
																 Map<String, Object> data, String className)
																																						throws SQLException,
																																						IonException {
		IClassMeta ancestor = (IClassMeta) meta.getAncestor();
		Object id = null;
		String query = "insert "
				+ SyncUtils.dbSanitiseName(meta.getName(), tablePrefix) + " (";

		Map<String, String> fields = new LinkedHashMap<String, String>();
		
		// FIXME Обработка составных ключей
		if (ancestor != null
				|| !meta.PropertyMeta(meta.KeyProperties()[0]).AutoAssigned()
				|| data.containsKey(meta.KeyProperties()[0])) {
			fields.put(SyncUtils.dbSanitiseName(meta.KeyProperties()[0], fieldPrefix),
								 meta.KeyProperties()[0]);
		}
		
		// FIXME Обработка составных ключей
		if (ancestor == null) {
			fields.put("_type", "class");
			if (!meta.PropertyMeta(meta.KeyProperties()[0]).AutoAssigned()
					|| data.containsKey(meta.KeyProperties()[0]))
				id = data.get(meta.KeyProperties()[0]);
		}

		// FIXME Обработка составных ключей
		Map<String, IPropertyMeta> props = explodeStructs(meta);
		for (Map.Entry<String, IPropertyMeta> entry : props.entrySet()) {
			if (data.containsKey(entry.getKey())
					&& !entry.getKey().equals(meta.KeyProperties()[0]))
				fields.put(SyncUtils.dbSanitiseName(entry.getKey(),fieldPrefix), entry.getKey());
		}

		query = query + StringUtils.join(fields.keySet(), ",") + ") values (?"
				+ StringUtils.repeat(",?", fields.size() - 1) + ")";

		PreparedStatement statement = null;

		if (ancestor != null)
			id = performInsert(c, ancestor, data, className);

		showSql(query);
		try {
			statement = c.prepareStatement(query,
																		 /*needgen ? */Statement.RETURN_GENERATED_KEYS
																						/*: Statement.NO_GENERATED_KEYS*/);
			int count = 1;
			for (Map.Entry<String, String> entry : fields.entrySet()) {
				// FIXME Обработка составных ключей
				if (entry.getValue().equals("class"))
					statement.setString(count, className);
				else if (entry.getValue().equals(meta.KeyProperties()[0]))
					setParameter(statement, meta.PropertyMeta(meta.KeyProperties()[0]),
											 (id == null) ? data.get(entry.getValue()) : id, count);
				else
					setParameter(statement, meta.PropertyMeta(entry.getValue()),
											 data.get(entry.getValue()), count);
				count++;
			}

			statement.executeUpdate();

			//if (needgen) {
				ResultSet generatedKeys = statement.getGeneratedKeys();
				if (generatedKeys.next()) {
					id = generatedKeys.getString(1);
				}
			//}
		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				statement.close();
			} catch (Exception e) {
			}
		}

		return id;
	}

	@Override
	public IItem CreateItem(String classname, Map<String, Object> data,
													IChangeLogger changeLogger) throws IonException {
		JdbcItem i = (JdbcItem) GetItem(classname);
		processValidators(i, data);

		Object id = null;
		IItem result = null;

		IStructMeta sm = metaRepository.Get(classname);
		if (sm instanceof IClassMeta) {
			IClassMeta cm = (IClassMeta) sm;
			
			processRefItems(i,data,changeLogger);
			
			// FIXME Обработка составных ключей
			String keyPropertyName = cm.KeyProperties()[0];

			IProperty keyProperty = i.Property(keyPropertyName);

			Map<String, Object> updates = new LinkedHashMap<String, Object>();

			for (Map.Entry<String, IProperty> p : i.getProperties().entrySet()) 
				if (p.getValue().Meta().Type() != MetaPropertyType.COLLECTION){
  				if (data.containsKey(p.getKey())) {
  					Object v = parseCreationValue(p.getValue().Meta(),
  																				data.containsKey(p.getKey()) ? data.get(p.getKey())
  																																		: null);
  					updates.put(p.getKey(), v);
  				} else {
  						Object v = p.getValue().Meta().DefaultValue(); 
  						if (v != null)
  								updates.put(p.getKey(), v);
  				}
				}

			trackUpdate(i, updates);

			if (keyProperty.Meta().AutoAssigned()
					&& (!updates.containsKey(keyPropertyName) || updates.get(keyPropertyName) == null)) {
				updates.put(keyPropertyName,
										parseCreationValue(keyProperty.Meta(), null));
			}

			Connection c = null;
			try {
				c = connectionProvider.getConnection();
				id = performInsert(c, cm, updates, cm.getName());
			} catch (SQLException e) {
				throw new IonException(e);
			}
			if (id != null) {
				result = getItemFromDb(classname, id.toString());
				if (result != null){
					putIntoCache(result);
					if (changeLogger != null)
						changeLogger.LogChange(ChangelogRecordType.CREATION,
															 classname, result.getItemId(),
															 updates);
				}
			}
		} else
			throw new IonException("Класс " + classname + " не найден!");
		return result;
	}

	@Override
	public IItem SaveItem(IItem item, IChangeLogger changeLogger)
																															 throws IonException {
		Map<String, Object> data = new HashMap<String, Object>();
		for (String property : item.getProperties().keySet()) {
			data.put(property, item.Get(property));
		}
		return CreateItem(item.getClassName(), data, changeLogger);
	}

	protected String getUpdateQuery(String classname, String keyPropertyName,
																	Set<String> properties) {
		String result = "update "
				+ SyncUtils.dbSanitiseName(classname, tablePrefix) + " set ";
		int count = 1;
		for (String property : properties) {
			if (count > 1) {
				result += ",";
			}
			result += SyncUtils.dbSanitiseName(property, fieldPrefix) + " = ?";
			count++;
		}
		result += " where "
				+ SyncUtils.dbSanitiseName(keyPropertyName, fieldPrefix) + " = ?";
		return result;
	}
	
	private Map<String, IPropertyMeta> explodeStructs(IStructMeta cm) throws IonException{
		Map<String, IPropertyMeta> props = new HashMap<String, IPropertyMeta>();
		for (Map.Entry<String, IPropertyMeta> pair : cm.PropertyMetas()
																												 .entrySet()) {
			if (pair.getValue().Type() == MetaPropertyType.STRUCT){
				IStructMeta stm = ((IStructPropertyMeta)pair.getValue()).StructClass();
				while (stm != null){
					for (IPropertyMeta pm1: stm.PropertyMetas().values()){
						props.put(pair.getValue().Name() + structSeparator + pm1.Name(), pm1);
					}
					stm = stm.getAncestor();
				}
			} else
				props.put(pair.getValue().Name(), pair.getValue());
		}
		return props;
	}
	
	private void processRefItems(IItem item, Map<String, Object> values, IChangeLogger logger) throws IonException {
		Map<String, Map<String, Object>> updates = new LinkedHashMap<String, Map<String,Object>>();
		
		for (Map.Entry<String, Object> pair: values.entrySet()){
			if (pair.getKey().contains(".")){
				String ref = pair.getKey().substring(0, pair.getKey().indexOf("."));
				String nm = pair.getKey().substring(pair.getKey().indexOf(".") + 1);
				if (!updates.containsKey(ref))
					updates.put(ref, new LinkedHashMap<String, Object>());
				updates.get(ref).put(nm, pair.getValue());
			}
		}
		
		for (Map.Entry<String, Map<String, Object>> up: updates.entrySet()){
			String rprop = up.getKey();
			IProperty p = item.Property(rprop);
			if ((p != null) && (p instanceof IReferenceProperty)){
				Object v = p.getValue();
				IReferencePropertyMeta rp = (IReferencePropertyMeta)p.Meta();
				if (v != null)
					this.EditItem(rp.ReferencedClass().getName(), v.toString(), up.getValue(), logger);
				else {
					IItem ri = this.CreateItem(rp.ReferencedClass().getName(), up.getValue(), logger);
					values.put(rprop, ri.getItemId());
				}
			}
		}
	}
	
	protected boolean checkChange(IPropertyMeta pm, Object oldVal, Object newVal){
		return !((newVal != null && oldVal != null && 
				(oldVal.equals(newVal) || oldVal.toString().equals(newVal.toString()))) 
				|| (newVal == null && oldVal == null));
	}

	@Override
	public IItem EditItem(String classname, String id, Map<String, Object> data,
												IChangeLogger changeLogger) throws IonException {
		JdbcItem i = (JdbcItem) GetItem(classname, id);
		processValidators(i, data);
		IItem result = null;
		if (i != null && !data.isEmpty()) {
			IStructMeta sm = i.getMetaClass();
			if (sm instanceof IClassMeta) {
				IClassMeta cm = (IClassMeta) sm;

				processRefItems(i,data,changeLogger);
				
				// FIXME Обработка составных ключей
				String keyPropertyName = cm.KeyProperties()[0];

				Map<String, Object> updates = new LinkedHashMap<String, Object>();

				trackUpdate(i, data);

				PreparedStatement statement = null;

				Connection c = null;
				try {
					c = connectionProvider.getConnection();

					IStructMeta ancestor = sm;

					while (ancestor != null) {
						Map<String, Object> local = new LinkedHashMap<String, Object>();
						Map<String, IPropertyMeta> props = explodeStructs(ancestor);
						for (Map.Entry<String, IPropertyMeta> pair : props.entrySet()) 
  						if (pair.getValue().Type() != MetaPropertyType.COLLECTION){							
  							if (data.containsKey(pair.getKey())) {
  								Object v = parseValue(pair.getValue(), data.get(pair.getKey()));
  								Object old = i.Property(pair.getKey()).getValue();
  								if (checkChange(pair.getValue(), old, v))
  									local.put(pair.getKey(), v);
  							}
  						}

						if (!local.isEmpty()) {
							String query = getUpdateQuery(ancestor.getName(),
																						keyPropertyName, local.keySet());
							showSql(query);
							statement = c.prepareStatement(query);
							int count = 1;
							for (Entry<String, Object> entry : local.entrySet()) {
								setParameter(statement, i.Property(entry.getKey()).Meta(),
														 entry.getValue(), count);
								count++;
							}
							statement.setString(count, id);
							statement.executeUpdate();
							statement.close();
						}
						updates.putAll(local);
						ancestor = ancestor.getAncestor();
					}
				} catch (SQLException e) {
					throw new IonException(e);
				} finally {
					try {
						statement.close();
					} catch (Exception e) {
					}
				}
				
				result = getItemFromDb(classname, updates.containsKey(keyPropertyName)?updates.get(keyPropertyName).toString():id);
				if (result != null)
					putIntoCache(result);
				if (changeLogger != null)
					changeLogger.LogChange(ChangelogRecordType.UPDATE,
																 i.getClassName(), 
																 i.getItemId(), updates);
			}
		}
		return result;
	}

	@Override
	public boolean DeleteItem(String classname, String id,
														IChangeLogger changeLogger) throws IonException {
		IStructMeta sm = metaRepository.Get(classname);
		if (sm != null) {
			if (sm instanceof IClassMeta) {
				IStructMeta root = sm;
				while (root.getAncestor() != null)
					root = root.getAncestor();

				IClassMeta actualCm = (IClassMeta) root;
				if (forceCascadeDeletions) {
					ResultSet rs = null;
					String type = null;
					try {
						@SuppressWarnings("serial")
						Map<String, Object> r = new HashMap<String, Object>() {
							{
								put("_type", new PropertyMeta("_type",
																							MetaPropertyType.STRING,
																							(short) 200));
							}
						};
						performSelect(id, (IClassMeta) root, r, null);
						type = r.get("_type").toString();
					} catch (SQLException e) {
						throw new IonException(e);
					} finally {
						if (rs != null) {
							try {
								rs.close();
							} catch (Exception e) {
							}
							try {
								rs.getStatement().close();
							} catch (Exception e) {
							}
						}
					}

					if (type != null)
						actualCm = (IClassMeta) metaRepository.Get(type);
				} else
					actualCm = (IClassMeta) root;

				if (actualCm != null) {
					PreparedStatement statement = null;
					Connection c = connectionProvider.getConnection();
					try {
						while (actualCm != null) {
							// FIXME Обработка составных ключей
							String query = "delete from "
									+ SyncUtils.dbSanitiseName(actualCm.getName(), tablePrefix)
									+ " where "
									+ SyncUtils.dbSanitiseName(actualCm.KeyProperties()[0],
																						 fieldPrefix) + " = ?";
							showSql(query);
							statement = c.prepareStatement(query);
							statement.setString(1, id);
							statement.executeUpdate();
							statement.close();
							actualCm = (IClassMeta) actualCm.getAncestor();
						}					
						
						if (changeLogger != null)
							changeLogger.LogChange(classname, id);
						removeFromCache(classname, id);
					} catch (SQLException e) {
						throw new IonException(e);
					} finally {
						if (statement != null)
							try {
								statement.close();
							} catch (Exception e) {
							}
					}
					return true;
				}
			}
		} else
			throw new IonException("Класс "+classname+" не найден!");
		return false;
	}
/*
	protected void stop(Connection c) throws IonException {
		if (c != null) {
			try {
				c.close();
			} catch (SQLException e) {
				throw new IonException(e);
			}
		}
	}

	protected Connection begin() throws IonException {
		return begin(false);
	}

	protected Connection begin(boolean transaction) throws IonException {
		try {
			Connection c = dataSource.getConnection();
			if (transaction)
				c.setAutoCommit(false);
			return c;
		} catch (SQLException e) {
			throw new IonException(e);
		}
	}

	protected void commit(Connection c) throws IonException {
		try {
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			throw new IonException(e);
		}
	}

	protected void rollback(Connection c) {
		try {
			c.rollback();
			c.setAutoCommit(true);
		} catch (SQLException e) {
		}
	}
*/
	@Override
	public void Put(IItem master, String collection, IItem detail)
																																throws IonException {
		Put(master, collection, detail, null);
	}

	@SuppressWarnings("serial")
	@Override
	public void Put(IItem master, String collection, IItem detail,
									IChangeLogger changeLogger) throws IonException {
		final String br = ((CollectionPropertyMeta) master.Property(collection)
																											.Meta()).BackReference();
		if (br != null && !br.isEmpty()) {
			// FIXME Обработка составных ключей
			final Object ref = master.Property(((IClassMeta) master.getMetaClass()).KeyProperties()[0])
															 .getValue();
			EditItem(detail.getClassName(), detail.getItemId(),
							 new HashMap<String, Object>() {
								 {
									 put(br, ref);
								 }
							 },changeLogger);
		} else {
			String actual = getActualTable(master.getMetaClass(), collection);
			String query = "insert " + SyncUtils.RelationshipName(actual, collection)
					+ " (master, detail) values (?, ?)";
			PreparedStatement statement = null;
			Connection c = null;
			try {
				c = connectionProvider.getConnection();
				statement = c.prepareStatement(query);
				// FIXME Обработка составных ключей
				statement.setObject(1,
														master.Get(((IClassMeta) master.getMetaClass()).KeyProperties()[0]));
				statement.setObject(2,
														detail.Get(((IClassMeta) detail.getMetaClass()).KeyProperties()[0]));
				statement.executeUpdate();
				
				if (changeLogger != null){
					Map<String, Object> updates = new HashMap<String, Object>();
					HashSet<LoggedItemInfo> items = new HashSet<LoggedItemInfo>();
					items.add(new LoggedItemInfo(detail.getClassName(), detail.getItemId()));
					updates.put(collection, items);
					changeLogger.LogChange(ChangelogRecordType.PUT, 
					                       master.getClassName(), master.getItemId(), updates);
				}
			} catch (SQLException e) {
				throw new IonException(e);
			} finally {
				try {
					statement.close();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void Eject(IItem master, String collection, IItem detail)
																																	throws IonException {
		Eject(master, collection, detail, null);
	}

	@SuppressWarnings("serial")
	@Override
	public void Eject(IItem master, String collection, IItem detail,
										IChangeLogger changeLogger) throws IonException {
		final String br = ((CollectionPropertyMeta) master.Property(collection)
																											.Meta()).BackReference();
		if (br != null && !br.isEmpty()) {
			EditItem(detail.getClassName(), detail.getItemId(),
							 new HashMap<String, Object>() {
								 {
									 put(br, null);
								 }
							 }, changeLogger);
		} else {
			String actual = getActualTable(master.getMetaClass(), collection);
			String tbl = SyncUtils.RelationshipName(actual, collection);
			String query = "delete from " + tbl + " where " + tbl + ".master = ? and " + tbl + ".detail = ?";
			PreparedStatement statement = null;
			Connection c = null;
			try {
				c = connectionProvider.getConnection();
				statement = c.prepareStatement(query);
				// FIXME Обработка составных ключей
				statement.setObject(1,
														master.Get(((IClassMeta) master.getMetaClass()).KeyProperties()[0]));
				statement.setObject(2,
														detail.Get(((IClassMeta) detail.getMetaClass()).KeyProperties()[0]));
				statement.executeUpdate();
				
				if (changeLogger != null){
					Map<String, Object> updates = new HashMap<String, Object>();
					HashSet<LoggedItemInfo> items = new HashSet<LoggedItemInfo>();
					items.add(new LoggedItemInfo(detail.getClassName(), detail.getItemId()));
					updates.put(collection, items);
					changeLogger.LogChange(ChangelogRecordType.EJECT, 
					                       master.getClassName(), master.getItemId(), updates);
				}				
			} catch (SQLException e) {
				throw new IonException(e);
			} finally {
				try {
					statement.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	private Map<String, String> formAssocHooks(Object m, String collection, String malias) throws IonException {
		Map<String, String> result = new HashMap<String, String>();
		IClassMeta master = null;
		String masterKey = null;
		if (m instanceof IItem){
			if (((IItem)m).getMetaClass() instanceof IClassMeta){
				master = (IClassMeta)((IItem)m).getMetaClass();
				masterKey = "?";
			}
		} else if (m instanceof IClassMeta) {
			master = (IClassMeta)m;
		} else
			throw new IonException("Некорректное значение параметра!");
		
		String masterTn = (malias != null)?malias:SyncUtils.dbSanitiseName(master.getName(),tablePrefix);
		// FIXME Обработка составных ключей
		if (m instanceof IClassMeta)
			masterKey = masterTn + "." + SyncUtils.dbSanitiseName(master.KeyProperties()[0],fieldPrefix);
		
		String br = ((CollectionPropertyMeta) master.PropertyMeta(collection)).BackReference();
		String bc = ((CollectionPropertyMeta) master.PropertyMeta(collection)).BackCollection();
		IClassMeta dcm = (IClassMeta) ((CollectionPropertyMeta) master.PropertyMeta(collection)).ItemsClass();
		if (br != null && !br.isEmpty()) {
			result.put("where", SyncUtils.dbSanitiseName(dcm.getName(),tablePrefix) + "." 
												+ SyncUtils.dbSanitiseName(br,fieldPrefix) + " = " + masterKey);
		} else if (bc != null && !bc.isEmpty()) {
			String tn = SyncUtils.RelationshipName(dcm.getName(), dcm.PropertyMeta(bc).Name());
			// FIXME Обработка составных ключей
			result.put("join", " inner join "
							+ tn
							+ " on "
							+ tn
							+ ".master = "
							+ SyncUtils.dbSanitiseName(dcm.getName(),tablePrefix)
							+ "."
							+ SyncUtils.dbSanitiseName(dcm.KeyProperties()[0],fieldPrefix)
							+ " and "
							+ tn + ".detail = " + masterKey);
		} else {
			String actual = getActualTable(master, collection);
			String tn = SyncUtils.RelationshipName(actual, collection);
			// FIXME Обработка составных ключей
			result.put("join", " inner join "
							+ tn
							+ " on "
							+ tn
							+ ".detail = "
							+ SyncUtils.dbSanitiseName(dcm.getName(),tablePrefix)
							+ "."
							+ SyncUtils.dbSanitiseName(dcm.KeyProperties()[0],fieldPrefix)
							+ " and "
							+ tn + ".master = " + masterKey);
		}		
		return result;
	}	

	@SuppressWarnings("serial")
	@Override
	public Collection<IItem> GetAssociationsList(IItem master, String collection,
																							 ListOptions options)
																																	 throws IonException {
		String br = ((CollectionPropertyMeta) master.Property(collection).Meta()).BackReference();
		if (br != null && !br.isEmpty()) {
			// FIXME Обработка составных ключей
			Object ref = master.Property(((IClassMeta) master.getMetaClass()).KeyProperties()[0])
												 .getValue();
			try {
				options.Filter().add(new Condition(br, ConditionType.EQUAL, ref));
			} catch (Exception e) {
				throw new IonException(e);
			}
			return GetList(((CollectionPropertyMeta) master.Property(collection)
																										 .Meta()).ItemsClass()
																														 .getName(),
										 options);
		} else {
			IClassMeta dcm = (IClassMeta) ((CollectionPropertyMeta) master.Property(collection).Meta()).ItemsClass();
			// FIXME Обработка составных ключей
			final Object masterid = master.Get(((IClassMeta) master.getMetaClass()).KeyProperties()[0]);
			
			Map<String, String> hooks = formAssocHooks(master, collection, null);
			
			return getItemsList(GetItem(dcm.getName()),
													options, hooks.get("join"),
													new ArrayList<Object>() {
														{
															add(masterid);
														}
													}, null);			
		}
	}
	
	@Override
	public Collection<IItem> GetAssociationsList(IItem master, String collection)
																																							 throws IonException {
		return GetAssociationsList(master, collection, new ListOptions());
	}

	@SuppressWarnings("serial")
	@Override
	public Iterator<IItem> GetAssociationsIterator(IItem master,
																								 String collection,
																								 ListOptions options)
																																		 throws IonException {
		String br = ((CollectionPropertyMeta) master.Property(collection).Meta()).BackReference();
		String bc = ((CollectionPropertyMeta) master.Property(collection).Meta()).BackCollection();
		IClassMeta dcm = (IClassMeta) ((CollectionPropertyMeta) master.Property(collection).Meta()).ItemsClass();
		if (br != null && !br.isEmpty()) {
			// FIXME Обработка составных ключей
			Object ref = master.Property(((IClassMeta) master.getMetaClass()).KeyProperties()[0])
												 .getValue();
			try {
				options.Filter().add(new Condition(br, ConditionType.EQUAL, ref));
			} catch (Exception e) {
				throw new IonException(e);
			}
			return GetIterator(((CollectionPropertyMeta) master.Property(collection)
																												 .Meta()).ItemsClass()
																																 .getName(),
												 options);
		} else if (bc != null && !bc.isEmpty()) {
			// String actual = getActualTable(master.getMetaClass(), collection);
			String tn = SyncUtils.RelationshipName(dcm.getName(), dcm.PropertyMeta(bc).Name());			
			// FIXME Обработка составных ключей
			final Object masterid = master.Get(((IClassMeta) master.getMetaClass()).KeyProperties()[0]);
			// FIXME Обработка составных ключей
			return getItemsIterator(GetItem(dcm.getName()),
													options,
													" inner join "
															+ tn
															+ " on "
															+ tn
															+ ".master = "
															+ SyncUtils.dbSanitiseName(dcm.getName(),
																												 tablePrefix)
															+ "."
															+ SyncUtils.dbSanitiseName(dcm.KeyProperties()[0],
																												 fieldPrefix) + " and "
															+ tn + ".detail = ?", new ArrayList<Object>() {
														{
															add(masterid);
														}
													}, null);
		} else {
			String actual = getActualTable(master.getMetaClass(), collection);
			String tn = SyncUtils.RelationshipName(actual, collection);
			// FIXME Обработка составных ключей
			final Object masterid = master.Get(((IClassMeta) master.getMetaClass()).KeyProperties()[0]);
			// FIXME Обработка составных ключей
			return getItemsIterator(GetItem(dcm.getName()),
															options,
															" inner join "
																	+ tn
																	+ " on "
																	+ tn
																	+ ".detail = "
																	+ SyncUtils.dbSanitiseName(dcm.getName(),
																														 tablePrefix)
																	+ "."
																	+ SyncUtils.dbSanitiseName(dcm.KeyProperties()[0],
																														 fieldPrefix)
																	+ " and " + tn + ".master = ?",
															new ArrayList<Object>() {
																{
																	add(masterid);
																}
															}, null);
		}
	}

	@Override
	public Iterator<IItem> GetAssociationsIterator(IItem master, String collection)
																																								 throws IonException {
		return GetAssociationsIterator(master, collection, new ListOptions());
	}

	@SuppressWarnings("serial")
	@Override
	public long GetAssociationsCount(IItem master, String collection,
																	 ListOptions options) throws IonException {
		String br = ((CollectionPropertyMeta) master.Property(collection).Meta()).BackReference();
		if (br != null && !br.isEmpty()) {
			// FIXME Обработка составных ключей
			Object ref = master.Property(((IClassMeta) master.getMetaClass()).KeyProperties()[0])
												 .getValue();
			try {
				options.Filter().add(new Condition(br, ConditionType.EQUAL, ref));
			} catch (Exception e) {
				throw new IonException(e);
			}
			return GetCount(((CollectionPropertyMeta) master.Property(collection)
																											.Meta()).ItemsClass()
																															.getName(),
											options);
		} else {
			String actual = getActualTable(master.getMetaClass(), collection);
			String tn = SyncUtils.RelationshipName(actual, collection);
			IClassMeta dcm = (IClassMeta) ((CollectionPropertyMeta) master.Property(collection)
																																		.Meta()).ItemsClass();
			// FIXME Обработка составных ключей
			final Object masterid = master.Get(((IClassMeta) master.getMetaClass()).KeyProperties()[0]);
			// FIXME Обработка составных ключей
			return getItemsCount(GetItem(dcm.getName()),
													 options,
													 " inner join "
															 + tn
															 + " on "
															 + tn
															 + ".detail = "
															 + SyncUtils.dbSanitiseName(dcm.getName(),
																													tablePrefix)
															 + "."
															 + SyncUtils.dbSanitiseName(dcm.KeyProperties()[0],
																													fieldPrefix)
															 + " and " + tn + ".master = ?",
													 new ArrayList<Object>() {
														 {
															 add(masterid);
														 }
													 }, null);
		}
	}

	@Override
	public long GetAssociationsCount(IItem master, String collection)
																																	 throws IonException {
		return GetAssociationsCount(master, collection, new ListOptions());
	}

	private String[] parseSelectionQuery(String query,
																			 final Map<String, Integer> parametersIndexes,
																			 final int offset) {
		final Map<String, String> sqlQueryParts = new HashMap<String, String>();
		SelectionLexer lexer = new SelectionLexer(new ANTLRInputStream(query));
		SelectionParser parser = new SelectionParser(new CommonTokenStream(lexer));

		parser.addParseListener(new SelectionBaseListener() {
			String classname = null;
			String alias = null;

			private String getAttributeString(String attr) {

				if (attr.equals("*")) {
					return "*";
				} else if (attr.contains(".")) {
					String[] attrParams = attr.split("\\.");
					if (attrParams[0].equals(alias)) {
						return alias + "."
								+ SyncUtils.dbSanitiseName(attrParams[1], fieldPrefix);
					} else {
						return SyncUtils.dbSanitiseName(attrParams[0], tablePrefix) + "."
								+ SyncUtils.dbSanitiseName(attrParams[1], fieldPrefix);
					}
				}
				return SyncUtils.dbSanitiseName(classname, tablePrefix) + "."
						+ SyncUtils.dbSanitiseName(attr, fieldPrefix);
			}

			@Override
			public void exitStatement(StatementContext ctx) {
				try {
					List<QueryContext> queries = ctx.query();
					for (QueryContext query : queries) {
						SelectClauseContext select = query.selectClause();
						FromClauseContext from = query.fromClause();
						WhereClauseContext where = query.whereClause();

						if (from != null) {
							classname = from.className.getText();
							IStructMeta sm = metaRepository.Get(classname);
							if (sm != null) {
								if (sm instanceof IClassMeta) {
									String fromPart = "FROM "
											+ SyncUtils.dbSanitiseName(classname, tablePrefix);
									if (from.classNameAlias != null) {
										fromPart += " AS " + from.classNameAlias.getText();
										alias = from.classNameAlias.getText();
									}
									fromPart += getInnerJoins((IClassMeta) sm, null);
									sqlQueryParts.put("classname", classname);
									sqlQueryParts.put("from", fromPart);
								}
							}
						}

						if (select != null) {
							String selectPart = "SELECT ";
							int count = 1;
							for (SelectAttributeContext sa : select.selectAttribute()) {
								if (count > 1)
									selectPart += ",";
								selectPart += getAttributeString(sa.attribute.getText());
								count++;
							}
							sqlQueryParts.put("select", selectPart);
						}

						if (where != null) {
							String wherePart = "WHERE ";
							int count2 = 1 + offset;
							for (ConditionsContext c : where.conditions()) {
								if (count2 > 1)
									wherePart += " AND ";
								wherePart += getAttributeString(c.condition().condAttr.getText())
										+ " " + c.condition().condType.getText() + " ";
								if (c.condition().condValue != null) {
									wherePart += "?";
									parametersIndexes.put(c.condition().condValue.getText(),
																				count2);
								} else if (c.condition().values() != null) {
									wherePart += "(";
									int innerCount = 0;
									for (ValuesContext value : c.condition().values()) {
										if (value.condValue != null) {
											if (innerCount > 0)
												wherePart += ",";
											wherePart += "?";
											parametersIndexes.put(value.condValue.getText(), count2
													+ innerCount);
											innerCount++;
										} else if (value.query() != null) {
											wherePart += parseSelectionQuery(value.query().getText(),
																											 parametersIndexes,
																											 count2 + innerCount - 1);
										}
									}
									wherePart += ")";
								}
								count2++;
							}
							sqlQueryParts.put("where", wherePart);
						}

					}
				} catch (IonException e) {
					e.printStackTrace();
				}
			}
		});
		parser.statement();
		String result = "";
		if (sqlQueryParts.get("select") != null) {
			result += sqlQueryParts.get("select") + " ";
		} else {
			result += "SELECT * ";
		}
		result += sqlQueryParts.get("from");
		if (sqlQueryParts.get("where") != null)
			result += " " + sqlQueryParts.get("where");
		return new String[] { sqlQueryParts.get("classname"), result };
	}

	@Override
	public Collection<IItem> FetchList(String query,
																		 Map<String, Object> parameters)
																																		throws IonException {
		final Map<String, Integer> parametersIndexes = new HashMap<String, Integer>();
		String[] sqlParams = parseSelectionQuery(query, parametersIndexes, 0);
		String classname = sqlParams[0];
		String sqlQuery = sqlParams[1];
		Map<String, IItem> result = null;
		try {
			Connection c = connectionProvider.getConnection();
  		PreparedStatement statement = c.prepareStatement(sqlQuery);
  		for (Entry<String, Object> param : parameters.entrySet()) {
  			if (parametersIndexes.containsKey(param.getKey())) {
  				statement.setObject(parametersIndexes.get(param.getKey()),
  														param.getValue());
  			}
  		}
  		IItem dummy = GetItem(classname);
  		ResultSet rs = statement.executeQuery();
  		Map<String, Collection<Object>> itemsToEnrich = new HashMap<String, Collection<Object>>();
  		result = produceItems(rs, dummy, itemsToEnrich, null);
  		produceEnrichedItems(rs, dummy, itemsToEnrich, result);
		} catch (SQLException e) {
			throw new IonException(e);
		}
		return result.values();
	}

	@Override
	public Iterator<IItem> FetchIterator(String query,
																			 Map<String, Object> parameters)
																																			throws IonException {
		final Map<String, Integer> parametersIndexes = new HashMap<String, Integer>();
		String[] sqlParams = parseSelectionQuery(query, parametersIndexes, 0);
		String classname = sqlParams[0];
		String sqlQuery = sqlParams[1];
		IStructMeta sm = metaRepository.Get(classname);
		if (sm != null) {
			if (sm instanceof IClassMeta) {
				ResultSet rs = null;
				try {
					Connection c = connectionProvider.getConnection();
					PreparedStatement statement = c.prepareStatement(sqlQuery);
					for (Entry<String, Object> param : parameters.entrySet()) {
						if (parametersIndexes.containsKey(param.getKey())) {
							statement.setObject(parametersIndexes.get(param.getKey()),
																param.getValue());
						}
					}
					rs = statement.executeQuery();
				} catch (SQLException e) {
					throw new IonException(e); 
				}
				return new JdbcListIterator(rs, (IClassMeta) sm, this, null);
			}
		}
		return null;
	}
}
