package ion.framework.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ion.core.CompositeIndex;
import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.FilterOption;
import ion.core.HistoryMode;
import ion.core.IMetaRepository;
import ion.core.IPropertyMeta;
import ion.core.IStructMeta;
import ion.core.IUserTypeMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.Sorting;
import ion.core.SortingMode;
import ion.core.data.ItemSelectionProvider;
import ion.core.logging.ILogger;
import ion.core.meta.ClassMeta;
import ion.core.meta.PropertyMeta;
import ion.core.meta.CollectionPropertyMeta;
import ion.core.meta.ReferencePropertyMeta;
import ion.core.meta.StructMeta;
import ion.core.meta.StructPropertyMeta;
import ion.core.meta.UserTypeMeta;
import ion.core.meta.UserTypePropertyMeta;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCompositeIndex;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSelectionProvider;
import ion.framework.meta.plain.StoredSorting;
import ion.framework.meta.plain.StoredUserTypeMeta;
import ion.framework.validators.JSONValidator;

public class IonMetaRepository implements IMetaRepository {
	private File metaDirectory;
	
	private ILogger logger;
	
	private Map<String, IStructMeta> structs;
	
	private Map<String, IUserTypeMeta> types;

	public class IonPropertiesLoader {
		public Map<String, IPropertyMeta> load(StoredClassMeta plain, IonMetaRepository rep) throws IonException, Exception{
			Map<String, IPropertyMeta> property_metas = new LinkedHashMap<String, IPropertyMeta>();
		
			IPropertyMeta pm;
			
			StoredPropertyMeta[] prprties = plain.properties.toArray(new StoredPropertyMeta[plain.properties.size()]);
			Arrays.sort(prprties);
			
			for (StoredPropertyMeta spm : prprties){
				pm = null;
				Boolean readonly = (plain.changeTracker.equals(spm.caption) || plain.creationTracker.equals(spm.caption))?true:spm.readonly;
				if (spm.type == MetaPropertyType.REFERENCE.getValue()){
					pm = new ReferencePropertyMeta(spm.name, spm.caption, spm.ref_class, spm.nullable, readonly, spm.unique, spm.order_number, (spm.eager_loading != null)?spm.eager_loading:false, spm.semantic, spm.hint, rep);
					
					Collection<FilterOption> conditions = parseSelConditions(spm.sel_conditions);
					
					Collection<Sorting> sorting = parseSelSorting(spm.sel_sorting);
					pm.SetSelection(new ItemSelectionProvider(conditions, sorting));
				} else if (spm.type == MetaPropertyType.COLLECTION.getValue()){
					pm = new CollectionPropertyMeta(spm.name, spm.caption, spm.items_class, spm.binding, spm.back_ref, spm.back_coll, spm.order_number, (spm.eager_loading != null)?spm.eager_loading:false, spm.semantic, spm.hint, rep);
					
					Collection<FilterOption> conditions = parseSelConditions(spm.sel_conditions);
					
					Collection<Sorting> sorting = parseSelSorting(spm.sel_sorting);
					pm.SetSelection(new ItemSelectionProvider(conditions, sorting));					
				} else if (spm.type == MetaPropertyType.STRUCT.getValue())
					pm = new StructPropertyMeta(spm.name, spm.caption, spm.ref_class, readonly, spm.order_number, spm.hint, rep);
				else if (spm.type == MetaPropertyType.CUSTOM.getValue()) {
					//DEV-36
					String typeName = spm.ref_class;
					IUserTypeMeta typeMeta = rep.GetUserType(typeName);
					Short decimals = spm.decimals;
					if (decimals == null) decimals = typeMeta.getDecimals(); 
					Short size = spm.size;
					if (size == null) size = typeMeta.getSize(); 
					pm = new UserTypePropertyMeta(spm.name, spm.caption, MetaPropertyType.fromInt(spm.type.intValue()), size, decimals, spm.nullable, 
							readonly, spm.indexed, spm.unique, spm.autoassigned, spm.hint,
							StoredPropertyMeta.ParseValue(typeMeta.getBaseType().getValue(), spm.default_value), 
							null, spm.order_number, typeMeta, spm.index_search, spm.formula);
				} else if (spm.type != null)			
					pm = new PropertyMeta(spm.name, spm.caption, MetaPropertyType.fromInt(spm.type.intValue()), spm.size, spm.decimals, spm.nullable, readonly, spm.indexed, spm.unique, spm.autoassigned, spm.hint, StoredPropertyMeta.ParseValue(spm.type, spm.default_value), spm.order_number, spm.index_search);

				if (pm != null){
					if (spm.selection_provider != null){
						switch (spm.selection_provider.type){
							case StoredSelectionProvider.TYPE_HQL:{
								pm.SetSelection(new IonMetaQuerySelectionProvider(spm.selection_provider.hq, spm.selection_provider.parameters));
							}break;
							case StoredSelectionProvider.TYPE_MATRIX:{
								pm.SetSelection(new IonMetaConditionalSelectionProvider(spm.selection_provider.matrix));
							}break;
							case StoredSelectionProvider.TYPE_SIMPLE:{
								pm.SetSelection(new IonMetaSimpleSelectionProvider(spm.selection_provider.list));
							}break;
						}
					}
					property_metas.put(pm.Name(), pm);
				}
			}
			return property_metas;
		}
		
