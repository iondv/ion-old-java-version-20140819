package ion.modeler.editors;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;
import ion.modeler.forms.IKeyValueProvider;
import ion.viewmodel.plain.StoredCollectionFilter;
import ion.viewmodel.plain.StoredColumn;
import ion.viewmodel.plain.StoredField;
import ion.viewmodel.plain.StoredFormViewModel;
import ion.viewmodel.plain.StoredAction;
import ion.viewmodel.plain.StoredTab;
import ion.viewmodel.view.ViewApplyMode;
import ion.viewmodel.view.CollectionFieldMode;
import ion.viewmodel.view.FieldSize;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.ReferenceFieldMode;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

public class FormViewEditor extends ViewModelEditor {
	
	public static final String ID = "ion.modeler.editors.formEditor";
	
	@SuppressWarnings("serial")
	private static final Map<String, String> typeNames = new HashMap<String, String>() {{
		put("create", "создания");
		put("item", "редактирования");
		put("detail", "детализации");
	}};

	// commands вместо actions для обратной совместимости со старыми actions
	private static final String ACTIONS = "commands";
	
	
	class CollectionFilterOptPropProvider implements IKeyValueProvider {
		
		private String getClassContext(EditorTreeNode n, Composer c) throws IOException{
			if (n.Parent == null)
				return className;
			
			if (n instanceof EditorItemNode){
				Object item = ((EditorItemNode) n).Item;
				if (item instanceof StoredKeyValue){
					String cn = getClassContext(n.Parent,c);
					String pn = ((StoredKeyValue) item).key;
					if (pn != null){
						StoredClassMeta cm = c.getClass(cn);
							while (cm != null){
								for (StoredPropertyMeta pm: cm.properties)
									if (pm.name != null && pm.name.equals(pn)){
										if (pm.type == MetaPropertyType.REFERENCE.getValue() 
												|| pm.type == MetaPropertyType.STRUCT.getValue())
											return pm.ref_class;
										if (pm.type == MetaPropertyType.COLLECTION.getValue())
											return pm.items_class;
									}
								if (cm.ancestor != null && !cm.ancestor.isEmpty())
									cm = c.getClass(cm.ancestor);
								else
									cm = null;
							}
					}
				}
			}
			
			return getClassContext(n.Parent, c); 
		}
		
		
		
