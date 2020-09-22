package ion.web.app.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.DACPermission;
import ion.core.FilterOption;
import ion.core.IAuthContext;
import ion.core.IClassMeta;
import ion.core.ICollectionPropertyMeta;
import ion.core.IConditionalSelectionProvider;
import ion.core.IDataRepository;
import ion.core.IItem;
import ion.core.IItemSelectionProvider;
import ion.core.IMetaRepository;
import ion.core.IProperty;
import ion.core.IPropertyMeta;
import ion.core.IQuerySelectionProvider;
import ion.core.IReferenceProperty;
import ion.core.IReferencePropertyMeta;
import ion.core.ISelectionProvider;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IWorkflowProvider;
import ion.core.IWorkflowState;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.MetaPropertyType;
import ion.core.Operation;
import ion.core.OperationType;
import ion.core.data.CollectionProperty;
import ion.core.data.ReferenceProperty;
import ion.core.data.SimpleSelectionProvider;
import ion.core.logging.ChangelogRecordType;
import ion.core.logging.IChangeLogger;
import ion.db.search.IFulltextSearchAdapter;
import ion.viewmodel.navigation.INode;
import ion.web.app.jstl.EvalUtils;
import ion.web.app.jstl.Urls;
import ion.web.app.util.AclManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IonWebAppService {
	
	@Autowired(required=false)
	protected AclManager aclManager;
	
	@Autowired
	protected IDataRepository data;
	
	@Autowired(required=false)
	protected IWorkflowProvider workflow;
	
	@Autowired
	protected IMetaRepository meta;
	
	@Autowired(required=false)
	protected IChangeLogger changeLogger;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired(required=false)
	protected IFulltextSearchAdapter fullTextSearchAdapter;
	
	@Autowired
	protected IAuthContext authContext;
	
	
	public IonWebAppService() {
	}
	
	private String serializeStructProperty(IStructPropertyMeta pm, IItem item) throws IonException {
		IStructMeta m = ((IStructPropertyMeta)pm).StructClass();
		String result = "";
		while (m != null){
			for (IPropertyMeta pm1: m.PropertyMetas().values())
				if (pm.Type() == MetaPropertyType.STRUCT)
					result = result + serializeStructProperty((IStructPropertyMeta)pm1, item);
				else {
					Object v = item.Get(pm.Name());
					String sv = (v == null)?"":("|" + v.toString());
					if (!sv.isEmpty())
						result += sv;
				}
			m = m.getAncestor();
		}
		return result;
	}
	
	protected String toFullText(IItem item) throws IonException {
		String result = "";
		for (IPropertyMeta pm : item.getMetaClass().PropertyMetas().values())
			if (pm.IndexSearch()){ 
				if (pm.Type().equals(MetaPropertyType.STRUCT)){
					result += serializeStructProperty((IStructPropertyMeta)pm, item);
				} else if (pm.Type().equals(MetaPropertyType.REFERENCE))
					result += toFullText(((ReferenceProperty)item.Property(pm.Name())).getReferedItem());
				else if (!pm.Type().equals(MetaPropertyType.COLLECTION)){
					Object v = item.Get(pm.Name());
					String sv = (v == null)?"":("|" + v.toString());
					if (!sv.isEmpty())
						result += sv;
				}
			}
		if (!result.isEmpty())
			result += "|" + item.getMetaClass().getCaption();
		return result;
	}
	
	protected boolean needLogging(ChangelogRecordType action, String classname, String id, Map<String, Object> values) throws IonException {
		return changeLogger != null;
	}
	
	protected IItem performCreate(String className, Map<String, Object> values) throws IonException {
		if(aclManager!=null){
			DACPermission[] permissions = {DACPermission.USE};
			if(!aclManager.checkClassAccess(className, permissions)){
				throw new IonException("Ошибка доступа");
			};
		}
		IItem result = data.CreateItem(className, values, needLogging(ChangelogRecordType.CREATION, className, null, values)?changeLogger:null);
		if (fullTextSearchAdapter != null && result != null)
			fullTextSearchAdapter.put(result.getClassName() + "@" + result.getItemId(), toFullText(result));
		return result;		
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem Create(String className, Map<String, Object> values) throws IonException {
		return performCreate(className, values);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem Create(String className, Map<String, Object> values, int initDepth) throws IonException {
		IItem result = performCreate(className, values);
		if (initDepth > 0)
			initItem(result, true, initDepth);
		return result;
	}
	
	private IItem checkWorkFlowPermissions(IItem item, String className, String id, DACPermission permission) throws IonException {
		if(workflow != null){
			if (item == null)
				item = data.GetItem(className, id);
			IWorkflowState state = workflow.GetState(item);
			if (state != null && !state.checkItemPermission(authContext.CurrentUser(), permission))
				throw new IonException("Ошибка доступа");
		}
		return item;
	}
		
	protected IItem performEdit(String className, String id, Map<String, Object> values) throws IonException {
		if(aclManager!=null){
			DACPermission[] permissions = {DACPermission.WRITE};
			if(!aclManager.checkObjectAccess(className, id, permissions)){
				if(!aclManager.checkClassAccess(className, permissions)){
					throw new IonException("Ошибка доступа");				
				}
			};
		}
		
		checkWorkFlowPermissions(null, className, id, DACPermission.WRITE);
		
		IItem item = data.EditItem(className, id, values, needLogging(ChangelogRecordType.UPDATE, className, id, values)?changeLogger:null);
		if (fullTextSearchAdapter != null)
			fullTextSearchAdapter.put(item.getClassName() + "@" + item.getItemId(), toFullText(item));
		return item;		
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem PerformTransition(String actionName, String className, String id, Map<String, Object> values, int initDepth) throws IonException {
		if (!actionName.contains("."))
			return null;
		if(workflow != null){
			// TODO: Проверка доступа
			IItem item = data.GetItem(className, id);
			String[] tn = actionName.split("\\.");
			
			workflow.ProcessTransition(item, tn[0], tn[1]);
			return performEdit(className, id, values);			
		}		
		return null;
	}	
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem Edit(String className, String id, Map<String, Object> values) throws IonException {
		return performEdit(className, id, values);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem Edit(String className, String id, Map<String, Object> values, int initDepth) throws IonException {
		IItem item = performEdit(className, id, values);
		if (initDepth > 0)
			initItem(item, true, initDepth);
		return item;
	}	
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public void Delete(String className, String id) throws IonException {
		if(aclManager!=null){
			DACPermission[] permissions = {DACPermission.DELETE};
			if(!aclManager.checkObjectAccess(className, id, permissions)){
				if(!aclManager.checkClassAccess(className, permissions)){
					throw new IonException("Ошибка доступа");				
				}
			};
		}
		data.DeleteItem(className, id, needLogging(ChangelogRecordType.DELETION, className, id, null)?changeLogger:null);
		if (fullTextSearchAdapter != null)
			fullTextSearchAdapter.remove(className + "@" + id);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem getDummy(String className) throws IonException {
		IItem item = data.GetItem(className);
		initItem(item, true);
		return item;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem getItem(String className, String id) throws IonException {
		if(aclManager!=null){
			DACPermission[] permissions = {DACPermission.READ};
			if(!aclManager.checkObjectAccess(className, id, permissions)){
				if(!aclManager.checkClassAccess(className, permissions)){
					throw new IonException("Ошибка доступа");				
				}
			};
		}
		
		IItem result = data.GetItem(className, id);
		
		if (result != null)
			checkWorkFlowPermissions(result, className, id, DACPermission.READ);
		
		/*
		 if (result != null)
			initItem(result, true);
		*/	
		return result;
	}
	
	private void eager(IItem item, boolean load_sl, int level, int depth) throws IonException {
		if (level > depth)
			return;
		for (IProperty p: item.getProperties().values()){
			if (p instanceof IReferenceProperty){
				//if (((IReferencePropertyMeta)p.Meta()).)
				IItem ri = ((IReferenceProperty) p).getReferedItem();
				if (ri != null)
					eager(ri, false, level + 1, depth);
			} /*else if (p instanceof ICollectionProperty){
				IItem[] items = ((ICollectionProperty) p).getItems();
				if (items != null)
  				for (IItem ci: items)
  					eager(ci, load_sl, level + 1, depth);
			}*/
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public void initItem(IItem item, boolean load_sl) throws IonException {
		eager(item,load_sl, 0, 3);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public void initItem(IItem item, boolean load_sl, int depth) throws IonException {
		eager(item,load_sl, 0, depth);
	}
	
	private void formSelectionDependencies(IProperty p, Map<String, Set<String>> st) throws IonException {
		java.util.List<String> dependencies = new LinkedList<String>();
		ISelectionProvider sp = p.Meta().Selection();
		
		if (sp instanceof IConditionalSelectionProvider) {
			IConditionalSelectionProvider csp = (IConditionalSelectionProvider)sp; 
			dependencies.addAll(csp.Dependencies());
		} else if (sp instanceof IQuerySelectionProvider) {
			IQuerySelectionProvider qsp = (IQuerySelectionProvider)sp;
			dependencies.addAll(qsp.Dependencies());
		} else if (sp instanceof IItemSelectionProvider) {
			IItemSelectionProvider isp = (IItemSelectionProvider)sp;
			dependencies.addAll(isp.Dependencies());
		}
		
		if (!dependencies.isEmpty()){
			for (String dep: dependencies) {
				Set<String> dependents = null;
				if (!st.containsKey(dep)){
					dependents = new HashSet<String>();
					st.put(dep, dependents);
				} else
					dependents = st.get(dep);
				dependents.add(p.getName());
			}
		}		
	}
	
	protected Map<String, Object> convItemToMap(IItem item, boolean drill) throws IonException {
		if (item != null){
			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, Set<String>> st = new HashMap<String, Set<String>>(); 
			data.put("__selection_triggers", st);
			Collection<Map<String,Object>> collection;
			for (IProperty p: item.getProperties().values()){
				switch (p.getType()){
					case COLLECTION:{
						if (drill){
							IItem[] items = ((CollectionProperty)p).getItems();
							collection = new LinkedList<Map<String,Object>>();
							if(items!=null){
								for (IItem i: items)
									collection.add(convItemToMap(i,false));
							}
							data.put(p.getName(), collection);
						}
					}break;
					case REFERENCE:{
						if (drill){
							IItem ri = ((ReferenceProperty)p).getReferedItem();
							String[] refInfo = new String[3];
							if(ri != null){
								refInfo[0] = ri.getClassName();
								refInfo[1] = ri.getItemId();
								refInfo[2] = ri.toString();
							}
							data.put(p.getName(), (p.getValue() == null)?null:p.getValue().toString());
							data.put(p.getName()+"__ref", refInfo);
							ISelectionProvider sp = p.Meta().Selection();
							
							formSelectionDependencies(p, st);
							
							if (sp instanceof IConditionalSelectionProvider || sp instanceof SimpleSelectionProvider){
								Map<String, String> sel = propertySelection(item, item.getClassName(), item.getItemId(), p.getName());
								if (sel != null && !sel.isEmpty()){
									data.put(p.getName()+"__str", (p.getValue() != null && sel.containsKey(p.getValue().toString()))?sel.get(p.getValue().toString()):"");
									data.put(p.getName()+"__sel", sel);
								}								
							}							
						}
					}break;
					case FILE:
					case IMAGE: data.put(p.getName(), p.getValue());data.put(p.getName()+"__url", Urls.fileUrl((String)p.getValue()));break;
					case DATETIME: data.put(p.getName(), EvalUtils.dateToStr((Date)p.getValue()));break;
					default:{
							data.put(p.getName(), p.getValue());
							Map<String, String> sel = propertySelection(item, item.getClassName(), item.getItemId(), p.getName());
							
							formSelectionDependencies(p, st);
														
							if (sel != null && !sel.isEmpty()){
								data.put(p.getName()+"__str", (p.getValue() != null && sel.containsKey(p.getValue().toString()))?sel.get(p.getValue().toString()):"");
								data.put(p.getName()+"__sel", sel);
							}
					}break;
				}
			}
			if (item.getMetaClass() instanceof IClassMeta){
				String[] itemInfo = {item.getClassName(),item.getItemId()};
				data.put("itemInfo", itemInfo);
			}
			data.put("class", item.getMetaClass().getCaption());
			data.put("itemToString", item.toString());
			return data;
		}
		return null;		
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Map<String, Object> ItemToMap(IItem item, boolean drill) throws IonException {
		return convItemToMap(item, drill);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Map<String, Object> ItemToMap(IItem item) throws IonException {
		return convItemToMap(item, true);
	}

	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Collection<IItem> List(String className, ListOptions lo) throws IonException {
		return List(className, lo, true);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Collection<IItem> List(String className, ListOptions lo, boolean checkClassAccess) throws IonException {
		if(aclManager!=null && checkClassAccess){
			DACPermission[] permissions = {DACPermission.READ};
			java.util.List<String> grantedClasses = aclManager.getClassAccessList(permissions);
			java.util.List<String> grantedObjects = aclManager.getObjectAccessList(permissions);
			java.util.List<String> filterClasses = new LinkedList<String>();
			Map<String,String> filterIds = new HashMap<String,String>();
			
			for(String cl: grantedClasses){
				IStructMeta clMeta = meta.Get(cl);
				if(clMeta!=null){
					if(clMeta.checkAncestor(className)!=null){
						filterClasses.add(cl);
					}
				}
			}
			
			for(String ob: grantedObjects){
				String[] parts = ob.split("@");
				if(!filterClasses.contains(parts[0])){
					filterIds.put(parts[0], parts[1]);
				}
			}
			
			if(filterClasses.isEmpty() && filterIds.isEmpty()){
				lo.SetTotalCount((long)0);
				return new LinkedList<IItem>();
				//throw new IonException("Ошибка доступа");
			}
			
			String[] classesToFilter = filterClasses.toArray(new String[filterClasses.size()]);
			try {
				java.util.List<FilterOption> fo = new LinkedList<FilterOption>();
				fo.add(new Condition("class",ConditionType.IN,classesToFilter));
				for(Entry<String,String> e: filterIds.entrySet()){
					FilterOption[] filterOptionsArray = {
							new Condition("class",ConditionType.EQUAL,e.getKey()),
							new Condition("id",ConditionType.EQUAL,e.getValue())
					};
					fo.add(new Operation(OperationType.AND,filterOptionsArray));
				}
				lo.Filter().add(new Operation(OperationType.OR, fo.toArray(new FilterOption[fo.size()])));
			} catch (Exception e) {
				throw new IonException(e);
			}
		}
		Collection<IItem> result = data.GetList(className,lo);
		/*
		 for (IItem i: result)
			initItem(i, false);
		*/	
		return result;
	}
	
	private IItem fillSelectionList(IItem item, String className, String id, IPropertyMeta pm, 
	                                ISelectionProvider sp, Map<String, String> sl) throws IonException {
		IItem result = item;
		if (sp instanceof IConditionalSelectionProvider){
			if (result == null){
				if (id == null)
					result = getDummy(className);
				else
					result = getItem(className, id);
			}
			Map<String, String> sl1 = ((IConditionalSelectionProvider)sp).SelectList(result);
			if (sl1 != null)
				sl.putAll(sl1);			
		} else if (sp instanceof IQuerySelectionProvider){
			if (result == null){
				if (id == null)
					result = getDummy(className);
				else
					result = getItem(className, id);				
			}
			Collection<IItem> items = data.FetchList(((IQuerySelectionProvider)sp).getQuery(), ((IQuerySelectionProvider)sp).getParameters(result));
			for (IItem i: items)
				sl.put(i.getItemId(), i.toString());
		} else if (sp instanceof IItemSelectionProvider){
			String cn = null;
			if (pm instanceof IReferencePropertyMeta)
				cn = ((IReferencePropertyMeta)pm).ReferencedClass().getName();
			if (pm instanceof ICollectionPropertyMeta)
				cn = ((ICollectionPropertyMeta)pm).ItemsClass().getName();

			if (cn != null){
				Collection<IItem> items = data.GetList(cn, new ListOptions(((IItemSelectionProvider)sp).getConditions()));
				for (IItem i: items)
					sl.put(i.getItemId(), i.toString());
			}
		} else {
			Map<String, String> sl1 = sp.SelectList();
			if (sl1 != null)
				sl.putAll(sl1);
		}
		return result;
	}
	
	private Map<String, String> propertySelection(IItem item, String className, String id, String name) throws IonException {
		IStructMeta sm = meta.Get(className);
		IPropertyMeta pm = sm.PropertyMeta(name);
		Map<String, String> result = new LinkedHashMap<String, String>(); 
		
		IItem titem = null;
		
		if (pm != null){
			ISelectionProvider sp = pm.Selection();
			if (sp != null) {
				titem = fillSelectionList(item,className, id, pm, sp, result);
			}
			if (workflow != null && id != null){
				if (workflow.HasWorkflows(sm)){
  				if (titem == null)
  					titem = getItem(className, id);
  				IWorkflowState wfstate = workflow.GetState(titem);
  				if (wfstate != null){
  					sp = wfstate.getSelectionProvider(name, authContext.CurrentUser());
  					if (sp != null)
  						fillSelectionList(titem, className, id, pm, sp, result);
  				}
				}
			}
		}
		if (result.isEmpty())
			return null;
		return result;
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Map<String, String> getPropertySelection(String className, String id, String name) throws IonException {
		return propertySelection(null, className, id, name);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Map<String, Map<String, String>> getPropertiesSelection(String className, String id, Map<String, Object> data, String[] properties) throws IonException {
		Map<String, Map<String, String>> result = new LinkedHashMap<String, Map<String, String>>();
		IItem item = null;
		if (id != null)
			item = this.getItem(className, id);
		else
			item = this.getDummy(className);
		
		if (item == null)
			return result;
		
		for (Map.Entry<String, Object> kv: data.entrySet())
			item.Set(kv.getKey(), kv.getValue());
		
		IWorkflowState wfstate = null;
		if (workflow != null){
			if (workflow.HasWorkflows(item.getMetaClass())){
					wfstate = workflow.GetState(item);
			}
		}
		
		if (properties == null)
			properties = item.getProperties().keySet().toArray(new String[item.getProperties().keySet().size()]);
		
		for (String p: properties){
			IProperty prop = item.Property(p);
			if (prop != null && !prop.Meta().Type().equals(MetaPropertyType.COLLECTION)){
				ISelectionProvider sp = prop.Meta().Selection();
				Map<String, String> selection = null;
				
				if (sp != null && !(sp instanceof IItemSelectionProvider)){
					selection = new LinkedHashMap<String, String>();
					item = fillSelectionList(item, item.getClassName(), item.getItemId(), prop.Meta(), sp, selection);
				}
				
				if (wfstate != null){
	  			sp = wfstate.getSelectionProvider(prop.getName(), authContext.CurrentUser());
	  			if (sp != null){
	  				if (selection == null)
	  					selection = new LinkedHashMap<String, String>();
	  				item = fillSelectionList(item, item.getClassName(), item.getItemId(), prop.Meta(), sp, selection);
	  			}
				}
	
				if (selection != null)
					result.put(prop.getName(), selection);
			}
		}	
		return result;		
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Map<String, Map<String, String>> getPropertiesSelection(String className, String id) throws IonException {
		return this.getPropertiesSelection(className, id, new HashMap<String, Object>(), null);
	}	
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Collection<IItem> getPropertySelectionItems(IItem item, String name, ListOptions lo) throws IonException {
		if(aclManager!=null){
			DACPermission[] permissions = {DACPermission.READ};
			if (!aclManager.checkObjectAccess(item.getClassName(), item.getItemId(), permissions)) {
				if (!aclManager.checkClassAccess(item.getClassName(), permissions)) {
					throw new IonException("Ошибка доступа");				
				}
			}
		}			
		
		IPropertyMeta pm = item.getMetaClass().PropertyMeta(name);
		if (pm != null){
			ISelectionProvider sp = pm.Selection();
			
			if (workflow != null){
				IWorkflowState wfstate = workflow.GetState(item);
				if (wfstate != null)
					sp = wfstate.getSelectionProvider(name, authContext.CurrentUser());
			}
			
			if (sp instanceof IItemSelectionProvider){
				String cn = null;
				if (pm instanceof IReferencePropertyMeta)
					cn = ((IReferencePropertyMeta)pm).ReferencedClass().getName();
				if (pm instanceof ICollectionPropertyMeta)
					cn = ((ICollectionPropertyMeta)pm).ItemsClass().getName();
				
				
				lo.Filter().addAll(((IItemSelectionProvider)sp).getConditions());
				lo.Sorting().addAll(((IItemSelectionProvider)sp).getSorting());
				return List(cn, lo, false);
			}
		}
		return new LinkedList<IItem>();
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public long getPropertySelectionItemCount(IItem item, String name, ListOptions lo) throws IonException {
		IProperty p = item.Property(name);
		if (p != null){
			ISelectionProvider sp = p.Meta().Selection();
			
			if (workflow != null){
				IWorkflowState wfstate = workflow.GetState(item);
				if (wfstate != null)
					sp = wfstate.getSelectionProvider(name, authContext.CurrentUser());
			}
			
			if (sp instanceof IItemSelectionProvider){
				String cn = null;
				IPropertyMeta pm = p.Meta();
				if (pm instanceof IReferencePropertyMeta)
					cn = ((IReferencePropertyMeta)pm).ReferencedClass().getName();
				if (pm instanceof ICollectionPropertyMeta)
					cn = ((ICollectionPropertyMeta)pm).ItemsClass().getName();
				
				
				lo.Filter().addAll(((IItemSelectionProvider)sp).getConditions());
				lo.Sorting().addAll(((IItemSelectionProvider)sp).getSorting());
				return getListCount(cn, lo);
			}
		}
		return 0;
	}		
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IItem getReferedItem(IItem item, String name) throws IonException {
		IProperty p = item.Property(name);
		if (p != null)
			return ((IReferenceProperty) p).getReferedItem();
		return null;
	}
		
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public boolean CheckNavAccess(String nodeid) throws IonException {
		if (aclManager != null)
			return aclManager.checkNodeAccess(nodeid, new DACPermission[]{DACPermission.READ});
		return true;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public boolean CheckClassInstanciationAccess(String className) throws IonException {
		if (aclManager != null)
			return aclManager.checkClassAccess(className, new DACPermission[]{DACPermission.USE});
		return true;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public boolean CheckClassSavingAccess(String className) throws IonException {
		if (aclManager != null)
			return aclManager.checkClassAccess(className, new DACPermission[]{DACPermission.WRITE});
		return true;
	}	
	
//	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
//	public boolean CheckClassPermissons(String className, DACPermission[] permissions){
//		if(aclManager != null)
//			return aclManager.checkClassAccess(className, permissions);
//		return true;
//	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public boolean CheckObjectPermissons(Object o, DACPermission[] permissions) throws IonException {
		if(aclManager != null)
			if(o instanceof IStructMeta)
				return aclManager.checkClassAccess(((IStructMeta) o).getName(), permissions);
			if(o instanceof INode)
				return aclManager.checkNodeAccess(((INode) o).getCode(), permissions);
			if(o instanceof IItem)
				return aclManager.checkObjectAccess(((IItem) o).getItemId(), permissions);
		return true;
	}
		
	@Transactional(
		propagation = Propagation.REQUIRED,
		rollbackFor = { IonException.class })
	public void Put(String container_class, String container_id, String collection, String element_class, String element_id)
																																					 throws IonException {
		if(aclManager!=null){
			DACPermission[] permissions = {DACPermission.WRITE};
			if(!aclManager.checkObjectAccess(container_class, container_id, permissions)){
				if(!aclManager.checkClassAccess(container_class, permissions)){
					throw new IonException("Ошибка доступа");				
				}
			}
			permissions[0] = DACPermission.USE;
			if(!aclManager.checkObjectAccess(element_class, element_id, permissions)){
				if(!aclManager.checkClassAccess(element_class, permissions)){
					throw new IonException("Ошибка доступа");				
				}
			}
		}
		
		data.Put(data.GetItem(container_class, container_id), collection, 
		         data.GetItem(element_class, element_id), 
		         needLogging(ChangelogRecordType.PUT, container_class, container_id, new HashMap<String, Object>())?changeLogger:null);
	}
	
	@Transactional(
 		propagation = Propagation.REQUIRED,
 		rollbackFor = { IonException.class })
 	public void Eject(String container_class, String container_id, String collection, String element_class, String element_id)
 																																					 throws IonException {
 		if(aclManager!=null){
 			DACPermission[] permissions = {DACPermission.WRITE};
 			if(!aclManager.checkObjectAccess(container_class, container_id, permissions)){
 				if(!aclManager.checkClassAccess(container_class, permissions)){
 					throw new IonException("Ошибка доступа");				
 				}
 			}
 			permissions[0] = DACPermission.READ;
 			if(!aclManager.checkObjectAccess(element_class, element_id, permissions)){
 				if(!aclManager.checkClassAccess(element_class, permissions)){
 					throw new IonException("Ошибка доступа");				
 				}
 			}
 		}
 		
 		data.Eject(data.GetItem(container_class, container_id), collection, 
 		           data.GetItem(element_class, element_id), 
 		           needLogging(ChangelogRecordType.PUT, container_class, container_id, new HashMap<String, Object>())?changeLogger:null);
 	}	

	@Transactional(
		propagation = Propagation.REQUIRED,
		rollbackFor = { IonException.class })
	public Collection<IItem> GetCollection(IItem item, String prop_name, ListOptions lo)
																																				 throws IonException {
		if(aclManager!=null){
			DACPermission[] permissions = {DACPermission.READ};
			if(!aclManager.checkObjectAccess(item.getClassName(), item.getItemId(), permissions)){
				if(!aclManager.checkClassAccess(item.getClassName(), permissions)){
					throw new IonException("Ошибка доступа");				
				}
			};
		}		
		return data.GetAssociationsList(item, prop_name, lo);
	}
	
	@Transactional(
	           		propagation = Propagation.REQUIRED,
	           		rollbackFor = { IonException.class })
 	public long GetCollectionSize(IItem item, String prop_name, ListOptions lo)
 																																				 throws IonException {
		return data.GetAssociationsCount(item, prop_name, lo);
 	}	
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public Long getListCount(String classname, ListOptions lo) throws IonException {
		return data.GetCount(classname, lo);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})
	public IWorkflowState GetWorkflowState(IItem item) throws IonException {
		if(workflow != null){
			return workflow.GetState(item);
		}
		return null;
	}
}