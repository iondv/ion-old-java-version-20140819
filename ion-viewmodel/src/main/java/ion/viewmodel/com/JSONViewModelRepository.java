package ion.viewmodel.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import ion.core.ICollectionPropertyMeta;
import ion.core.IPropertyMeta;
import ion.core.IReferencePropertyMeta;
import ion.core.IStructMeta;
import ion.core.IStructPropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.logging.ILogger;
import ion.framework.meta.plain.StoredValidatorMeta;
import ion.framework.validators.JSONValidator;
import ion.viewmodel.plain.StoredAction;
import ion.viewmodel.plain.StoredColumn;
import ion.viewmodel.plain.StoredField;
import ion.viewmodel.plain.StoredFormViewModel;
import ion.viewmodel.plain.StoredListAction;
import ion.viewmodel.plain.StoredListViewModel;
import ion.viewmodel.plain.StoredTab;
import ion.viewmodel.plain.StoredViewModel;
import ion.viewmodel.view.Action;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.CollectionField;
import ion.viewmodel.view.CollectionFieldMode;
import ion.viewmodel.view.DataField;
import ion.viewmodel.view.Field;
import ion.viewmodel.view.FieldAction;
import ion.viewmodel.view.FieldGroup;
import ion.viewmodel.view.FieldSize;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.FormAction;
import ion.viewmodel.view.FormTab;
import ion.viewmodel.view.FormViewModel;
import ion.viewmodel.view.HistoryDisplayMode;
import ion.viewmodel.view.IField;
import ion.viewmodel.view.IFormTab;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.IListColumn;
import ion.viewmodel.view.IListViewModel;
import ion.viewmodel.view.IViewModelRepository;
import ion.viewmodel.view.ListAction;
import ion.viewmodel.view.ListColumn;
import ion.viewmodel.view.ListReferenceColumn;
import ion.viewmodel.view.ListViewModel;
import ion.viewmodel.view.ReferenceField;
import ion.viewmodel.view.ReferenceFieldMode;
import ion.viewmodel.view.Validator;
import ion.viewmodel.view.ViewApplyMode;

public class JSONViewModelRepository implements IViewModelRepository {	

	protected File modelsDirectory;
	protected File validatorsDirectory;
	
	protected Map<String, Object> listModels;
	protected Map<String, Object> collectionModels;
	protected Map<String, Object> itemModels;
	protected Map<String, Object> createModels;
	protected Map<String, Object> detailModels;
	protected Map<String,String> masks;
	protected Map<String,Validator> validators;
	
	protected ILogger logger;
	

	public JSONViewModelRepository() {
		listModels = new HashMap<String, Object>();
		collectionModels = new HashMap<String, Object>();
		itemModels = new HashMap<String, Object>();
		createModels = new HashMap<String, Object>();
		detailModels = new HashMap<String, Object>();
		validators = new HashMap<String, Validator>();
	}
	
	public void setLogger(ILogger logger){
		this.logger = logger;
	}

	private Boolean validate(StoredViewModel o){
		JSONValidator v = new JSONValidator();
		String[] result;
		if (o instanceof StoredFormViewModel)
			result = v.Validate((StoredFormViewModel)o);
		else
			result = v.Validate((StoredListViewModel)o);
		
		if (logger != null)
			for (String msg: result)
				logger.Warning(msg);
		
		return result.length == 0;
	}
	
	private void processModelLoadException(File f, StoredViewModel o, Throwable e){
		if (logger != null){
			logger.Warning("Не удалось загрузить модель представления "+ f.getPath() +".", e);
		}
	}
	