		@Override
		public Map<String, String> Provide(Object model) {
			Composer c = getComposer();
			IStructuredSelection selection = collectionSelection("collectionFilters");
			if (selection != null && selection.getFirstElement() instanceof EditorItemNode){
				StoredClassMeta cm = null;
				try {
					cm = c.getClass(getClassContext((EditorTreeNode)selection.getFirstElement(), c));
					return getClassProperties(c, cm, null, null, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return new HashMap<String, String>();
		}		
	}	
	
	public FormViewEditor() {
		plainClass = StoredFormViewModel.class;
	}
	
	@Override
	protected String formPartName() {
		if (model != null) {
			return String.format("Форма %s: %s", typeNames.get(type), super.formPartName());
		}
		return "";
	}
		
	@SuppressWarnings("serial")
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = super.getSelections(classname);
		if (classname.equals("StoredFormViewModel")){
			selections.put("siblingFixBy", new PropProvider(null));
			selections.put("siblingNavigateBy", new PropProvider(null));			
			selections.put("overrideMode", new LinkedHashMap<String, String>(){{
				put(String.valueOf(ViewApplyMode.HIDE.getValue()), "Перекрыть");
				put(String.valueOf(ViewApplyMode.OVERRIDE.getValue()), "Переопределить");
			}});
		}
		return selections;
	}	
	
	@SuppressWarnings("serial")
	protected void formProperties(){
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("version", "Версия");
		captions.put("siblingFixBy", "Отбор смежных объектов по");
		captions.put("siblingNavigateBy", "Переход к смежному объекту по");
		captions.put("overrideMode", "Режим наложения");
		formSettings.put("StoredFormViewModel",
				new FormSettings(captions, new HashMap<String, String>(){{
					put("siblingFixBy", "set");
					put("siblingNavigateBy","set");
				}}, getSelections("StoredFormViewModel")));
	}
	
	protected void masterSetup(String name, TreeViewer viewer){
		if (name.equals("tabs")){
			viewer.setComparator(new ItemComparator());
		}
	}

	@SuppressWarnings("serial")
	protected void formLists(){
		final FormViewEditor me = this;
		
		lists.put("tabs", new Object[]{
				"Вкладки",
				new TreeMap<String, FormSettings>(){{

					put("StoredTab",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("caption","Имя вкладки");
						}},
						new HashMap<String, String>(),
						new HashMap<String, Object>()
					));					
					
					put("StoredField",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("caption","Имя поля");
							put("hint","Подсказка");
							put("property","Атрибут");
							put("order_number", "Порядковый номер");
							put("size","Размер");
							put("type","Тип поля");
							put("mode","Режим отображения");
							put("hierarchyAttributes","Иерархия по");
							put("selection_paginated","Постраничный список выбора");
							put("readonly","Только чтение");
							put("maskName","Имя маски");
							put("mask","Маска");
							put("required","Обязательное");
							put("visibility","Условия отображения");
							put("enablement","Условия активности");
							put("obligation","Условия обязательности");
							put("validators","Валидаторы");
							put("tags","Теги");
						}},
						new HashMap<String, String>(){{
							put("hierarchyAttributes","set");
							put("required","boolean");
							put("visibility","multiline");
							put("enablement","multiline");
							put("obligation","multiline");
							put("validators","set");
							put("readonly","boolean");
							put("selection_paginated","boolean");
							put("tags","commaSeparated");
						}},
						new HashMap<String, Object>(){{
							put("property", new FieldPropProvider("tabs",null));
							put("type",new LinkedHashMap<String,String>(){{
								put(String.valueOf(FieldType.TEXT.getValue()),"Текстовое");
								put(String.valueOf(FieldType.COMBO.getValue()),"Выпадающий список");
								put(String.valueOf(FieldType.CHECKBOX.getValue()),"Флаг");
								put(String.valueOf(FieldType.DATE_PICKER.getValue()),"Выбор даты");
								put(String.valueOf(FieldType.DATETIME_PICKER.getValue()),"Выбор даты-времени");
								put(String.valueOf(FieldType.FILE.getValue()),"Выбор файла");
								put(String.valueOf(FieldType.IMAGE.getValue()),"Выбор изображения");
								put(String.valueOf(FieldType.ATTACHMENTS.getValue()),"Прикрепленные файлы");
								put(String.valueOf(FieldType.MULTILINE.getValue()),"Многострочный текст");
								put(String.valueOf(FieldType.WYSIWYG.getValue()),"Форматированный текст");
								put(String.valueOf(FieldType.RADIO.getValue()),"Альтернативный выбор");
								put(String.valueOf(FieldType.MULTISELECT.getValue()),"Множественный выбор");
								put(String.valueOf(FieldType.PASSWORD.getValue()),"Пароль");
								put(String.valueOf(FieldType.NUMBER_PICKER.getValue()),"Редактор целых чисел");
								put(String.valueOf(FieldType.DECIMAL_EDITOR.getValue()),"Редактор вещественных чисел");
								put(String.valueOf(FieldType.GROUP.getValue()),"Группа");
								put(String.valueOf(FieldType.REFERENCE.getValue()),"Ссылка");
								put(String.valueOf(FieldType.COLLECTION.getValue()),"Коллекция");
								put(String.valueOf(FieldType.PERIOD_PICKER.getValue()),"Выбор периода");
								put(String.valueOf(FieldType.GEO.getValue()),"Геообъект");
								put(String.valueOf(FieldType.URL.getValue()),"URL");
							}});
							put("size",new LinkedHashMap<String,String>(){{
								put(String.valueOf(FieldSize.TINY.getValue()),"Малый");
								put(String.valueOf(FieldSize.SHORT.getValue()),"Меньше среднего");
								put(String.valueOf(FieldSize.MEDIUM.getValue()),"Средний");
								put(String.valueOf(FieldSize.LONG.getValue()),"Больше среднего");
								put(String.valueOf(FieldSize.BIG.getValue()),"Большой");
							}});							
							put("mode", new FieldModeProvider());							
							put("hierarchyAttributes", new FieldPropProvider("tabs", MetaPropertyType.REFERENCE));
							put("validators", getValidatorsList());
						}}
					));
					