		private Collection<Sorting> parseSelSorting(Collection<StoredSorting> ss) throws Exception {
			Collection<Sorting> selc = new ArrayList<Sorting>();
			for (StoredSorting sce: ss){
				selc.add(new Sorting(sce.property, SortingMode.fromInt(sce.mode)));
			}	
			return selc;
		}
/*		
		private java.util.List<Condition> parseValueConditions(java.util.List<StoredCondition> sc){
			java.util.List<Condition> valueCondition = new ArrayList<Condition>();
			if(sc != null)
				for(StoredCondition vc : sc){
					valueCondition.add(new Condition(vc.property, ConditionType.fromInt(vc.operation), vc.value
//					                                 ,parseValueConditions(vc.valueConditions)
					                                 ));
				}
			return valueCondition;
		}
*/		
		private Collection<FilterOption> parseSelConditions(Collection<StoredCondition> sc) throws Exception{
			Collection<FilterOption> selc = new ArrayList<FilterOption>();
			for (StoredCondition sce: sc){
				selc.add(new Condition(sce.property, ConditionType.fromInt(sce.operation.intValue()), sce.value
//				                       ,parseValueConditions(sce.valueConditions)
				                       ));
			}	
			return selc;
		}		
	}
	
	public class IonStructMeta extends StructMeta {
		
		private StoredClassMeta plain;
		
		public IonStructMeta(StoredClassMeta plain, IonMetaRepository rep) throws Exception{
			super(plain.name, plain.version, plain.caption, plain.semantic, rep);
			this.plain = plain;
			IonPropertiesLoader loader = new IonPropertiesLoader();
			propertyMetas = loader.load(plain, rep);
			descendants = new LinkedList<IStructMeta>();			
		}
		
		@Override
		public IStructMeta getAncestor() throws IonException {
			return anc;
		}	
		
		@Override
		public Map<String, IPropertyMeta> PropertyMetas() throws IonException {
			return propertyMetas;
		}		
				
		private void assignAncestor(){
			if (plain.ancestor != null && !plain.ancestor.isEmpty()){	
				anc = (IonStructMeta) ((IonMetaRepository)rep).structs.get(plain.ancestor);
				if (anc != null)
					((IonStructMeta)anc).descendants.add(this);				
			}
		}		
	}	
	
	public class IonClassMeta extends ClassMeta {
		
		private StoredClassMeta plain;
		
		public IonClassMeta(StoredClassMeta plain, IonMetaRepository rep) throws Exception{
			super(plain.key.toArray(new String[plain.key.size()]), 
			      plain.name, 
			      plain.version, 
			      plain.caption, 
			      plain.semantic, 
			      rep, 
			      plain.container, 
			      plain.creationTracker, 
			      plain.changeTracker, 
			      HistoryMode.fromInt(plain.history), 
			      plain.journaling,
			      parseCompositeIndexes(plain));
			this.plain = plain;
			IonPropertiesLoader loader = new IonPropertiesLoader();
			propertyMetas = loader.load(plain, rep);
			descendants = new LinkedList<IStructMeta>();
		}
		
		@Override
		public IStructMeta getAncestor() throws IonException {
			return anc;
		}	
		
		@Override
		public Map<String, IPropertyMeta> PropertyMetas() throws IonException {
			return propertyMetas;
		}		
				
		private void assignAncestor(){
			if (plain.ancestor != null && !plain.ancestor.isEmpty()){	
				anc = (IonClassMeta)((IonMetaRepository)rep).structs.get(plain.ancestor);
				if (anc != null)
					((IonClassMeta)anc).descendants.add(this);
			}
		}		
	}

	private class IonUserTypeMeta extends UserTypeMeta {
		public IonUserTypeMeta(StoredUserTypeMeta plain, IonMetaRepository rep) {
			super(plain.name, plain.caption, MetaPropertyType.fromInt(plain.type),
					plain.size, plain.decimals, plain.mask, plain.mask_name);
		}
	}
	
	private CompositeIndex[] parseCompositeIndexes(StoredClassMeta cm){
		CompositeIndex[] ci;
		if (cm.compositeIndexes != null){
			ci = new CompositeIndex[cm.compositeIndexes.size()];
			int i = 0;
			for (StoredCompositeIndex sci: cm.compositeIndexes){
				ci[i] = new CompositeIndex(sci.properties.toArray(new String[sci.properties.size()]), sci.unique);
				i++;
			}
		} else
			ci = new CompositeIndex[0];
		return ci;
	}	
	
	private Boolean validate(StoredClassMeta meta){
		JSONValidator v = new JSONValidator();
		String[] result = v.Validate(meta);
		if (logger != null)
		for (String msg: result)
			logger.Warning(msg);
		return result.length == 0;
	}
	
	private void processClassLoadException(StoredClassMeta meta, Exception e){
		if (logger != null){
			logger.Error("Доменный класс "+meta.caption+"["+meta.name+"] не был загружен!", e);
		}
	}
	
