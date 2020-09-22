package ion.modeler.editors;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ion.core.ConditionType;
import ion.core.HistoryMode;
import ion.core.MetaPropertyType;
import ion.core.OperationType;
import ion.core.SortingMode;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCompositeIndex;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredSelectionProvider;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredMatrixEntry;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSorting;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;
import ion.modeler.forms.IKeyValueProvider;
import ion.modeler.wizards.NewPropertyWizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class EntityEditor extends IonEditor {
	
	public static final String ID = "ion.modeler.editors.entityEditor";
	
	public class CPKVProvider implements IKeyValueProvider {

		StoredClassMeta c;
		
		public CPKVProvider(StoredClassMeta c){
			this.c = c;
		}
		
		@Override
		public Map<String, String> Provide(Object model) {
			return getClassProperties(getComposer(), c, null, null, 0);
		}		
	}
	
	class CondPropProvider implements IKeyValueProvider {

		@Override
		public Map<String, String> Provide(Object model) {
			Composer c = getComposer();
			StoredClassMeta cm = null;
			try {
				IStructuredSelection sel = collectionSelection("properties");
				
				if (sel != null){
					EditorTreeNode node = (EditorTreeNode)sel.getFirstElement();
					if (node != null && node.Parent.Parent instanceof EditorItemNode){
						Object item = ((EditorItemNode)node.Parent.Parent).Item;
						if (item instanceof StoredMatrixEntry)
							cm = (StoredClassMeta)getModel();
						if (item instanceof StoredPropertyMeta){
							if (((StoredPropertyMeta) item).type == MetaPropertyType.REFERENCE.getValue())
								cm = c.getClass(((StoredPropertyMeta)item).ref_class);
							else if (((StoredPropertyMeta) item).type == MetaPropertyType.COLLECTION.getValue())
								cm = c.getClass(((StoredPropertyMeta)item).items_class);							
						}
						if (cm != null){
							IKeyValueProvider worker = new CPKVProvider(cm);
							return worker.Provide(model);
						}
					} else if(node != null && node.Parent instanceof EditorItemNode){
						Object item = ((EditorItemNode)node.Parent).Item;
						Object pm = ((EditorItemNode)node.Parent.Parent.Parent).Item;
						if (item instanceof StoredCondition && pm instanceof StoredPropertyMeta){
							cm = c.getClass(((StoredPropertyMeta)pm).ref_class);
							String itemsClass = null;
							if(cm != null)
								for(StoredPropertyMeta spm : cm.properties){
									if(spm.name.equals(((StoredCondition)item).property))
										itemsClass = spm.items_class;
								}
							if(itemsClass != null){
								cm = c.getClass(itemsClass);
								IKeyValueProvider worker = new CPKVProvider(cm);
								return worker.Provide(model);
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new HashMap<String, String>();
		}
	}
	
	class CondOperProvider implements IKeyValueProvider {
		@Override
		public Map<String, String> Provide(Object model) {
			Map<String, String> result = new HashMap<String, String>();
			if (model instanceof StoredCondition){
				StoredCondition c = (StoredCondition)model;
				if (c.property != null && !c.property.isEmpty()){
					result.put(String.valueOf(ConditionType.EQUAL.getValue()),"=");
					result.put(String.valueOf(ConditionType.LESS.getValue()),"<");
					result.put(String.valueOf(ConditionType.MORE.getValue()),">");
					result.put(String.valueOf(ConditionType.LESS_OR_EQUAL.getValue()),"<=");
					result.put(String.valueOf(ConditionType.MORE_OR_EQUAL.getValue()),">=");
					result.put(String.valueOf(ConditionType.NOT_EQUAL.getValue()),"<>");
					result.put(String.valueOf(ConditionType.EMPTY.getValue()),"пусто");
					result.put(String.valueOf(ConditionType.NOT_EMPTY.getValue()),"не пусто");
					result.put(String.valueOf(ConditionType.CONTAINS.getValue()),"содержит");					
				} else {
					result.put(String.valueOf(OperationType.AND.getValue()),"и");
					result.put(String.valueOf(OperationType.OR.getValue()),"или");
					result.put(String.valueOf(OperationType.NOT.getValue()),"не");
					result.put(String.valueOf(OperationType.MIN.getValue()),"мин");
					result.put(String.valueOf(OperationType.MAX.getValue()),"макс");
				}
			}
			return result;
		}
	}	
	
	class RoleProvider implements IKeyValueProvider {

		@Override
		public Map<String, String> Provide(Object model) {
			Map<String,String> result = new LinkedHashMap<String,String>();
			StoredClassMeta cm = (StoredClassMeta) getModel();
			for (StoredPropertyMeta pm: cm.properties){
				if (pm.type == MetaPropertyType.STRING.getValue())
					result.put(pm.name, pm.caption);
			}
			return result;
		}
	}	
	
	@Override
	protected Object loadModel(IFile f) throws IOException {
		Composer c = new Composer(f.getProject());
		return c.Read(f.getLocation().toString(), StoredClassMeta.class);
	}

	@Override
	protected String mainPageText() {
		return "Сущность";
	}
	
	protected void masterSetup(String name, TreeViewer viewer){
		viewer.setComparator(new ItemComparator());
	}
	
	@SuppressWarnings("serial")
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = super.getSelections(classname);
		
		if (classname.equals("StoredClassMeta")){
			Map<String, String> sl = new LinkedHashMap<String,String>();

			StoredPropertyMeta[] propertylist = new StoredPropertyMeta[((StoredClassMeta)model).properties.size()];
			propertylist = ((StoredClassMeta)model).properties.toArray(propertylist);		
			Arrays.sort(propertylist);
			for (StoredPropertyMeta pm: propertylist){
				/*if(pm.type == MetaPropertyType.STRUCT.getValue()){
					try {
						StoredClassMeta struct = composer.getClass(pm.ref_class);
						while(struct != null){
							for(StoredPropertyMeta spm : struct.properties)
								sl.put(pm.name+"$"+spm.name, pm.caption+"."+spm.caption);
							struct = composer.getClass(struct.ancestor);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}					
				} else {*/
					sl.put(pm.name, pm.caption);
				//}
			}
	
			selections.put("key", sl);
			selections.put("container", sl);	
			selections.put("ancestor", getClassSelection());
			selections.put("creationTracker", sl);
			selections.put("changeTracker", sl);
			selections.put("history", new LinkedHashMap<String, String>(){{
				put(String.valueOf(HistoryMode.NONE.getValue()),"нет");
				put(String.valueOf(HistoryMode.OCCASIONAL.getValue()),"произвольно");
				put(String.valueOf(HistoryMode.HOURLY.getValue()),"с точностью до часа");
				put(String.valueOf(HistoryMode.DAILY.getValue()),"с точностью до суток");
				put(String.valueOf(HistoryMode.WEEKLY.getValue()),"с точностью до недели");
				put(String.valueOf(HistoryMode.MONTHLY.getValue()),"с точностью до месяца");
				put(String.valueOf(HistoryMode.ANNUAL.getValue()),"с точностью до года");
			}});
		}
		return selections;
	}
	
	@SuppressWarnings("serial")
	protected void formProperties(){
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("is_struct", "Является структурой");
		captions.put("caption", "Логическое имя");
		captions.put("version", "Версия");
		captions.put("ancestor", "Родительский класс");
		captions.put("key", "Ключевые атрибуты");
		captions.put("semantic", "Семантический атрибут");
		captions.put("container", "Атрибут ссылки на контейнер");
		captions.put("creationTracker", "Метка времени создания");
		captions.put("changeTracker", "Метка времени изменения");
		captions.put("history", "Снимки данных");
		captions.put("journaling", "Журналирование изменений");
		
		formSettings.put("StoredClassMeta", new FormSettings(captions, new HashMap<String,String>(){
		{
			put("is_struct","boolean");
			put("key","set");
			put("journaling","boolean");
		}}, getSelections("StoredClassMeta")));
	}
	
	@SuppressWarnings("serial")
	protected void formLists(){
		final EntityEditor me = this;
		
		lists.put("properties", new Object[]{
				"Атрибуты",
				new TreeMap<String, FormSettings>(){{
					
					put("StoredPropertyMeta",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("caption","Логическое имя");
							put("hint","Подсказка");
							put("type","Тип значений");
							put("order_number", "Порядковый номер");
							put("size","Размер");
							put("allowedFileTypes","Допустимые типы файлов");
							put("maxFileCount","Максимальное количество файлов");
							put("decimals","Число знаков после запятой");
							put("nullable","Допустимо пустое значение");
							put("readonly","Только для чтения");
							put("indexed","Индексировать для поиска");
							put("unique","Уникальные значения");
							put("autoassigned","Автозаполнение");
							put("default_value","Значение по умолчанию");
							put("formula","Формула");
							put("ref_class","Класс ссылки");
							put("items_class","Класс коллекции");
							put("semantic","Семантика");
							put("back_ref","Атрибут обратной ссылки");
							put("back_coll","Атрибут обратной коллекции");
							put("binding","Основание коллекции");
							put("index_search","Полнотекстовый поиск");
							put("eager_loading","Жадная загрузка");
						}},
						new HashMap<String,String>(){{
							put("allowedFileTypes","commaSeparated");
							put("nullable","boolean");
							put("readonly","boolean");
							put("indexed","boolean");
							put("unique","boolean");
							put("autoassigned","boolean");
							put("index_search","boolean");
							put("eager_loading","boolean");
						}},
						new HashMap<String, Object>(){{
							put("ref_class", new AttrClassListProvider(me));
							put("items_class", new AttrClassListProvider(me));
							put("back_ref", new ClassPropertiesProvider(me.getComposer(), "items_class", MetaPropertyType.REFERENCE));
							put("back_coll", new ClassPropertiesProvider(me.getComposer(), "items_class", MetaPropertyType.COLLECTION));
							if (me.model != null)
								put("binding", new CPKVProvider((StoredClassMeta)me.model));		
							
							put("type", new LinkedHashMap<String, String>(){{
								put(String.valueOf(MetaPropertyType.STRING.getValue()),"Строка");
								put(String.valueOf(MetaPropertyType.INT.getValue()),"Целое");
								put(String.valueOf(MetaPropertyType.REAL.getValue()),"Действительное");
								put(String.valueOf(MetaPropertyType.BOOLEAN.getValue()),"Логический");
								put(String.valueOf(MetaPropertyType.DATETIME.getValue()),"Дата/Время");
								put(String.valueOf(MetaPropertyType.DECIMAL.getValue()),"Десятичное");
								put(String.valueOf(MetaPropertyType.REFERENCE.getValue()),"Ссылка");
								put(String.valueOf(MetaPropertyType.COLLECTION.getValue()),"Коллекция");
								put(String.valueOf(MetaPropertyType.FILE.getValue()),"Файл");
								put(String.valueOf(MetaPropertyType.IMAGE.getValue()),"Изображение");
								put(String.valueOf(MetaPropertyType.FILESLIST.getValue()),"Коллекция файлов");
								put(String.valueOf(MetaPropertyType.TEXT.getValue()),"Текст");
								put(String.valueOf(MetaPropertyType.HTML.getValue()),"HTML");
								put(String.valueOf(MetaPropertyType.URL.getValue()),"URL");
								put(String.valueOf(MetaPropertyType.GUID.getValue()),"Глобальный идентификатор");
								put(String.valueOf(MetaPropertyType.PASSWORD.getValue()),"Пароль");
								put(String.valueOf(MetaPropertyType.SET.getValue()),"Множество");
								put(String.valueOf(MetaPropertyType.GEO.getValue()),"Геоданные");
								put(String.valueOf(MetaPropertyType.PERIOD.getValue()),"Период");								
								put(String.valueOf(MetaPropertyType.STRUCT.getValue()),"Структура");
								put(String.valueOf(MetaPropertyType.CUSTOM.getValue()),"Пользовательский тип");
								put(String.valueOf(MetaPropertyType.USER.getValue()),"Пользователь");
							}});							
						}}
					));
					
					put("StoredCondition",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("property","Атрибут");
							put("operation","Операция");
							put("value","Значение");
						}},
						new HashMap<String,String>(),
						new HashMap<String, Object>(){{
							put("property", new CondPropProvider());
							put("operation", new CondOperProvider());							
						}}
					));		
					
					put("StoredSorting",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("property","Атрибут");
							put("mode","Порядок сортировки");
						}},
						new HashMap<String,String>(),
						new HashMap<String, Object>(){{
							put("property", new CondPropProvider());
							put("mode", new LinkedHashMap<String, String>(){{
								put(String.valueOf(SortingMode.ASC.getValue()),"по возрастанию");
								put(String.valueOf(SortingMode.DESC.getValue()),"по убыванию");
							}});							
						}}
					));

					put("StoredSelectionProvider",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("type","Тип");
							put("hq","Запрос");
						}},
						new HashMap<String,String>(){{
							put("hq","multiLine");
						}},
						new HashMap<String, Object>(){{
							put("type", new LinkedHashMap<String, String>(){{
								put(StoredSelectionProvider.TYPE_SIMPLE,"список");
								put(StoredSelectionProvider.TYPE_MATRIX,"матрица");
								put(StoredSelectionProvider.TYPE_HQL,"запрос");
							}});							
						}}
					));

					put("StoredMatrixEntry",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("comment","Комментарий");
						}},
						new HashMap<String,String>(),
						new HashMap<String, Object>()
					));					
					
					put("StoredKeyValue",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("key","Ключ");
							put("value","Значение");
						}},
						new HashMap<String,String>(),
						new HashMap<String, Object>()			
					));
				}},				
				new EditorTreeContentProvider() {
					
					@Override
					protected boolean hasItemNodeChildren(Object element) {
						if (element instanceof StoredPropertyMeta){
							StoredPropertyMeta el = (StoredPropertyMeta)element;
							return (el.sel_conditions != null && !el.sel_conditions.isEmpty())
								|| (el.selection_provider != null)
								|| (el.sel_sorting != null && !el.sel_sorting.isEmpty());
						}
						
						if (element instanceof StoredSelectionProvider){
							if (StoredSelectionProvider.TYPE_HQL.equals(((StoredSelectionProvider) element).type))
								return ((StoredSelectionProvider) element).parameters != null && !((StoredSelectionProvider) element).parameters.isEmpty();
							else if (StoredSelectionProvider.TYPE_MATRIX.equals(((StoredSelectionProvider) element).type))
								return ((StoredSelectionProvider) element).matrix != null && !((StoredSelectionProvider) element).matrix.isEmpty();
							else
								return((StoredSelectionProvider) element).list != null && !((StoredSelectionProvider) element).list.isEmpty();
						}
						
						if (element instanceof StoredMatrixEntry){
							return (((StoredMatrixEntry) element).conditions != null && !((StoredMatrixEntry) element).conditions.isEmpty())
								|| (((StoredMatrixEntry) element).result != null && !((StoredMatrixEntry) element).result.isEmpty());							
						}
						
						if (element instanceof StoredCondition){
								return !(((StoredCondition)element).nestedConditions.isEmpty());
						}
						return false;
					}

					@Override
					protected Object[] getItemNodeChildren(Object item) {
						if (item instanceof StoredPropertyMeta){
							if (
								((StoredPropertyMeta) item).type == MetaPropertyType.REFERENCE.getValue() || 
								((StoredPropertyMeta) item).type == MetaPropertyType.COLLECTION.getValue()
								)
								return new String[]{
									"Условия отбора допустимых значений",
									"Сортировка выборки допустимых значений"
								};
							else if (((StoredPropertyMeta) item).selection_provider != null)
								return new Object[]{((StoredPropertyMeta) item).selection_provider};
							else	
								return new Object[]{};
						}
						else if (item instanceof StoredSelectionProvider){
							if (StoredSelectionProvider.TYPE_HQL.equals(((StoredSelectionProvider) item).type))
								return ((StoredSelectionProvider) item).parameters.toArray();
							else if (StoredSelectionProvider.TYPE_MATRIX.equals(((StoredSelectionProvider) item).type))
								return((StoredSelectionProvider) item).matrix.toArray();
							else
								return((StoredSelectionProvider) item).list.toArray();
						}
						else if (item instanceof StoredMatrixEntry){
							return new String[]{"Условия","Результаты"};							
						}	
						else if (item instanceof StoredCondition){
							return ((StoredCondition)item).nestedConditions.toArray();
						}	
						
						return null;
					}
					
					@Override
					protected Object[] getItemGroupChildren(Object item, String group) {
						if (group.equals("Условия отбора допустимых значений"))
							return ((StoredPropertyMeta)item).sel_conditions.toArray();

						if (group.equals("Сортировка выборки допустимых значений"))
							return ((StoredPropertyMeta)item).sel_sorting.toArray();

						if (group.equals("Условия"))
							return ((StoredMatrixEntry)item).conditions.toArray();
						
						if (group.equals("Результаты"))
							return ((StoredMatrixEntry)item).result.toArray();
						
						return null;
					}
				},
				new EditorTreeLabelProvider() {
					
					@Override
					protected String getEditorItemNodeText(Object item) {
				    	if (item instanceof StoredPropertyMeta)
				    		return ((StoredPropertyMeta)item).caption + " ["+ ((StoredPropertyMeta)item).name +"]";
				    	
				    	if (item instanceof StoredCondition) //!!!
				    		return condToString((StoredCondition)item);
				    	
				    	if (item instanceof StoredSorting)//!!!
				    		return ((StoredSorting) item).property;
				    	
				    	if (item instanceof StoredSelectionProvider)///!!!
				    		return "Список выбора допустимых значений";			    	
				    	
				    	if (item instanceof StoredMatrixEntry)///!!!
				    		return ((StoredMatrixEntry) item).comment;
				    	
				    	if (item instanceof StoredKeyValue)///!!!
				    		return ((StoredKeyValue) item).key;
						
				    	return "";
					}
					
					@Override
					protected Image getEditorItemNodeImage(Object item) {
						return null;
					}
					
					@Override
					protected Image getEditorItemGroupImage(String code) {
						return null;
					}
				},
				new IMenuListener() {
					
					@Override
					public void menuAboutToShow(IMenuManager manager) {
						manager.add(new Action("Новый атрибут"){
							@Override
							public void run() {
								try {
									//TODO
									IWizardDescriptor wd = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(NewPropertyWizard.REGISTRY_ID);
									NewPropertyWizard w = (NewPropertyWizard)wd.createWizard();
									w.setCaller(me);
									w.setNextOrderNumber(findNextOrderNumber());
									w.init(PlatformUI.getWorkbench(), me.collectionSelection("properties"));
									w.setContext((StoredClassMeta)model);
									WizardDialog dialog = new  WizardDialog(w.getShell(), w);
									dialog.setTitle(w.getWindowTitle());
									dialog.open();
								} catch (CoreException e) {
									e.printStackTrace();
								}								
							}							
						});
						IStructuredSelection selection = me.collectionSelection("properties");
						if (selection != null){ 
							if (selection.getFirstElement() instanceof EditorItemNode){
								EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
								Object item = selected.Item; 
								
								if (item instanceof StoredPropertyMeta){
									final StoredPropertyMeta pm = (StoredPropertyMeta)item;
									
									if (pm.type == MetaPropertyType.REFERENCE.getValue() || 
											pm.type == MetaPropertyType.COLLECTION.getValue()){
										manager.add(new Action("Добавить фильтр списка допустимых значений"){
											@Override
											public void run() {
												pm.sel_conditions.add(new StoredCondition());
												me.refreshCollection("properties");
												//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
												me.setDirty();
											}
										});
										
										manager.add(new Action("Добавить сортировку списка допустимых значений"){
											@Override
											public void run() {
												pm.sel_sorting.add(new StoredSorting());
												me.refreshCollection("properties");
												//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
												me.setDirty();
											}
										});																			
									} else {
										manager.add(new Action("Добавить список допустимых значений"){
											@Override
											public void run() {
												pm.selection_provider = new StoredSelectionProvider(new LinkedList<StoredKeyValue>());
												me.refreshCollection("properties");
												//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
												me.setDirty();
											}
										});											
									}
									
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											((StoredClassMeta)model).properties.remove(pm);
											me.refreshCollection("properties");
											//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
											me.setDirty();
										}
									});
								}
								
								if (item instanceof StoredCondition){
									final StoredCondition sc = (StoredCondition)item;
									if(selected.Parent.Parent instanceof EditorItemNode){
										if (((EditorItemNode)selected.Parent.Parent).Item instanceof StoredPropertyMeta){
											final StoredPropertyMeta pm = (StoredPropertyMeta)((EditorItemNode)selected.Parent.Parent).Item;											
											manager.add(new Action("Удалить"){
												@Override
												public void run() {
													pm.sel_conditions.remove(sc);
													me.refreshCollection("properties");
													//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
													me.setDirty();
												}
											});
										}
										
										if (((EditorItemNode)selected.Parent.Parent).Item instanceof StoredMatrixEntry){
											final StoredMatrixEntry sme = (StoredMatrixEntry)((EditorItemNode)selected.Parent.Parent).Item;
											manager.add(new Action("Удалить"){
												@Override
												public void run() {
													sme.conditions.remove(sc);
													me.refreshCollection("properties");
													//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
													me.setDirty();
												}
											});									
										}
									}
									
									if(selected.Parent.Parent instanceof EditorItemGroup){
										//
										if (((EditorItemNode)selected.Parent.Parent.Parent).Item instanceof StoredPropertyMeta){
											final StoredCondition parentSC = (StoredCondition)((EditorItemNode)selected.Parent).Item;
											
											manager.add(new Action("Удалить"){
												@Override
												public void run() {
													parentSC.nestedConditions.remove(sc);
													me.refreshCollection("properties");
													me.setDirty();
												}
											});
											
										}
									}
										
									manager.add(new Action("Добавить операнд"){
										@Override
										public void run() {
											sc.nestedConditions.add(new StoredCondition());
											me.refreshCollection("properties");
											me.setDirty();
										}
									});
								}
								
								if (item instanceof StoredSorting){
									final StoredSorting sc = (StoredSorting)item;
									final StoredPropertyMeta pm = (StoredPropertyMeta)((EditorItemNode)selected.Parent.Parent).Item;
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											pm.sel_sorting.remove(sc);
											me.refreshCollection("properties");
											//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
											me.setDirty();
										}
									});										
								}
								
								if (item instanceof StoredSelectionProvider){
									final StoredSelectionProvider sc = (StoredSelectionProvider)item;
									final StoredPropertyMeta pm = (StoredPropertyMeta)((EditorItemNode)selected.Parent).Item;
									
									if (sc.type != null){
										if (sc.type.equals(StoredSelectionProvider.TYPE_MATRIX)){
											manager.add(new Action("Добавить вектор"){
												@Override
												public void run() {
													sc.matrix.add(new StoredMatrixEntry());
													me.refreshCollection("properties");
													//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
													me.setDirty();
												}
											});										
										} else if (sc.type.equals(StoredSelectionProvider.TYPE_HQL)) {
											manager.add(new Action("Добавить параметр запроса"){
												@Override
												public void run() {
													sc.parameters.add(new StoredKeyValue("", ""));
													me.refreshCollection("properties");
													//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
													me.setDirty();
												}
											});											
										} else if (sc.type.equals(StoredSelectionProvider.TYPE_SIMPLE)) {
											manager.add(new Action("Добавить ключ-значение"){
												@Override
												public void run() {
													sc.list.add(new StoredKeyValue("", ""));
													me.refreshCollection("properties");
													//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
													me.setDirty();
												}
											});											
										}					
									}
									
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											pm.selection_provider = null;
											me.refreshCollection("properties");
											//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
											me.setDirty();
										}
									});									
								}

								if (item instanceof StoredMatrixEntry){
									final StoredMatrixEntry sc = (StoredMatrixEntry)item;
									final StoredSelectionProvider pm = (StoredSelectionProvider)((EditorItemNode)selected.Parent).Item;
									
									manager.add(new Action("Добавить условие"){
										@Override
										public void run() {
											sc.conditions.add(new StoredCondition());
											me.refreshCollection("properties");
											//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
											me.setDirty();
										}
									});	
									
									manager.add(new Action("Добавить строку результата"){
										@Override
										public void run() {
											sc.result.add(new StoredKeyValue("", ""));
											me.refreshCollection("properties");
											//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
											me.setDirty();
										}
									});										
									
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											pm.matrix.remove(sc);
											me.refreshCollection("properties");
											//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
											me.setDirty();
										}
									});										
								}
								
								if (item instanceof StoredKeyValue){
									final StoredKeyValue sc = (StoredKeyValue)item;
									if (selected.Parent instanceof EditorItemNode){
										final StoredSelectionProvider pm = (StoredSelectionProvider)((EditorItemNode)selected.Parent).Item;
										manager.add(new Action("Удалить"){
											@Override
											public void run() {
												pm.parameters.remove(sc);
												me.refreshCollection("properties");
												//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
												me.setDirty();
											}
										});									
									}
									
									if (selected.Parent instanceof EditorItemGroup){
										final StoredMatrixEntry pm = (StoredMatrixEntry)((EditorItemNode)selected.Parent.Parent).Item;
										manager.add(new Action("Удалить"){
											@Override
											public void run() {
												pm.result.remove(sc);
												me.refreshCollection("properties");
												//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
												me.setDirty();
											}
										});									
									}										
								}
								
								
								
							}
							
							
						}
					}
				},
				new IDragDropProvider() {
					
					@Override
					public Object OnDrop(List<Object> list, Object target, String buffer, int location) {
  					StoredPropertyMeta dragged = null;
  					int order_number = 0;
  					if(!buffer.equals("none") && target instanceof EditorItemNode && ((EditorItemNode)target).Item instanceof StoredPropertyMeta){							
  						switch(location){
			  				case 1 :
			  					order_number = ((StoredPropertyMeta)((EditorItemNode)target).Item).order_number;
			  		      break;
			  		    case 2 :
			  		    	order_number = ((StoredPropertyMeta)((EditorItemNode)target).Item).order_number+1;
			  		      break;
			  		    case 3 :
			  		    	order_number = ((StoredPropertyMeta)((EditorItemNode)target).Item).order_number+1;
			  		      break;
			  		    case 4 :
			  		    	order_number = ((StoredPropertyMeta)list.get(list.size()-1)).order_number+1;
			  		      break;
			    		}					   
  					} else
  						return null;
  				  for(int i=0;i<list.size();i++){
  			    	Object li = list.get(i);
  			    	if(li instanceof StoredPropertyMeta){
  			    		if(buffer.equals(((StoredPropertyMeta)list.get(i)).name))
  			    				dragged = (StoredPropertyMeta)list.get(i);
    		    	} else 
    		    		return null;
  				 	}
						if(dragged != null){
							dragged.order_number = order_number;
							excludeDuplicatedOrderNumbers(dragged, list);
						}
						return dragged;						
					}
					
					private void excludeDuplicatedOrderNumbers(StoredPropertyMeta u, List<Object> list){
						for(Object o:list){
							if(o instanceof StoredPropertyMeta){
								StoredPropertyMeta spm = (StoredPropertyMeta)o;
  							if(!spm.name.equals(u.name) && spm.order_number == u.order_number){
  								spm.order_number = spm.order_number+1;
  								excludeDuplicatedOrderNumbers(spm, list);
  							}
							}
						}
					}
					
					@Override
					public String OnDrag(Object o) {
						String result = "none";
						if(o instanceof EditorItemNode){
							EditorItemNode ein = (EditorItemNode)o;
							if(ein.Item instanceof StoredPropertyMeta)
								result = ((StoredPropertyMeta)ein.Item).name;
						}
						return result;
					}
				}	
		});	
		
		lists.put("compositeIndexes", new Object[]{
				"Индексация",
				new TreeMap<String, FormSettings>(){{
					
					put("StoredCompositeIndex",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("properties","Атрибуты");
							put("unique","Уникальный");
						}},
						new HashMap<String,String>(){{
							put("properties","set");
							put("unique","boolean");
						}},
						new HashMap<String, Object>(){{
							put("properties", new ClassPropertiesProvider((StoredClassMeta)model));
						}}
					));
				}},				
				new EditorTreeContentProvider() {
					
					@Override
					protected boolean hasItemNodeChildren(Object element) {
						return false;
					}

					@Override
					protected Object[] getItemNodeChildren(Object item) {
						return null;
					}
					
					@Override
					protected Object[] getItemGroupChildren(Object item, String group) {
						return null;
					}
				},
				new EditorTreeLabelProvider() {
					
					@Override
					protected String getEditorItemNodeText(Object item) {
				    	if (item instanceof StoredCompositeIndex){
				    		String result = "";
				    		if (((StoredCompositeIndex) item).properties != null){
				    			result = String.join("-", ((StoredCompositeIndex) item).properties);
				    		}
				    		return result;
				    	}
				    	return "";
					}
					
					@Override
					protected Image getEditorItemNodeImage(Object item) {
						return null;
					}
					
					@Override
					protected Image getEditorItemGroupImage(String code) {
						return null;
					}
				},
				new IMenuListener() {
					
					@Override
					public void menuAboutToShow(IMenuManager manager) {
						manager.add(new Action("Новый составной индекс"){
							@Override
							public void run() {
								if (((StoredClassMeta)model).compositeIndexes == null){
									((StoredClassMeta)model).compositeIndexes = new LinkedList<StoredCompositeIndex>();
								}
								
								StoredCompositeIndex ind = new StoredCompositeIndex();
								ind.properties = new LinkedList<String>();
								ind.unique = false;
								((StoredClassMeta)model).compositeIndexes.add(new StoredCompositeIndex());
								me.refreshCollection("compositeIndexes");
								me.setDirty();
							}							
						});
						IStructuredSelection selection = me.collectionSelection("properties");
						if (selection != null){ 
							if (selection.getFirstElement() instanceof EditorItemNode){
								EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
								Object item = selected.Item; 
								
								if (item instanceof StoredCompositeIndex){
									final StoredCompositeIndex ci = (StoredCompositeIndex)item;
									
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											((StoredClassMeta)model).compositeIndexes.remove(ci);
											me.refreshCollection("compositeIndexes");
											//me.setCollectionInput("properties", ((StoredClassMeta)model).properties);
											me.setDirty();
										}
									});
								}
							}
							
							
						}
					}
				},
				null
		});		
	}

	@Override
	protected String formPartName() {
		if (model != null)
			return "Сущность: "+((StoredClassMeta)model).caption;
		return "";
	}
	
	private Integer findNextOrderNumber(){
		StoredPropertyMeta[] propertylist = new StoredPropertyMeta[((StoredClassMeta)model).properties.size()];
		propertylist = ((StoredClassMeta)model).properties.toArray(propertylist);
		Integer maxOrderNumber = 0;
		if(propertylist == null){
			maxOrderNumber = 10;
		}else{
			for(StoredPropertyMeta property : propertylist){
				if(property.order_number > maxOrderNumber) maxOrderNumber = property.order_number;
			}
			if(maxOrderNumber == 0 || maxOrderNumber % 10 == 0){
				maxOrderNumber += 10;
			}else{
				maxOrderNumber += 10 - (maxOrderNumber % 10);
			}
		}
		return maxOrderNumber;
	}
}