					put("StoredColumn",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("caption","Заголовок");
							put("property","Атрибут");							
							put("type","Тип поля");
							put("selection_paginated","Постраничный список выбора");
							put("sorted","Разрешена сортировка");
							put("order_number", "Порядковый номер");
							put("readonly","Только чтение");
						}},
						new HashMap<String,String>(){{
							put("sorted","boolean");
							put("readonly","boolean");
							put("selection_paginated","boolean");
						}},
						new HashMap<String, Object>(){{
							put("property", new FieldPropProvider("tabs",null));
							put("type",new LinkedHashMap<String,String>(){{
								put(String.valueOf(FieldType.TEXT.getValue()),"Текстовое");
								put(String.valueOf(FieldType.COMBO.getValue()),"Выпадающий список");
								put(String.valueOf(FieldType.CHECKBOX.getValue()),"Флаг");
								put(String.valueOf(FieldType.DATE_PICKER.getValue()),"Выбор даты");
								put(String.valueOf(FieldType.DATETIME_PICKER.getValue()),"Выбор даты-времени");
								put(String.valueOf(FieldType.FILE.getValue()),"Выбор файла");
								put(String.valueOf(FieldType.IMAGE.getValue()),"Выбор изображения");
								put(String.valueOf(FieldType.MULTILINE.getValue()),"Многострочный текст");
								put(String.valueOf(FieldType.WYSIWYG.getValue()),"Форматированный текст");
								put(String.valueOf(FieldType.RADIO.getValue()),"Альтернативный выбор");
								put(String.valueOf(FieldType.MULTISELECT.getValue()),"Множественный выбор");
								put(String.valueOf(FieldType.PASSWORD.getValue()),"Пароль");
								put(String.valueOf(FieldType.NUMBER_PICKER.getValue()),"Редактор целых чисел");
								put(String.valueOf(FieldType.DECIMAL_EDITOR.getValue()),"Редактор вещественных чисел");
								put(String.valueOf(FieldType.GROUP.getValue()),"Группа");
								put(String.valueOf(FieldType.REFERENCE.getValue()),"Ссылка");
								put(String.valueOf(FieldType.COLLECTION.getValue()),"Коллекция");
								put(String.valueOf(FieldType.PERIOD_PICKER.getValue()),"Выбор периода");
								put(String.valueOf(FieldType.GEO.getValue()),"Геообъект");								
								put(String.valueOf(FieldType.URL.getValue()),"URL");
							}});
						}}
					));
					
					put("StoredAction",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("id","Код");
							put("caption","Имя");
							put("visibilityCondition", "Условие видимости");
							put("enableCondition","Условие активности");
							put("isBulk","Пакетная операция");
						}},
						new HashMap<String, String>(){{
							put("visibilityCondition","multiline");
							put("isBulk","boolean");
							
						}},
						new HashMap<String, Object>()
					));
				}},				
				new EditorTreeContentProvider() {
					
					@Override
					protected boolean hasItemNodeChildren(Object element) {
						if (element instanceof StoredTab){
							return !(((StoredTab) element).shortFields != null 
									&& ((StoredTab) element).shortFields.isEmpty())
							|| !(((StoredTab) element).fullFields != null 
							&& ((StoredTab) element).fullFields.isEmpty());
						}

						if (element instanceof StoredField){
							return (((StoredField) element).columns != null && !((StoredField) element).columns.isEmpty())
							|| (((StoredField) element).fields != null && !((StoredField) element).fields.isEmpty())
							|| (((StoredField) element).commands != null && !((StoredField) element).commands.isEmpty());
						}
						return false;
					}
					
					@Override
					protected Object[] getItemNodeChildren(Object item) {
						if (item instanceof StoredTab){
							return new Object[]{
								"Краткий вид","Полный вид"
							};
						}
						
						if (item instanceof StoredField){
							Collection<Object> result = new LinkedList<Object>();
							if (((StoredField) item).commands != null && !((StoredField) item).commands.isEmpty())
								result.add("Действия");
							if (((StoredField) item).type == FieldType.COLLECTION.getValue())
								result.addAll(((StoredField) item).columns);
							else if (((StoredField) item).type == FieldType.GROUP.getValue()||
										(((StoredField) item).type == FieldType.REFERENCE.getValue() && ((StoredField) item).mode == ReferenceFieldMode.INFO.getValue()))
								result.addAll(((StoredField) item).fields);
							return result.toArray();
						}						
						return null;
					}
					
					@Override
					protected Object[] getItemGroupChildren(Object item, String group) {
						if (group.equals("Краткий вид"))
							return ((StoredTab)item).shortFields.toArray();
						
						if (group.equals("Полный вид"))
							return ((StoredTab)item).fullFields.toArray();
						
						if (item instanceof StoredField && group.equals("Действия")){
							if (((StoredField)item).commands != null)
								return ((StoredField)item).commands.toArray();
						}
						
						return null;
					}
				},
				new EditorTreeLabelProvider() {
					
					@Override
					protected String getEditorItemNodeText(Object item) {
				    	if (item instanceof StoredTab)
				    		return ((StoredTab) item).caption;						
						
				    	if (item instanceof StoredField)
				    		return ((StoredField) item).caption;
				    	
				    	if (item instanceof StoredColumn)
				    		return ((StoredColumn) item).caption;				    	

				    	if (item instanceof StoredAction)
				    		return ((StoredAction) item).caption;				    	
				    	
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
						manager.add(new Action("Добавить вкладку"){
							@Override
							public void run() {
								me.setDirty();
								StoredTab st = new StoredTab();
								((StoredFormViewModel)me.getModel()).tabs.add(st);
								me.refreshCollection("tabs");
								me.setPageDetailModel("tabs", st);
							}
						});						
						
						
						final IStructuredSelection selection = me.collectionSelection("tabs");					
						if (selection != null){ 
							if (selection.getFirstElement() instanceof EditorItemGroup){
								EditorItemGroup selected = (EditorItemGroup) selection.getFirstElement();
								final StoredTab st = (StoredTab)((EditorItemNode)selected.Parent).Item;
								if (selected.Type.equals("Краткий вид")){
									manager.add(new Action("Добавить поле"){
										@Override
										public void run() {
											me.setDirty();
											StoredField sf = new StoredField("", "", "");
											st.shortFields.add(sf);
											me.refreshCollection("tabs");
											me.setPageDetailModel("tabs", sf);
										}
									});									
								} else if (selected.Type.equals("Полный вид")){
									manager.add(new Action("Добавить поле"){
										@Override
										public void run() {
											me.setDirty();
											StoredField sf = new StoredField("", "", "");
											st.fullFields.add(sf);
											me.refreshCollection("tabs");
											me.setPageDetailModel("tabs", sf);
										}
									});																		
								}
							}
							else
							if (selection.getFirstElement() instanceof EditorItemNode){
								EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
								Object item = selected.Item;								
								
								if (item instanceof StoredTab){
									final StoredTab sc = (StoredTab)item;
									
									manager.add(new Action("Добавить поле в краткий вид"){
										@Override
										public void run() {
											me.setDirty();
											StoredField sf = new StoredField("", "", "");
											sc.shortFields.add(sf);
											me.refreshCollection("tabs");
											me.setPageDetailModel("tabs", sf);
										}
									});									
							
									manager.add(new Action("Добавить поле в полный вид"){
										@Override
										public void run() {
											me.setDirty();
											StoredField sf = new StoredField("", "", "");
											sc.fullFields.add(sf);
											me.refreshCollection("tabs");
											me.setPageDetailModel("tabs", sf);
										}
									});									
																		
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											me.setDirty();
											((StoredFormViewModel)me.getModel()).tabs.remove(sc);
											me.refreshCollection("tabs");
										}
									});									
								}
								
								if (item instanceof StoredField){
									final StoredField sc = (StoredField)item;

									manager.add(new Action("Добавить действие"){
										@Override
										public void run() {
											me.setDirty();
											StoredAction sa = new StoredAction("", "", "", "", false, false, false, false);
											if (sc.commands == null)
												sc.commands = new LinkedList<StoredAction>();
											sc.commands.add(sa);
											me.refreshCollection("tabs");
											me.setPageDetailModel("tabs", sa);
										}
									});										
									
									if (
										sc.type == FieldType.GROUP.getValue()
										||
										(
											sc.type == FieldType.REFERENCE.getValue() 
											&& 
											sc.mode == ReferenceFieldMode.INFO.getValue()
										)
										){
											manager.add(new Action("Добавить поле"){
												@Override
												public void run() {
													me.setDirty();
													StoredField sf = new StoredField("", "", "");
													sc.fields.add(sf);
													me.refreshCollection("tabs");
													me.setPageDetailModel("tabs", sf);
												}
											});											
										}									
									
									if (
										sc.type == FieldType.COLLECTION.getValue() 
										&& (sc.mode == (Integer)CollectionFieldMode.LIST.getValue() 
											|| sc.mode == (Integer)CollectionFieldMode.TABLE.getValue())
									){
										manager.add(new Action("Добавить колонку"){
											@Override
											public void run() {
												me.setDirty();
												StoredColumn sf = new StoredColumn("", "", "");
												sc.columns.add(sf);
												me.refreshCollection("tabs");
												me.setPageDetailModel("tabs", sf);
											}
										});											
									}									
									
									if (selected.Parent != null){
										if (selected.Parent instanceof EditorItemNode)
											if (((EditorItemNode)selected.Parent).Item instanceof StoredField){
												final StoredField pf = (StoredField)((EditorItemNode)selected.Parent).Item;
												manager.add(new Action("Удалить"){
													@Override
													public void run() {
														me.setDirty();
														pf.fields.remove(sc);
														me.refreshCollection("tabs");
													}
												});		
											}
										
										if (selected.Parent instanceof EditorItemGroup){
											final StoredTab pt = (StoredTab)((EditorItemNode)selected.Parent.Parent).Item;
											if (((EditorItemGroup)selected.Parent).Type.equals("Краткий вид")){
												manager.add(new Action("Удалить"){
													@Override
													public void run() {
														me.setDirty();
														pt.shortFields.remove(sc);
														me.refreshCollection("tabs");
													}
												});		
											} else	
											if (((EditorItemGroup)selected.Parent).Type.equals("Полный вид")){
												manager.add(new Action("Удалить"){
													@Override
													public void run() {
														me.setDirty();
														pt.fullFields.remove(sc);
														me.refreshCollection("tabs");
													}
												});		
											}	
										}
									}
								}
								
								if (item instanceof StoredColumn){
									final StoredColumn sc = (StoredColumn)item;
									if (selected.Parent != null){
										final StoredField pf = (StoredField)((EditorItemNode)selected.Parent).Item;
										manager.add(new Action("Удалить"){
											@Override
											public void run() {
												me.setDirty();
												pf.columns.remove(sc);
												me.refreshCollection("tabs");
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
					public Object OnDrop(List<Object> list, Object target, String buffer,
															 int location) {
						if(!buffer.equals("none") && target instanceof EditorTreeNode && location != 4){
							Collection<StoredField> target_collection = getTargetCollection((EditorTreeNode)target, location);
							
							if(target_collection != null){
								int order_number = 0;
								if(location == 3){
									for(StoredField sf : target_collection)
										if(sf.order_number >= order_number)
											order_number = sf.order_number + 1;
								} else {
									if(location == 1){
										order_number = ((StoredField)((EditorItemNode)target).Item).order_number;
									} else if(location == 2){
										order_number = ((StoredField)((EditorItemNode)target).Item).order_number+1;
									}
								}
								
								Object[] s = getDragged(list, buffer.split("\\."));
								StoredField dragged = (StoredField)s[0];
								@SuppressWarnings("unchecked")
								Collection<StoredField> dragged_collection = (Collection<StoredField>)s[1];
								
								if(dragged != null && dragged_collection != null){  								      								
  								if(dragged_collection != null){
  									if(!dragged_collection.equals(target_collection)){
  										dragged_collection.remove(dragged);
  										target_collection.add(dragged);
  									}
  									dragged.order_number = order_number;
    								excludeDuplicatedOrderNumbers(dragged, target_collection);
  								}
								}
							}
						}
						return null;
					}
					
					private String fullFieldsType = "Полный вид";
					private String shortFieldsType = "Краткий вид";
					
					private Collection<StoredField> getTargetCollection(EditorTreeNode target, int location){
						Collection<StoredField> target_collection  = null;
						if(target instanceof EditorItemGroup){
							EditorItemGroup g = (EditorItemGroup)target;
							if(g.Parent instanceof EditorItemNode && ((EditorItemNode)g.Parent).Item instanceof StoredTab){
								if(location == 3){
									if(g.Type.equals(fullFieldsType))
										target_collection = ((StoredTab)((EditorItemNode)g.Parent).Item).fullFields;
									else if(g.Type.equals(shortFieldsType))
										target_collection = ((StoredTab)((EditorItemNode)g.Parent).Item).shortFields;
								}
							}
						}	else if(target instanceof EditorItemNode) {
							EditorItemNode n = (EditorItemNode)target;
							if(n.Item instanceof StoredTab){
								if(location == 3){
									target_collection = ((StoredTab)n.Item).fullFields;
								}
							} else if(n.Item instanceof StoredField){
								if(location == 1 || location == 2){
									if(n.Parent instanceof EditorItemGroup){
										EditorItemGroup g = (EditorItemGroup)n.Parent;
										if(g.Parent instanceof EditorItemNode && ((EditorItemNode)g.Parent).Item instanceof StoredTab){
											if(g.Type.equals(fullFieldsType))
	  										target_collection = ((StoredTab)((EditorItemNode)g.Parent).Item).fullFields;
	  									else if(g.Type.equals(shortFieldsType))
	  										target_collection = ((StoredTab)((EditorItemNode)g.Parent).Item).shortFields;
										}
									} else if(n.Parent instanceof EditorItemNode){
										if(((EditorItemNode)n.Parent).Item instanceof StoredField){
											target_collection = ((StoredField)((EditorItemNode)n.Parent).Item).fields;
										}
									}
								} else if(location == 3){
									target_collection = ((StoredField)n.Item).fields;
								}
							}
						}
						return target_collection;
					}
					
					private Object[] getDragged(Collection<Object> list, String[] path){
						StoredField dragged = null;
						Collection<?> temp = list;
						for(int i = 0; i < path.length; i++){							
							for(Object o : temp){
								if(o instanceof StoredTab){
									if(((StoredTab)o).caption.equals(path[i])){
										if(i < path.length - 1){
											i++;												
											if(path[i].equals(fullFieldsType)){
												temp = ((StoredTab)o).fullFields;
											} else if(path[i].equals(shortFieldsType)){
												temp = ((StoredTab)o).shortFields;
											}
										}
										break;
									}
								} else if(o instanceof StoredField){
									String[] t = path[i].split("\\|");
									String c = t[0];
									String p = (t.length > 1)?t[1]:"";
									StoredField f = (StoredField)o;
									if(f.caption.equals(c) && f.property.equals(p)){
										if(i == path.length-1){
											dragged = f;											
										} else
											temp = f.fields;										
										break;
									}
								}
							}
						}
						return new Object[]{dragged, temp};						
					}
					
					private void excludeDuplicatedOrderNumbers(StoredField u, Collection<StoredField> list){
						for(StoredField sf:list){
							if(!(sf.caption.equals(u.caption) && sf.property.equals(u.property)) && sf.order_number == u.order_number){
								sf.order_number = sf.order_number+1;
								excludeDuplicatedOrderNumbers(sf, list);
							}
						}
					}
					
					@Override
					public String OnDrag(Object o) {
						String result = "none";
						if(o instanceof EditorItemNode)
							if(((EditorItemNode)o).Item instanceof StoredField)
								result = getNodePath((EditorItemNode)o);
						return result;
					}
										
					private String getNodePath(EditorTreeNode n){
						String result = "";
						if(n.Parent != null)
							result += getNodePath(n.Parent)+".";
						if(n instanceof EditorItemNode){
							if(((EditorItemNode)n).Item instanceof StoredField)
								result += ((StoredField)((EditorItemNode)n).Item).caption+"|"+((StoredField)((EditorItemNode)n).Item).property;
							else if(((EditorItemNode)n).Item instanceof StoredTab)
								result += ((StoredTab)((EditorItemNode)n).Item).caption;
						} else if(n instanceof EditorItemGroup)
							result += ((EditorItemGroup)n).Type;							
						return result;
					}
				}
		});
		addActionsTab(me, lists);
		addCollFiltersTab(me, lists);
	}

	@SuppressWarnings("serial")
	static void addActionsTab(final ViewModelEditor me, Map<String, Object[]> lists) {
		lists.put(ACTIONS, new Object[] {
				"Действия",
				new TreeMap<String, FormSettings>(){{
					put("StoredAction",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("id","Код");
							put("caption","Имя");
							put("visibilityCondition", "Условие видимости");
							put("enableCondition","Условие активности");
							put("signBefore","ЭП входящих данных");
							put("signAfter","ЭП исходящих данных");
						}},
						new HashMap<String, String>(){{
							put("visibilityCondition","multiline");
							put("enableCondition","multiline");
							put("signBefore","boolean");
							put("signAfter","boolean");
						}},
						new HashMap<String, Object>()
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
				    	if (item instanceof StoredAction)
				    		return ((StoredAction) item).caption;						
						
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
						manager.add(new Action("Добавить действие"){
							@Override
							public void run() {
								me.setDirty();
								StoredAction sa = new StoredAction();
								((StoredFormViewModel)me.getModel()).commands.add(sa);
								me.refreshCollection(ACTIONS);
								me.setPageDetailModel(ACTIONS, sa);
							}
						});						
						
						
						final IStructuredSelection selection = me.collectionSelection(ACTIONS);
						if (selection != null){ 
							Object node = selection.getFirstElement();
							if (node instanceof EditorItemNode) {
								Object item = ((EditorItemNode)node).Item;
								if (item instanceof StoredAction) {
									final StoredAction selected = (StoredAction)item;
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											me.setDirty();
											((StoredFormViewModel)me.getModel()).commands.remove(selected);
											me.refreshCollection(ACTIONS);
										}
									});
								}
							}
						}
					}
				}
				
		});
	}	
	
	
	@SuppressWarnings("serial")
	void addCollFiltersTab(final ViewModelEditor me, Map<String, Object[]> lists) {
		lists.put("collectionFilters", new Object[] {
				"Фильтрация коллекций",
				new TreeMap<String, FormSettings>(){{
					put("StoredCollectionFilter",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("name","Имя");
						}},
						new HashMap<String, String>(),
						new HashMap<String, Object>()
					));						
					put("StoredKeyValue",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
								put("key","Коллекция");
								put("value","Отбор по");
						}},
						new HashMap<String, String>(),
						new HashMap<String, Object>(){{
							put("key",new PropProvider(MetaPropertyType.COLLECTION));
							put("value",new CollectionFilterOptPropProvider());
						}}
					));
				}},				
				new EditorTreeContentProvider() {
					@Override
					protected boolean hasItemNodeChildren(Object element) {
						if (element instanceof StoredCollectionFilter)
							return (((StoredCollectionFilter) element).options != null && !((StoredCollectionFilter) element).options.isEmpty());
						return false;
					}
					
					@Override
					protected Object[] getItemNodeChildren(Object item) {
						if (item instanceof StoredCollectionFilter)
							return ((StoredCollectionFilter) item).options.toArray();
						
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
				    	if (item instanceof StoredCollectionFilter)
				    		return ((StoredCollectionFilter) item).name;						

				    	if (item instanceof StoredKeyValue)
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
						manager.add(new Action("Добавить фильтр"){
							@Override
							public void run() {
								me.setDirty();
								StoredCollectionFilter sa = new StoredCollectionFilter();
								StoredFormViewModel m = ((StoredFormViewModel)me.getModel());
								if (m.collectionFilters == null)
									m.collectionFilters = new LinkedList<StoredCollectionFilter>();
								m.collectionFilters.add(sa);
								me.refreshCollection("collectionFilters");
								me.setPageDetailModel("collectionFilters", sa);
							}
						});						
						
						
						final IStructuredSelection selection = me.collectionSelection("collectionFilters");
						if (selection != null){ 
							Object node = selection.getFirstElement();
							if (node instanceof EditorItemNode) {
								Object item = ((EditorItemNode)node).Item;
								if (item instanceof StoredCollectionFilter) {
									final StoredCollectionFilter selected = (StoredCollectionFilter)item;
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											me.setDirty();
											((StoredFormViewModel)me.getModel()).collectionFilters.remove(selected);
											me.refreshCollection("collectionFilters");
										}
									});
									
									manager.add(new Action("Добавить опцию"){
										@Override
										public void run() {
											me.setDirty();
											StoredKeyValue opt = new StoredKeyValue();
											if (selected.options == null)
												selected.options = new LinkedList<StoredKeyValue>();
											selected.options.add(opt);
											me.refreshCollection("collectionFilters");
											me.setPageDetailModel("collectionFilters", opt);
										}
									});									
								}
								
								if (item instanceof StoredKeyValue) {
									final StoredKeyValue selected = (StoredKeyValue)item;
									final StoredCollectionFilter cf = (StoredCollectionFilter)((EditorItemNode)(((EditorItemNode) node).Parent)).Item;
									
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											me.setDirty();
											cf.options.remove(selected);
											me.refreshCollection("collectionFilters");
										}
									});
								}								
							}
						}
					}
				}
				
		});
	}		
}
