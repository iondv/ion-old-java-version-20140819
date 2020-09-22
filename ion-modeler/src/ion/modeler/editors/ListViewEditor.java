package ion.modeler.editors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import ion.modeler.forms.FormSettings;
import ion.viewmodel.plain.StoredAction;
import ion.viewmodel.plain.StoredListViewModel;
import ion.viewmodel.plain.StoredColumn;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.ViewApplyMode;
import ion.viewmodel.view.ActionType;

public class ListViewEditor extends ViewModelEditor {
	
	public static final String ID = "ion.modeler.editors.listEditor";	
	
	// commands вместо actions для обратной совместимости со старыми actions
	private static final String ACTIONS = "commands";
	
	public ListViewEditor() {
		plainClass = StoredListViewModel.class;
	}

	@Override
	protected String formPartName() {
		if (model != null)
			return "Список: "+ super.formPartName();
		return "";
	}
	
	@SuppressWarnings("serial")
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = super.getSelections(classname);
		if (classname.equals("StoredListViewModel")){
			selections.put("actions", new LinkedHashMap<String, String>(){{
				put(String.valueOf(ActionType.CREATE.getValue()),"Создать");
				put(String.valueOf(ActionType.EDIT.getValue()),"Изменить");
				put(String.valueOf(ActionType.DELETE.getValue()),"Удалить");
				put(String.valueOf(ActionType.REFRESH.getValue()),"Обновить");
			}});
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
		captions.put("allowSearch", "Доступен поиск");
		captions.put("pageSize", "Количество записей на странице");
		captions.put("overrideMode", "Режим наложения");
		captions.put("useEditModels", "Использовать формы редактирования для детализации");
		formSettings.put("StoredListViewModel", new FormSettings(captions, new HashMap<String, String>(){{
			put("allowSearch","Boolean");
			put("useEditModels","Boolean");
		}},getSelections("StoredListViewModel")));
	}
	
	protected void masterSetup(String name, TreeViewer viewer){
		viewer.setComparator(new ItemComparator());
	}
	
	@SuppressWarnings("serial")
	protected void formLists(){
		final ListViewEditor me = this;
		
		lists.put("columns", new Object[]{
				"Колонки",
				new TreeMap<String, FormSettings>(){{
					put("StoredColumn",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("caption","Заголовок");
							put("hint","Подсказка");
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
							put("property", new PropProvider(null));
							put("type",new LinkedHashMap<String,String>(){{
								put(String.valueOf(FieldType.TEXT.getValue()),"Текстовое");
								put(String.valueOf(FieldType.COMBO.getValue()),"Выпадающий список");
								put(String.valueOf(FieldType.CHECKBOX.getValue()),"Флаг");
								put(String.valueOf(FieldType.DATETIME_PICKER.getValue()),"Выбор даты-времени");
								put(String.valueOf(FieldType.DATE_PICKER.getValue()),"Выбор даты");
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
				    	
				    	if (item instanceof StoredColumn)
				    		return ((StoredColumn) item).caption;
				    	
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
						manager.add(new Action("Добавить колонку"){
							@Override
							public void run() {
								me.setDirty();
								StoredColumn sc = new StoredColumn("","","");
								((StoredListViewModel)me.getModel()).columns.add(sc);
								me.refreshCollection("columns");
								me.setPageDetailModel("columns", sc);
							}
						});
						
						IStructuredSelection selection = me.collectionSelection("columns");
						if (selection != null){ 
							if (selection.getFirstElement() instanceof EditorItemNode){
								EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
								Object item = selected.Item; 
								
								if (item instanceof StoredColumn){
									final StoredColumn sc = (StoredColumn)item;
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											me.setDirty();
											((StoredListViewModel)me.getModel()).columns.remove(sc);
											me.refreshCollection("columns");
										}
									});										
								}								
							}
						}
					}
				},
				new IDragDropProvider() {
					
					@Override
					public Object OnDrop(List<Object> list, Object target, String buffer,
															 int location) {
						StoredColumn dragged = null;
  			    int order_number = 0;
  					if(!buffer.equals("none") && target instanceof EditorItemNode && ((EditorItemNode)target).Item instanceof StoredColumn){							
  						switch(location){
			  				case 1 :
			  					order_number = ((StoredColumn)((EditorItemNode)target).Item).order_number;
			  		      break;
			  		    case 2 :
			  		    	order_number = ((StoredColumn)((EditorItemNode)target).Item).order_number+1;
			  		      break;
			  		    case 3 :
			  		    	order_number = ((StoredColumn)((EditorItemNode)target).Item).order_number+1;
			  		      break;
			  		    case 4 :
			  		    	order_number = ((StoredColumn)list.get(list.size()-1)).order_number+1;
			  		      break;
			    		}			   
  					} else
  						return null;
  				  for(int i=0;i<list.size();i++){
  			    	Object li = list.get(i);
  			    	if(li instanceof StoredColumn){
  			    		if(buffer.equals(((StoredColumn)list.get(i)).caption+"|"+((StoredColumn)list.get(i)).property))
  			    				dragged = (StoredColumn)list.get(i);
    		    	} else 
    		    		return null;
  				 	}
						if(dragged != null){
							dragged.order_number = order_number;
							excludeDuplicatedOrderNumbers(dragged, list);
						}
						return dragged;	
					}
					
					private void excludeDuplicatedOrderNumbers(StoredColumn u, List<Object> list){
						for(Object o:list){
							if(o instanceof StoredColumn){
								StoredColumn spm = (StoredColumn)o;
  							if(!(spm.caption.equals(u.caption) && spm.property.equals(u.property)) && spm.order_number == u.order_number){
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
							if(ein.Item instanceof StoredColumn)
								result = ((StoredColumn)ein.Item).caption + "|" + ((StoredColumn)ein.Item).property;
						}
						return result;
					}
				}
		});
		
		addActionsTab(me, lists);
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
							put("isBulk","Групповая");
							
						}},
						new HashMap<String, String>(){{
							put("visibilityCondition","multiline");
							put("enableCondition","multiline");
							put("signBefore","boolean");
							put("signAfter","boolean");
							put("isBulk","boolean");
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
								((StoredListViewModel)me.getModel()).commands.add(new StoredAction());
								me.refreshCollection(ACTIONS);
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
											((StoredListViewModel)me.getModel()).commands.remove(selected);
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
}
