package ion.modeler.editors;

import ion.core.ConditionType;
import ion.core.MetaPropertyType;
import ion.core.OperationType;
import ion.core.SortingMode;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSorting;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;
import ion.modeler.forms.IKeyValueProvider;
import ion.viewmodel.navigation.NodeType;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredPathChain;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

public class NodeEditor extends IonEditor {

	public static final String ID = "ion.modeler.editors.nodeEditor";
	
	protected String sectionName = "";
	
	class CondPropProvider implements IKeyValueProvider {

		@Override
		public Map<String, String> Provide(Object model) {
			Composer c = getComposer();
			StoredClassMeta cm = null;
			try {
				String cn = ((StoredNavNode)getModel()).classname;
				if (cn != null){
					cm = c.getClass(cn);
					if (cm != null){
						Map<String,String> result = new LinkedHashMap<String, String>();
						StoredPropertyMeta[] propertyList = new StoredPropertyMeta[cm.properties.size()];
						propertyList = cm.properties.toArray(propertyList);
						Arrays.sort(propertyList);
						for (StoredPropertyMeta pm : propertyList)
							result.put(pm.name, pm.caption);
						return result;
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

	@Override
	protected Object loadModel(IFile f) throws IOException {
		Composer c = new Composer(f.getProject());
		sectionName = f.getProjectRelativePath().segment(1);
		return c.Read(f.getLocation().toString(), StoredNavNode.class);
	}

	@Override
	protected String mainPageText() {
		return "Узел";
	}	
	
	@SuppressWarnings("serial")
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = super.getSelections(classname);
		
		if (classname.equals("StoredNavNode")){
			selections.put("type", new LinkedHashMap<String,String>(){{
				put(String.valueOf(NodeType.GROUP.getValue()),"Группа");
				put(String.valueOf(NodeType.CLASS.getValue()),"Страница класса");
				put(String.valueOf(NodeType.CONTAINER.getValue()),"Страница контейнера");
				put(String.valueOf(NodeType.HYPERLINK.getValue()),"Гиперссылка");
			}});
			selections.put("classname", getClassSelection());
			selections.put("collection",new ClassPropertiesProvider(getComposer(), "classname", MetaPropertyType.COLLECTION));
		}
		return selections;
	}	
	
	protected void formProperties(){
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("orderNumber", "Порядковый номер");
		captions.put("caption", "Логическое имя");
		captions.put("type", "Тип");
		captions.put("classname", "Класс");
		captions.put("container", "ID контейнера");
		captions.put("collection", "Атрибут коллекции");
		captions.put("url", "URL");
		captions.put("hint", "Подсказка");
		formSettings.put("StoredNavNode", new FormSettings(captions, new HashMap<String, String>(),
				getSelections("StoredNavNode")));
	}

	@Override
	protected String formPartName() {
		if (model != null)
			return "Узел: "+((StoredNavNode)model).caption;
		return "";
	}	
	
	@SuppressWarnings("serial")
	protected void formLists(){
		final NodeEditor me = this;
		
		lists.put("conditions", new Object[]{
				"Условия выборки",
				new TreeMap<String, FormSettings>(){{
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
				}},				
				new EditorTreeContentProvider() {
					
					@Override
					protected boolean hasItemNodeChildren(Object element) {
						if (element instanceof StoredCondition){
							return !(((StoredCondition)element).nestedConditions.isEmpty());
						}
						return false;
					}
					
					@Override
					protected Object[] getItemNodeChildren(Object item) {
						if (item instanceof StoredCondition){
							return ((StoredCondition)item).nestedConditions.toArray();
						}	
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
				    	
				    	if (item instanceof StoredCondition)
				    		return condToString((StoredCondition) item);
				    	
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
						manager.add(new Action("Добавить условие"){
							@Override
							public void run() {
								me.setDirty();
								StoredCondition nc = new StoredCondition();
								((StoredNavNode)me.getModel()).conditions.add(nc);
								me.refreshCollection("conditions");
								me.setPageDetailModel("conditions", nc);
							}
						});
						
						IStructuredSelection selection = me.collectionSelection("conditions");
						if (selection != null){
							if (selection.getFirstElement() instanceof EditorItemNode){
								
								final EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
								Object item = selected.Item; 
								
								if (item instanceof StoredCondition){
									final StoredCondition sc = (StoredCondition)item;

									manager.add(new Action("Добавить операнд"){
										@Override
										public void run() {
											me.setDirty();
											sc.nestedConditions.add(new StoredCondition());
											me.refreshCollection("conditions");
										}
									});
									
									
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											if((EditorItemNode)selected.Parent != null){
												StoredCondition parentSC = (StoredCondition)((EditorItemNode)selected.Parent).Item;
												parentSC.nestedConditions.remove(sc);
											} else {
												((StoredNavNode)me.getModel()).conditions.remove(sc);
											}
											me.setDirty();
											me.refreshCollection("conditions");
										}
									});		
								}								
							}
						}
					}
				}
		});
		
		lists.put("sorting", new Object[]{
				"Cортировка",
				new TreeMap<String, FormSettings>(){{					
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
				    	
				    	if (item instanceof StoredSorting)
				    		return ((StoredSorting) item).property;
						
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
						manager.add(new Action("Добавить сортировку"){
							@Override
							public void run() {
								me.setDirty();
								StoredSorting ns = new StoredSorting();
								((StoredNavNode)me.getModel()).sorting.add(ns);
								me.refreshCollection("sorting");
								me.setPageDetailModel("sorting", ns);
							}
						});						
						
						IStructuredSelection selection = me.collectionSelection("sorting");
						if (selection != null){ 
							if (selection.getFirstElement() instanceof EditorItemNode){
								EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
								Object item = selected.Item; 
								
								if (item instanceof StoredSorting){
									final StoredSorting sc = (StoredSorting)item;
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											me.setDirty();
											((StoredNavNode)me.getModel()).sorting.remove(sc);
											me.refreshCollection("sorting");
										}
									});										
								}								
							}
						}
					}
				}
		});		
		
		lists.put("pathChains", new Object[]{
				"Хлебные крошки",
				new TreeMap<String, FormSettings>(){{					
					put("StoredPathChain",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("class_name","Класс");
							put("path","Путь");
						}},
						new HashMap<String,String>(){{
							put("path","commaSeparated");
						}},
						new HashMap<String, Object>(){{
							put("class_name", new ClassListProvider(me));
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
				    	if (item instanceof StoredPathChain)
				    		return ((StoredPathChain) item).class_name;
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
						manager.add(new Action("Добавить путь"){
							@Override
							public void run() {
								me.setDirty();
								StoredPathChain npc = new StoredPathChain();
								((StoredNavNode)me.getModel()).pathChains.add(npc);
								me.refreshCollection("pathChains");
								me.setPageDetailModel("pathChains", npc);
							}
						});						
						
						IStructuredSelection selection = me.collectionSelection("pathChains");
						if (selection != null){ 
							if (selection.getFirstElement() instanceof EditorItemNode){
								EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
								Object item = selected.Item; 
								
								if (item instanceof StoredPathChain){
									final StoredPathChain sc = (StoredPathChain)item;
									manager.add(new Action("Удалить"){
										@Override
										public void run() {
											me.setDirty();
											((StoredNavNode)me.getModel()).pathChains.remove(sc);
											me.refreshCollection("pathChains");
										}
									});										
								}								
							}
						}
					}
				}
		});			
	}
	
	protected void performSaving(Composer c) throws IOException, CoreException{
		c.saveNavNode(model, sectionName);
	}
}