	protected void loadModel(File f, String path, Gson gs) throws IOException {
		Reader r;
		
		if (f.isDirectory()){
			File[] fl = f.listFiles();
			for (File sf: fl){
				loadModel(sf, path + "/" + f.getName(),gs);
			}			
		} else if (f.getName().equals("masks.json")){
			r = new InputStreamReader(new FileInputStream(f),"UTF-8");
			masks = gs.fromJson(r, new TypeToken<Map<String,String>>(){}.getType());
			r.close();
		} else if (f.getName().equals("list.json")){
			r = new InputStreamReader(new FileInputStream(f),"UTF-8");
			StoredListViewModel sm = gs.fromJson(r, StoredListViewModel.class);
			r.close();
			if (!validate(sm)){
				processModelLoadException(f, sm, new IonException("Модель представления не прошла валидацию!"));
			} else {	
				try {
					listModels.put(path,sm);
				} catch (NullPointerException e){
					processModelLoadException(f, sm, e);
				}
			}
		} else if (f.getName().equals("item.json") || f.getName().equals("create.json") || f.getName().equals("detail.json")) {
			Map<String, Object> models = null;
			switch (f.getName()) {
				case "item.json": models = itemModels; break;
				case "create.json": models = createModels; break;
				case "detail.json": models = detailModels; break;
				default: assert(false);
			}
			r = new InputStreamReader(new FileInputStream(f),"UTF-8");
			StoredFormViewModel sm = gs.fromJson(r, StoredFormViewModel.class);
			r.close();
			if (!validate(sm)){
				processModelLoadException(f, sm, new IonException("Модель представления не прошла валидацию!"));
			} else {	
				try {
					models.put(path, sm);
				} catch (NullPointerException e){
					processModelLoadException(f, sm, e);
				}
			}
		} else if (f.getName().endsWith(".collection.json")){
			r = new InputStreamReader(new FileInputStream(f),"UTF-8");
			StoredListViewModel sm = gs.fromJson(r, StoredListViewModel.class);
			r.close();
			if (!validate(sm)){
				processModelLoadException(f, sm, new IonException("Модель представления не прошла валидацию!"));
			} else {	
				try {
					collectionModels.put(path+"/"+f.getName().replace(".collection.json", "").replace(".", "/"),sm);
				} catch (NullPointerException e){
					processModelLoadException(f, sm, e);
				}
			}
		}
		
	}
	
	
	/**
	 * @param f
	 * @param path
	 * @param gs
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	
	protected Object getModel(Map<String, Object> list, String key, IStructMeta meta, Set<FieldType> ignoreFieldTypes) {
		if (list.containsKey(key)){
			Object model = list.get(key);
			if (model instanceof StoredViewModel){
				if (model instanceof StoredListViewModel)
					model = new ListViewModel(((StoredViewModel) model).version,parseColumns(((StoredListViewModel) model).columns), 
							((StoredListViewModel) model).pageSize, 
							((StoredListViewModel) model).allowSearch, 
							parseListActions(((StoredListViewModel) model).commands), 
							(((StoredViewModel) model).overrideMode == null)?ViewApplyMode.HIDE:ViewApplyMode.fromInt(((StoredViewModel) model).overrideMode),
							((StoredListViewModel) model).useEditModels);
				

				if (model instanceof StoredFormViewModel)
					model = new FormViewModel(((StoredViewModel) model).version, parseTabs(((StoredFormViewModel) model).tabs, meta, ignoreFieldTypes), 
							parseActions(((StoredFormViewModel) model).commands), 
							(((StoredViewModel) model).overrideMode == null)?ViewApplyMode.HIDE:ViewApplyMode.fromInt(((StoredViewModel) model).overrideMode),
							HistoryDisplayMode.fromInt(((StoredFormViewModel) model).historyDisplayMode),null,null);
				
				//list.put(key, model);
			} 
			return model;
		}
		return null;
	}
	
	protected void processValidator(File f, Gson gs) throws UnsupportedEncodingException, FileNotFoundException{
		Reader r;
		if(f.getName().endsWith(".valid.json")){
			r = new InputStreamReader(new FileInputStream(f),"UTF-8");
			StoredValidatorMeta svm = gs.fromJson(r,StoredValidatorMeta.class);
			validators.put(svm.name, new Validator(svm.name, svm.caption, svm.assignByContainer, svm.validationExpression, svm.mask));
		}
	}
	
	
	
	public File getModelsDirectory(){
		return modelsDirectory;
	}
	
	public void setModelsDirectory(File dir, File validatorsDir) throws IonException{
		listModels.clear();
		collectionModels.clear();
		itemModels.clear();
		createModels.clear();	
		validators.clear();
		
		modelsDirectory = dir;
		validatorsDirectory = validatorsDir;
		Gson gs = new GsonBuilder().serializeNulls().create();
		if (validatorsDirectory!=null && validatorsDirectory.isDirectory()){
			try {
				File[] vl = validatorsDirectory.listFiles();
				for (File v: vl)
					processValidator(v, gs);
			} catch (Exception e){
				throw new IonException(e);
			}
		}
		if (modelsDirectory.isDirectory()){
			try {
				File[] fl = modelsDirectory.listFiles();
				for (File f: fl)
					loadModel(f, "", gs);
			} catch (Exception e){
				throw new IonException(e);
			}
		}
		
		if (masks == null)
			masks = new HashMap<String, String>();		
	}
	
	public void setModelsDirectory(File dir) throws IonException{
		setModelsDirectory(dir,null);
	}
	
	protected Set<ActionType> actions(Integer src){
		return Action.IntToActionTypeSet(src);
	}
	
	protected List<IListColumn> parseColumns(Collection<StoredColumn> src){
		LinkedList<IListColumn> result = new LinkedList<IListColumn>();
		for (StoredColumn col: src){
			if (col.type == FieldType.REFERENCE.getValue())
				result.add(new ListReferenceColumn(col.caption, col.property, (col.size == null)?null:FieldSize.fromInt(col.size), col.sorted, col.order_number, col.readonly, (col.selection_paginated != null)?col.selection_paginated:false, col.hint));
			else
				result.add(new ListColumn(col.caption, col.property, (col.type == null)?FieldType.TEXT:FieldType.fromInt(col.type), (col.size == null)?null:FieldSize.fromInt(col.size), col.sorted, col.order_number, col.readonly, col.hint));
		}
		Collections.sort(result);
		return result;
	}
	
	protected Collection<StoredColumn> getDefaultColumns(IStructMeta meta) throws IonException{
		Collection<StoredColumn> result = new LinkedList<StoredColumn>();
		IStructMeta ancestor = meta;
		while(ancestor != null){
  		for(IPropertyMeta pm : ancestor.PropertyMetas().values()){
  			StoredColumn c = new StoredColumn(pm.Caption(), pm.Name(), pm.Hint());
  			c.type = FieldType.fromPropertyType(pm.Type()).getValue();
  			result.add(c);
  		}
  		ancestor = ancestor.getAncestor();
		}
		return result;
	}
	
	protected Collection<StoredField> getDefaultFields(IStructMeta meta, String name_prefix) throws IonException{
		Collection<StoredField> result = new ArrayList<StoredField>();
		IStructMeta ancestor = meta;
		while(ancestor != null){
  		for(IPropertyMeta pm : ancestor.PropertyMetas().values()){
  			if (pm.Type() == MetaPropertyType.REFERENCE)
  				result.add(new StoredField(pm.Caption(), name_prefix+pm.Name(), ReferenceFieldMode.LINK, new ArrayList<StoredField>(),pm.Hint(),null));  				
  			else if (pm.Type() == MetaPropertyType.COLLECTION)
  				result.add(new StoredField(pm.Caption(), name_prefix+pm.Name(), CollectionFieldMode.LINK, new ArrayList<StoredColumn>(), null,pm.Hint(),null));
  			else if (pm.Type() == MetaPropertyType.STRUCT){
  				StoredField sf = new StoredField(pm.Caption(), name_prefix+pm.Name(),pm.Hint());
  				sf.type = FieldType.GROUP.getValue();
  				sf.fields = getDefaultFields(((IStructPropertyMeta)pm).StructClass(), name_prefix+pm.Name()+"$");
  				result.add(sf);  				
  			} else
  				result.add(new StoredField(pm.Caption(), name_prefix+pm.Name(), FieldType.fromPropertyType(pm.Type()), null,pm.Hint(),null));
  		}
  		ancestor = ancestor.getAncestor();
		}
		return result;
	}
	
	protected IField parseField(StoredField f, IStructMeta meta, Boolean readonly){
		FieldType t = (f.type == null)?FieldType.TEXT:FieldType.fromInt(f.type);
		
		if (f.mode == null){
			if (t == FieldType.REFERENCE)
				f.mode = ReferenceFieldMode.LINK.getValue();
			else if (t == FieldType.COLLECTION)
				f.mode = CollectionFieldMode.LINK.getValue();
		}
		
		boolean ro = false;
		if(f.readonly != null)
			ro = f.readonly;
		
		Collection<StoredField> fields = f.fields;
		Collection<StoredColumn> columns = f.columns;
		IStructMeta nested_cm = meta;
		if (f.property != null && !f.property.isEmpty()){
			try {
  			IPropertyMeta pm = meta.PropertyMeta(f.property);
  			switch (t){
  				case COLLECTION:
  					if(f.mode == CollectionFieldMode.TABLE.getValue()){
  						if(pm.Type() == MetaPropertyType.COLLECTION && pm instanceof ICollectionPropertyMeta){
  							nested_cm = ((ICollectionPropertyMeta)pm).ItemsClass();
    	    			if(columns == null || columns.isEmpty())
    	    				columns = getDefaultColumns(nested_cm);
  						}
  					}
  					break;
					case REFERENCE:
						if (f.mode == ReferenceFieldMode.INFO.getValue()) {
							if (pm.Type() == MetaPropertyType.REFERENCE
									&& pm instanceof IReferencePropertyMeta) {
								nested_cm = ((IReferencePropertyMeta) pm).ReferencedClass();
								if (fields == null || fields.isEmpty())
									fields = getDefaultFields(nested_cm, "");
							}
						}
						break;
  				case GROUP:
						if (pm.Type() == MetaPropertyType.STRUCT
								&& pm instanceof IStructPropertyMeta) {
							nested_cm = ((IStructPropertyMeta) pm).StructClass();
							if (fields == null || fields.isEmpty())
								fields = getDefaultFields(nested_cm, pm.Name()+"$");
						}
  					break;
					default:
						break;
  			}
  				
  			if (pm != null && pm.ReadOnly())
  					ro = true; 
			} catch (IonException e) {
				
			}
		}
		
		Field fld = null;
		switch (t){
			case COLLECTION:fld = new CollectionField(f.caption, 
					f.property, 
					CollectionFieldMode.fromInt(f.mode), 
					parseColumns(columns),
					parseFieldActions(f.commands), 
					f.order_number,
					readonly?true:ro,
					f.required,
					f.hint,
					HistoryDisplayMode.fromInt(f.historyDisplayMode)
				);
			break;
			case REFERENCE:{fld = new ReferenceField(f.caption, 
					f.property,
					(f.selection_paginated != null)?f.selection_paginated:true,
					ReferenceFieldMode.fromInt(f.mode), 
					parseFields(fields, nested_cm, readonly?true:ro),
					parseFieldActions(f.commands),
					f.hierarchyAttributes,
					f.order_number,
					f.required, 
					readonly?true:ro,
					f.hint,
					HistoryDisplayMode.fromInt(f.historyDisplayMode)
				);
			}break;
			case GROUP:fld = new FieldGroup(f.caption,
					parseFields(fields, nested_cm, readonly?true:ro), 
					f.property, 
					f.order_number, 
					readonly?true:ro,
					f.hint
				);
			break;
			default:fld = new DataField(f.caption, 
					f.property, 
					FieldSize.fromInt(f.size), 
					f.maskName, 
					t, 
					f.order_number, f.hint,
					f.required, 
					f.mask,
					readonly?true:ro
				);
			break;
		}
		
		if (fld != null){
			if (f.visibility != null && !f.visibility.isEmpty())
				fld.setVisibilityExpression(f.visibility);

			if (f.enablement != null && !f.enablement.isEmpty())
				fld.setEnablementExpression(f.enablement);
		
			if (f.obligation != null && !f.obligation.isEmpty())
				fld.setObligationExpression(f.obligation);
			
			if (f.validators != null && !f.validators.isEmpty())
				fld.setValidators(f.validators);
				
		}
		
		return fld;
	}
	
	protected List<FieldAction> parseFieldActions(Collection<StoredAction> actions){
		List<FieldAction> result = new LinkedList<FieldAction>();
		for (StoredAction a: actions){
			result.add(new FieldAction(a.id, a.caption, a.visibilityCondition, a.enableCondition));
		}
		return result;
	}
	
	protected List<IField> parseFields(Collection<StoredField> src, IStructMeta meta){
		return parseFields(src, meta, false, new HashSet<FieldType>());
	}
	
	protected List<IField> parseFields(Collection<StoredField> src, IStructMeta meta, Boolean readonly){
		return parseFields(src, meta, readonly, new HashSet<FieldType>());
	}
	
	protected List<IField> parseFields(Collection<StoredField> src, IStructMeta meta, Set<FieldType> ignoreFieldTypes){
		return parseFields(src, meta, false, ignoreFieldTypes);
	}
	
	protected List<IField> parseFields(Collection<StoredField> src, IStructMeta meta, Boolean readonly, Set<FieldType> ignoreFieldTypes){
		LinkedList<IField> result = new LinkedList<IField>();
		for (StoredField f: src){
			if(!ignoreFieldTypes.contains(FieldType.fromInt(f.type)))
				result.add(parseField(f, meta, readonly?true:((f.readonly != null)?f.readonly:false)));
		}
		return result;		
	}
	
	protected String nodePath(String node, String classname){
		return (node.isEmpty()?"":"/") + node.replaceAll("[^0-9a-zA-Z]+", "/") + "/" + classname;
	}

	@Override
	public IListViewModel getListViewModel(String node, IStructMeta meta) {
		String code = nodePath(node, meta.getName());
		if (listModels.containsKey(code))
			return (IListViewModel)getModel(listModels, nodePath(node, meta.getName()), meta, new HashSet<FieldType>());
		
		try {
			if (meta.getAncestor() != null)
				return getListViewModel(node, meta.getAncestor());
		} catch (IonException e) {
			logger.Error("Ошибка получения модели представления!", e);
		}
		return null;
	}
	
	@Override
	public IListViewModel getListViewModel(IStructMeta meta) {
		return getListViewModel("",meta);
	}	

	@Override
	public IListViewModel getCollectionViewModel(String node, IStructMeta meta, String collection) {
		String code = nodePath(node,meta.getName())+"/"+collection;
		if (listModels.containsKey(code))
			return (IListViewModel)getModel(listModels, code, meta, new HashSet<FieldType>());
		
		try {
			if (meta.getAncestor() != null)
				return getCollectionViewModel(node, meta.getAncestor(), collection);
		} catch (IonException e) {
			logger.Error("Ошибка получения модели представления!", e);
		}
		
		return null;
	}

	@Override
	public IListViewModel getCollectionViewModel(IStructMeta meta, String collection) {
		return getCollectionViewModel("", meta, collection);
	}	
	
	protected Collection<IFormTab> parseTabs(Collection<StoredTab> src, IStructMeta meta, Set<FieldType> ignoreFieldTypes){
		Collection<IFormTab> result = new LinkedList<IFormTab>();
		for (StoredTab t: src){
			result.add(new FormTab(t.caption, parseFields(t.fullFields, meta, ignoreFieldTypes),parseFields(t.shortFields, meta, ignoreFieldTypes)));
		}
		return result;		
	}
	
	protected Map<String, FormAction> parseActions(Collection<StoredAction> src) {
		Map<String, FormAction> result = new LinkedHashMap<String, FormAction>();
		for (StoredAction a: src) {
			result.put(a.id, new FormAction(a.id, a.caption, a.visibilityCondition, a.enableCondition, a.signBefore, a.signAfter));
		}
		return result;
	}
	
	protected Map<String, ListAction> parseListActions(Collection<StoredListAction> src) {
		Map<String, ListAction> result = new LinkedHashMap<String, ListAction>();
		for (StoredListAction a: src) {
			result.put(a.id, new ListAction(a.id, a.caption, a.needSelectedItem, a.isBulk));
		}
		return result;
	}
	
	protected IFormViewModel getItemViewModel(String node, IStructMeta meta, Set<FieldType> ignoreFieldTypes) {
		String code = nodePath(node, meta.getName());
		if (itemModels.containsKey(code))
			return (IFormViewModel)getModel(itemModels, code, meta, ignoreFieldTypes);
		
		try {
			if (meta.getAncestor() != null)
				return getItemViewModel(node, meta.getAncestor());
		} catch (IonException e) {
			logger.Error("Ошибка получения модели представления!", e);
		}
		return null;
	}
	
	@Override
	public IFormViewModel getItemViewModel(String node, IStructMeta meta) {
		return getItemViewModel(node, meta, new HashSet<FieldType>());
	}
	
	@Override
	public IFormViewModel getItemViewModel(IStructMeta meta) {
		return getItemViewModel("", meta);
	}	

	@Override
	public IFormViewModel getCreationViewModel(String node, IStructMeta meta) {
		String code = nodePath(node, meta.getName());
		HashSet<FieldType> ignoreFT = new HashSet<FieldType>();
		if (createModels.containsKey(code))
			return (IFormViewModel)getModel(createModels, code, meta, ignoreFT);
		
		ignoreFT.add(FieldType.COLLECTION);
		IFormViewModel vm = getItemViewModel(node, meta, ignoreFT);
		try {
			if (vm == null && meta.getAncestor() != null)
				vm = getCreationViewModel(node, meta.getAncestor());
		} catch (IonException e) {
			logger.Error("Ошибка получения модели представления!",e);
		}
		return vm;	
	}
	
	@Override
	public IFormViewModel getCreationViewModel(IStructMeta meta) {
		return getCreationViewModel("", meta);
	}	

	@Override
	public IFormViewModel getDetailViewModel(String node, IStructMeta meta) {
		String code = nodePath(node, meta.getName());
		if (detailModels.containsKey(code))
			return (IFormViewModel)getModel(detailModels, code, meta, new HashSet<FieldType>());
		
		try {
			if (meta.getAncestor() != null)
				return getDetailViewModel(node, meta.getAncestor());
		} catch (IonException e) {
			logger.Error("Ошибка получения модели представления!", e);
		}
		return null;
	}

	@Override
	public IFormViewModel getDetailViewModel(IStructMeta meta) {
		return getDetailViewModel("", meta);
	}
	
	
	@Override
	public String getMask(String name) {
		return masks.get(name);
	}

	@Override
	public Map<String, Validator> getValidators() {
		return validators;
	}
}