	private IUserTypeMeta loadUserTypeMeta(File f, Gson gs) throws IonException {
		IUserTypeMeta result = null;
		Reader r;
		try {
			r = new InputStreamReader(new FileInputStream(f),"UTF-8");
			try {
				StoredUserTypeMeta plain = gs.fromJson(r, StoredUserTypeMeta.class);
				result = new IonUserTypeMeta(plain, this);
				types.put(result.getName(), result);
			} finally {
				try {
					r.close();
				} catch (IOException e) {
					throw new IonException(e);
				}
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			throw new IonException(e);
		}
		// TODO: валидация StoredUserTypeMeta
		return result;
	}
	
	private IStructMeta loadClassMeta(File f,Gson gs) throws IonException{
		try {
			IStructMeta result = null;
			Reader r = new InputStreamReader(new FileInputStream(f),"UTF-8");
			StoredClassMeta plain = gs.fromJson(r, StoredClassMeta.class);
			r.close();
			if (!validate(plain)){
				processClassLoadException(plain, new IonException("Доменный класс не прошел валидацию!"));
			} else {
				try {
					if (plain.is_struct){
						result = new IonStructMeta(plain, this);
					} else {
						result = new IonClassMeta(plain, this);
					}
				} catch (NullPointerException e){
					processClassLoadException(plain, e);
				}
				if (result != null)
					structs.put(result.getName(), result);
			}
			return result;
		} catch (Exception e){
			throw new IonException(e);
		}		
	}

	public IStructMeta Obtain(String classname) throws IonException{
		if (!structs.containsKey(classname)){
			final String cn = classname;
			File[] fls = this.metaDirectory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals(cn+".class.json");
				}
			});
			
			if (fls.length == 0)
				throw new IonException("Class meta not found!");

			Gson gs = new GsonBuilder().serializeNulls().create();
			IStructMeta result = null;
			for (File f: fls)
				result = loadClassMeta(f, gs);
			
			if (result instanceof IonStructMeta)
				((IonStructMeta) result).assignAncestor();
			else if (result instanceof IonClassMeta)
				((IonClassMeta) result).assignAncestor();
		}
		return structs.get(classname);
	}
	
	public IonMetaRepository(){
		structs = new HashMap<String, IStructMeta>();
		types = new HashMap<String, IUserTypeMeta>();
	}
	
	public File getMetaDirectory(){
		return metaDirectory;
	}
	
	public void setMetaDirectory(File dir) throws IonException{
		metaDirectory = dir;
		structs.clear();
		types.clear();
		if (metaDirectory.exists()){

			Gson gs = new GsonBuilder().serializeNulls().create();
			String nm;

			// загрузка пользовательских типов данных
			File typedir = new File(metaDirectory.getAbsolutePath(), "types");
			if (typedir.exists() && typedir.isDirectory()) {
				File[] typefiles = typedir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".type.json");
					}
				});
				try {
					for (File cf: typefiles){
						loadUserTypeMeta(cf, gs);
					}
				} catch (Exception e) {
					throw new IonException(e);
				}
			}
			
			File[] metafiles = metaDirectory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".class.json");
				}
			});
			
			try {
				for (File cf: metafiles){
					nm = cf.getName().replace(".class.json", "");
					if (!structs.containsKey(nm))
						loadClassMeta(cf, gs);
				}
			} catch (Exception e) {
				throw new IonException(e);
			}

			for (IStructMeta cl: structs.values()){
				if (cl instanceof IonStructMeta)
					((IonStructMeta)cl).assignAncestor();
				else if (cl instanceof IonClassMeta)
					((IonClassMeta)cl).assignAncestor();
			}
		} 
		else 
			throw new IonException("Meta directory not set!");		
	}
	
	public void setLogger(ILogger logger){
		this.logger = logger;
	}

	public IStructMeta Get(String name) {
		return structs.get(name);
	}

	public Collection<IStructMeta> List() {
		return structs.values();
	}

	public Collection<IStructMeta> List(String ancestor) throws IonException {
		return List(ancestor,false);
	}
	
	private void fillDescendants(Collection<IStructMeta> src, Collection<IStructMeta> result) throws IonException{
		result.addAll(src);
		for (IStructMeta c: src){
			fillDescendants(c.Descendants(true), result);
		}
	}

	public Collection<IStructMeta> List(String ancestor, Boolean direct) throws IonException {
		if (direct)
			return structs.get(ancestor).Descendants(true);
		Collection<IStructMeta> result = new ArrayList<IStructMeta>();
		fillDescendants(structs.get(ancestor).Descendants(true), result);
		return result;
	}

	public IStructMeta Ancestor(String classname) throws IonException {
		IStructMeta cm = Get(classname);
		if (cm != null)
			return cm.getAncestor();
		return null;
	}

	public Map<String, IPropertyMeta> PropertyMetas(String name) throws IonException {
		return structs.get(name).PropertyMetas();
	}
	
	@Override
	public IUserTypeMeta GetUserType(String name) throws IonException {
		return types.get(name);
	}
	
}
