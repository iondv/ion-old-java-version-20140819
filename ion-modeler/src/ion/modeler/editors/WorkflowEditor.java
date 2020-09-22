package ion.modeler.editors;

import ion.core.ConditionType;
import ion.core.DACPermission;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredMatrixEntry;
import ion.framework.meta.plain.StoredPermissions;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredPropertyPermissions;
import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.framework.workflow.plain.StoredWorkflowSelectionProvider;
import ion.framework.workflow.plain.StoredWorkflowState;
import ion.framework.workflow.plain.StoredWorkflowTransition;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;
import ion.modeler.wizards.NewWorkflowStateWizard;
import ion.modeler.wizards.NewWorkflowTransitionWizard;

import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class WorkflowEditor extends IonEditor{
	
	public static final String ID = "ion.modeler.editors.workflowEditor";
	
	
	public Map<String, String> getClassRoles(String classname){
		return getClassRoles(classname, true, true);
	}
	
	public Map<String, String> getClassRoles(String classname, boolean pure, boolean structs){
		Map<String, String> result = new LinkedHashMap<String,String>();
		if(classname != null){
			Map<String, Object[]> classes;
			try {
				classes = getComposer().ClassMetas(true);
				Object[] v = classes.get(classname);
				StoredClassMeta c = ((StoredClassMeta)v[1]);
				if ((c.is_struct && structs) || (!c.is_struct && pure)){
					for (StoredPropertyMeta pm: c.properties){
						if (pm.type == MetaPropertyType.USER.getValue())
							result.put(pm.name, pm.caption);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public Map<String, String> getStates(){
		Map<String, String> result = new LinkedHashMap<String,String>();
		for(StoredWorkflowState sws : ((StoredWorkflowModel)model).states){
			result.put(sws.name, sws.name);
		}
		return result;
	}
	
	public Map<String, String> getClassAttributes(String classname){
		return getClassAttributes(classname, true, true);
	}
	
	public Map<String, String> getClassAttributes(String classname, boolean pure, boolean structs){
		Map<String, String> result = new LinkedHashMap<String,String>();
		if(classname != null){
			Map<String, Object[]> classes;
			try {
				classes = getComposer().ClassMetas(true);
				Object[] v = classes.get(classname);
				StoredClassMeta c = ((StoredClassMeta)v[1]);
				if ((c.is_struct && structs) || (!c.is_struct && pure)){
					for (StoredPropertyMeta pm: c.properties){
						result.put(pm.name, pm.caption);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
  protected Object loadModel(IFile f) throws IOException {
		Composer composer = new Composer(f.getProject());
		return composer.Read(f.getLocation().toString(), StoredWorkflowModel.class);
  }
	
	public static Map<String, String> getCaptions() {
		Map<String, String> captions = new LinkedHashMap<String,String>();
		captions.put("caption", "Логическое имя");
		captions.put("wfClass","Класс процесса");
		captions.put("startState", "Начальное состояние");
		return captions;
	}

	protected void formProperties(){
		Map<String, String> captions = getCaptions();
		formSettings.put(StoredWorkflowModel.class.getSimpleName(), new FormSettings(captions, new HashMap<String, String>(), getSelections(StoredWorkflowModel.class.getSimpleName())));
	}
	
	protected Map<String,Object> getSelections(String classname){
		Map<String, String> selectionStates = new LinkedHashMap<String,String>();
		Map<String,Object> selections = super.getSelections(classname);
		StoredWorkflowState[] statesArray = new StoredWorkflowState[((StoredWorkflowModel)model).states.size()];
		statesArray = ((StoredWorkflowModel)model).states.toArray(statesArray);
		for(StoredWorkflowState s : statesArray){
			selectionStates.put(s.name, s.caption);
		}
		selections.put("wfClass",getClassSelection());
		selections.put("startState",selectionStates);
		return selections;
	}

	protected void masterSetup(String name, TreeViewer viewer){
		viewer.setComparator(new ItemComparator());
	}
	
	@Override
  protected String mainPageText() {
	  return "Бизнес процесс";
  }

	@Override
  protected String formPartName() {
		if (model != null)
			return "Бизнес процесс: " + ((StoredWorkflowModel)model).name;
	  return "";
  }
	
	@SuppressWarnings("serial")
	@Override
	protected void formLists() {
		final WorkflowEditor me = this;
		
	  lists.put("states", new Object[]{
 	     "Состояния",
 	     new TreeMap<String, FormSettings>(){{
					put("StoredWorkflowState", new FormSettings(
					    new LinkedHashMap<String,String>(){{
					    	put("name","Системное имя");
					    	put("caption","Логическое имя");
					    }},
					    new HashMap<String,String>(){{
					    	 //properties
					    }},
					    new HashMap<String, Object>(){{
					    	 //selections
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
							put("property", getClassAttributes(((StoredWorkflowModel)model).wfClass));
							put("operation", new LinkedHashMap<String, String>(){{
								put(String.valueOf(ConditionType.EQUAL.getValue()),"=");
								put(String.valueOf(ConditionType.LESS.getValue()),"<");
								put(String.valueOf(ConditionType.MORE.getValue()),">");
								put(String.valueOf(ConditionType.LESS_OR_EQUAL.getValue()),"<=");
								put(String.valueOf(ConditionType.MORE_OR_EQUAL.getValue()),">=");
								put(String.valueOf(ConditionType.NOT_EQUAL.getValue()),"<>");
								put(String.valueOf(ConditionType.EMPTY.getValue()),"пусто");
								put(String.valueOf(ConditionType.NOT_EMPTY.getValue()),"не пусто");
							}});							
						}}
					));
					put("StoredPermissions",
					new FormSettings(	
						new LinkedHashMap<String,String>(){{
							put("role","Роль");
							put("permissions","Разрешения");
						}},
						new HashMap<String,String>(){{
							put("permissions","set");
						}},
						new HashMap<String, Object>(){{
							put("role", getClassRoles(((StoredWorkflowModel)model).wfClass));
							put("permissions", new LinkedHashMap<String, String>(){{
								put(String.valueOf(DACPermission.FULL.getValue()),"Полный доступ");
								put(String.valueOf(DACPermission.READ.getValue()),"Чтение");
								put(String.valueOf(DACPermission.WRITE.getValue()),"Запись");
								put(String.valueOf(DACPermission.DELETE.getValue()),"Удаление");
								put(String.valueOf(DACPermission.USE.getValue()),"Использование");
							}});							
						}}
					));
 	    	 put("StoredPropertyPermissions", new FormSettings(
 	    	     new LinkedHashMap<String,String>(){{
 	    	    	put("property","Атрибут");
 	    	     }},
 	    	     new HashMap<String,String>(){{
 	    	    	 //properties
 	    	     }},
 	    	     new HashMap<String, Object>(){{
 	    	    	put("property", getClassAttributes(((StoredWorkflowModel)model).wfClass));
 	    	     }}
 	    	 ));
 	    	 put("StoredWorkflowSelectionProvider", new FormSettings(
	    	     new LinkedHashMap<String,String>(){{
	    	    	 put("role","Роль");
	    	    	 put("property","Атрибут");
	    	    	 put("type","Тип");
	    	    	 put("hq","Запрос");
	    	    	 put("parameters","Параметры запроса");
	    	     }},
	    	     new HashMap<String,String>(){{
	    	    	 put("hq","multiLine");
	    	    	 put("parameters","table");
	    	     }},
	    	     new HashMap<String, Object>(){{
	    	    	 put("role", getClassRoles(((StoredWorkflowModel)model).wfClass));
	    	    	 put("property", getClassAttributes(((StoredWorkflowModel)model).wfClass));
	    	    	 put("type", new LinkedHashMap<String, String>(){{
	    	    		 put(StoredWorkflowSelectionProvider.TYPE_MATRIX,"матрица");
	    	    		 put(StoredWorkflowSelectionProvider.TYPE_HQL,"запрос");
	    	    		 put(StoredWorkflowSelectionProvider.TYPE_SIMPLE,"простая выборка");
	    	    	 }});
	    	    	 put("parameters", getClassAttributes(((StoredWorkflowModel)model).wfClass));
	    	     }}
	    	 ));
 	    	 put("StoredKeyValue", new FormSettings(
	    	     new LinkedHashMap<String,String>(){{
	    	    	 put("key","Атрибут");
	    	    	 put("value","Значение");
	    	     }},
	    	     new HashMap<String,String>(){{}},
	    	     new HashMap<String, Object>(){{
	    	    	 put("key", getClassAttributes(((StoredWorkflowModel)model).wfClass));
	    	     }}
	    	 ));
 	    	 put("StoredMatrixEntry", new FormSettings(
	    	     new LinkedHashMap<String,String>(){{
	    	    	 put("comment","Комментарий");
	    	     }},
	    	     new HashMap<String,String>(){{}},
	    	     new HashMap<String, Object>(){{}}
	    	 ));
 	    	
 	     }},
 	     new EditorTreeContentProvider() {
 	    	 
 				@Override
 				protected boolean hasItemNodeChildren(Object element) {
 					if(element instanceof StoredWorkflowState){
 						return !((StoredWorkflowState)element).conditions.isEmpty() ||
 								!((StoredWorkflowState)element).itemPermissions.isEmpty() || 
 								!((StoredWorkflowState)element).propertyPermissions.isEmpty() ||
 								!((StoredWorkflowState)element).selectionProviders.isEmpty();
 					}
 					if(element instanceof StoredPropertyPermissions){
 						return !((StoredPropertyPermissions)element).permissions.isEmpty();
 					}
 					if(element instanceof StoredWorkflowSelectionProvider){
 						return !((StoredWorkflowSelectionProvider)element).matrix.isEmpty() ||
 								!((StoredWorkflowSelectionProvider)element).list.isEmpty();
 					}
 					if(element instanceof StoredMatrixEntry){
 						return !((StoredMatrixEntry)element).conditions.isEmpty() ||
 								!((StoredMatrixEntry)element).result.isEmpty();
 					}
 					return false;
 				}
 				
 				@Override
 				protected Object[] getItemNodeChildren(Object item) {
 					if(item instanceof StoredWorkflowState){
 	 					return new Object[]{
 	 							"Условия",
 	 							"Разрешения для объекта",
 	 							"Разрешения для свойств",
 	 							"Выборка допустимых значений"
 	 							};
 					}
 					if(item instanceof StoredPropertyPermissions){
 						return new Object[]{"Разрешения"};
 					}
 					if(item instanceof StoredWorkflowSelectionProvider){
 						return new Object[]{"Матрица","Выборка"};
 					}
 					if(item instanceof StoredMatrixEntry){
 						return new Object[]{"Условия","Результаты"};
 					}
 					return null;
 				}
 				
 				@Override
 				protected Object[] getItemGroupChildren(Object item, String group) {
 					if(group.equals("Условия")){
 						if(item instanceof StoredMatrixEntry){
 							return ((StoredMatrixEntry)item).conditions.toArray();
 						}else if(item instanceof StoredWorkflowState){
 							return ((StoredWorkflowState)item).conditions.toArray();
 						}
 					}
 					if(group.equals("Разрешения для объекта")){
 						return ((StoredWorkflowState)item).itemPermissions.toArray();
 					}
 					if(group.equals("Разрешения для свойств")){
 						return ((StoredWorkflowState)item).propertyPermissions.toArray();
 					}
 					if(group.equals("Выборка допустимых значений")){
 						return ((StoredWorkflowState)item).selectionProviders.toArray();
 					}
 					if(group.equals("Разрешения")){
 						return ((StoredPropertyPermissions)item).permissions.toArray();
 					}
 					if(group.equals("Матрица")){
 						return ((StoredWorkflowSelectionProvider)item).matrix.toArray();
 					}
 					if(group.equals("Выборка")){
 						return ((StoredWorkflowSelectionProvider)item).list.toArray();
 					}
 					if(group.equals("Результаты")){
 						return ((StoredMatrixEntry)item).result.toArray();
 					}
 					return null;
 				}
 			},
 			new EditorTreeLabelProvider() {
 				
 				@Override
 				protected String getEditorItemNodeText(Object item) {
 					if(item instanceof StoredWorkflowState){
 						return ((StoredWorkflowState)item).caption;
 					}
 					if(item instanceof StoredCondition){
 						return ((StoredCondition)item).property;
 					}
 					if(item instanceof StoredPermissions){
 						return ((StoredPermissions)item).role;
 					}
 					if(item instanceof StoredPropertyPermissions){
 						return ((StoredPropertyPermissions)item).property;
 					}
 					if(item instanceof StoredWorkflowSelectionProvider){
 						return ((StoredWorkflowSelectionProvider)item).role + 
 								((((StoredWorkflowSelectionProvider)item).property!=null)?" : "
 								+ ((StoredWorkflowSelectionProvider)item).property:" ") ;
 					}
 					if(item instanceof StoredKeyValue){
 						return ((StoredKeyValue)item).key;
 					}
 					if(item instanceof StoredMatrixEntry){
 						return ((StoredMatrixEntry)item).comment;
 					}
 					return null;
 				}
 				
 				@Override
 				protected Image getEditorItemNodeImage(Object item) {
 					// TODO Auto-generated method stub
 					return null;
 				}
 				
 				@Override
 				protected Image getEditorItemGroupImage(String code) {
 					// TODO Auto-generated method stub
 					return null;
 				}
 			},
 			new IMenuListener() {
 				
 				@Override
 				public void menuAboutToShow(IMenuManager manager) {
 					manager.add(new Action("Новое состояние"){
 						@Override
 						public void run() {
 							try {
 								IWizardDescriptor wd = PlatformUI.getWorkbench().
 										getNewWizardRegistry().
 										findWizard(NewWorkflowStateWizard.REGISTRY_ID);
 								NewWorkflowStateWizard w = (NewWorkflowStateWizard)wd.createWizard();
 								w.init(PlatformUI.getWorkbench(), me.collectionSelection("states"));
 								WizardDialog dialog = new WizardDialog(w.getShell(), w);
 								dialog.setTitle(w.getWindowTitle());
 								w.setContext((StoredWorkflowModel)model);
 								w.setCaller(me);
 								dialog.open();									
 							} catch (CoreException e) {
 								e.printStackTrace();
 							}	
 						}
 					});
 					final IStructuredSelection selection = me.collectionSelection("states");
 					if(selection != null){
 						if(selection.getFirstElement() instanceof EditorItemGroup){
 							EditorItemGroup selected = (EditorItemGroup) selection.getFirstElement();
 							if(((EditorItemNode)selected.Parent).Item instanceof StoredWorkflowState){
 	 							final StoredWorkflowState st = (StoredWorkflowState)((EditorItemNode)selected.Parent).Item;
 	 							if (selected.Type.equals("Условия")){
 									manager.add(new Action("Добавить условие"){
 										@Override
 										public void run() {
 											me.setDirty();
 											st.conditions.add(new StoredCondition(null, null, null));
 											me.refreshCollection("states");
 										}
 									});
 	 							}
 	 							if (selected.Type.equals("Разрешения для объекта")){
 									manager.add(new Action("Добавить разрешение для объекта"){
 										@Override
 										public void run() {
 											me.setDirty();
 											st.itemPermissions.add(new StoredPermissions(null, null));
 											me.refreshCollection("states");
 										}
 									});
 	 							}
 	 							if (selected.Type.equals("Разрешения для свойств")){
 									manager.add(new Action("Добавить разрешение для свойства"){
 										@Override
 										public void run() {
 											me.setDirty();
 											st.propertyPermissions.add(new StoredPropertyPermissions(null, new ArrayList<StoredPermissions>()));
 											me.refreshCollection("states");
 										}
 									});
 	 							}
 	 							if (selected.Type.equals("Выборка допустимых значений")){
 									manager.add(new Action("Добавить выборку допустимых значений"){
 										@Override
 										public void run() {
 											me.setDirty();
 											st.selectionProviders.add(new StoredWorkflowSelectionProvider());
 											me.refreshCollection("states");
 										}
 									});
 	 							}
 							}
 							
 						}	else if (selection.getFirstElement() instanceof EditorItemNode){
 							EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
							Object item = selected.Item;
							
							if(item instanceof StoredWorkflowState){
								final StoredWorkflowState sws = (StoredWorkflowState)item;
								
								manager.add(new Action("Добавить условие"){
									@Override
									public void run() {
										me.setDirty();
										sws.conditions.add(new StoredCondition(null, null, null));
										me.refreshCollection("states");
									}
								});
								
								manager.add(new Action("Добавить разрешение для объекта"){
									@Override
									public void run() {
										me.setDirty();
										sws.itemPermissions.add(new StoredPermissions(null, null));
										me.refreshCollection("states");
									}
								});
								
								manager.add(new Action("Добавить разрешение для свойства"){
									@Override
									public void run() {
										me.setDirty();
										sws.propertyPermissions.add(new StoredPropertyPermissions(null, new ArrayList<StoredPermissions>()));
										me.refreshCollection("states");
									}
								});
								
								manager.add(new Action("Добавить выборку допустимых значений"){
									@Override
									public void run() {
										me.setDirty();
										sws.selectionProviders.add(new StoredWorkflowSelectionProvider());
										me.refreshCollection("states");
									}
								});
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										((StoredWorkflowModel)model).states.remove(sws);
										me.refreshCollection("states");
									}
								});
								
							}
							if(item instanceof StoredPropertyPermissions){
								final StoredPropertyPermissions spp = (StoredPropertyPermissions)item;
								final StoredWorkflowState st = (StoredWorkflowState)((EditorItemNode)selected.Parent.Parent).Item;
								
								manager.add(new Action("Добавить разрешение"){
									@Override
									public void run() {
										me.setDirty();
										spp.permissions.add(new StoredPermissions(null, null));
										me.refreshCollection("states");
									}
								});
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										st.propertyPermissions.remove(spp);
										me.refreshCollection("states");
									}
								});
							}
							if(item instanceof StoredPermissions){
								final StoredPermissions spp = (StoredPermissions)item;
								final Object permissionParent = ((EditorItemNode)selected.Parent.Parent).Item;
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										if(permissionParent instanceof StoredWorkflowState){
											((StoredWorkflowState)permissionParent).itemPermissions.remove(spp);
										}else if(permissionParent instanceof StoredPropertyPermissions){
											((StoredPropertyPermissions)permissionParent).permissions.remove(spp);
										}
										me.refreshCollection("states");
									}
								});
							}
							if(item instanceof StoredCondition){
								final StoredCondition sc = (StoredCondition)item;
								final Object o = ((EditorItemNode)selected.Parent.Parent).Item;
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										if(o instanceof StoredWorkflowState){
											((StoredWorkflowState)o).conditions.remove(sc);
										}
										if(o instanceof StoredMatrixEntry){
											((StoredMatrixEntry)o).conditions.remove(sc);
										}
										me.refreshCollection("states");
									}
								});
							}
							if(item instanceof StoredWorkflowSelectionProvider){
								final StoredWorkflowSelectionProvider swsp = (StoredWorkflowSelectionProvider)item;
								final StoredWorkflowState sws = (StoredWorkflowState)((EditorItemNode)selected.Parent.Parent).Item;
								
								if(swsp.type.equals(StoredWorkflowSelectionProvider.TYPE_MATRIX)){
									manager.add(new Action("Добавить матрицу"){
										@Override
										public void run() {
											me.setDirty();
											swsp.matrix.add(new StoredMatrixEntry());
											me.refreshCollection("states");
										}
									});
								}
								
								if(swsp.type.equals(StoredWorkflowSelectionProvider.TYPE_SIMPLE)){
									manager.add(new Action("Добавить выборку"){
										@Override
										public void run() {
											me.setDirty();
											swsp.list.add(new StoredKeyValue());
											me.refreshCollection("states");
										}
									});
								}
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										sws.selectionProviders.remove(swsp);
										me.refreshCollection("states");
									}
								});
							}
							if(item instanceof StoredMatrixEntry){
								final StoredMatrixEntry sme = (StoredMatrixEntry)item;
								final StoredWorkflowSelectionProvider swsp = (StoredWorkflowSelectionProvider)((EditorItemNode)selected.Parent.Parent).Item;
								
								manager.add(new Action("Добавить условие"){
									@Override
									public void run() {
										me.setDirty();
										sme.conditions.add(new StoredCondition());
										me.refreshCollection("states");
									}
								});
								
								manager.add(new Action("Добавить результат"){
									@Override
									public void run() {
										me.setDirty();
										sme.result.add(new StoredKeyValue());
										me.refreshCollection("states");
									}
								});
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										swsp.matrix.remove(sme);
										me.refreshCollection("states");
									}
								});
							}
							if(item instanceof StoredKeyValue){
								final StoredKeyValue skv = (StoredKeyValue)item;
								final Object o = ((EditorItemNode)selected.Parent.Parent).Item;
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										if(o instanceof StoredMatrixEntry){
											((StoredMatrixEntry)o).result.remove(skv);
										} else if (o instanceof StoredWorkflowSelectionProvider){
											((StoredWorkflowSelectionProvider)o).list.remove(skv);
										}
										me.refreshCollection("states");
									}
								});
							}
 						}
 					}
 				}
 				
 				
 			}
 	     
 	  });
		
	  lists.put("transitions", new Object[]{
	     "Переходы",
	     new TreeMap<String, FormSettings>(){{
	    	 put("StoredWorkflowTransition", new FormSettings(
	    	     new LinkedHashMap<String,String>(){{
	    	    	 put("name","Системное имя");
	    	    	 put("caption","Логическое имя");
	    	    	 put("startState","Начальное состояние");
	    	    	 put("finishState","Завершающее состояние");
	    	    	 put("signBefore","Подписать до");
	    	    	 put("signAfter","Подписать после");
	    	    	 put("roles","Роли");
	    	     }},
	    	     new HashMap<String,String>(){{
	    	    	 put("signBefore","boolean");
	    	    	 put("signAfter","boolean");
	    	    	 put("roles","role_table");
	    	     }},
	    	     new HashMap<String, Object>(){{
	    	    	 put("startState",getStates());
	    	    	 put("finishState",getStates());
	    	    	 put("roles",getClassRoles(((StoredWorkflowModel)model).wfClass));
	    	     }}
	    	 ));
	    	 
 	    	 put("StoredKeyValue", new FormSettings(
	    	     new LinkedHashMap<String,String>(){{
	    	    	 put("key","Атрибут");
	    	    	 put("value","Значение");
	    	     }},
	    	     new HashMap<String,String>(){{}},
	    	     new HashMap<String, Object>(){{
	    	    	 put("key", getClassAttributes(((StoredWorkflowModel)model).wfClass));
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
						put("property", getClassAttributes(((StoredWorkflowModel)model).wfClass));
						put("operation", new LinkedHashMap<String, String>(){{
							put(String.valueOf(ConditionType.EQUAL.getValue()),"=");
							put(String.valueOf(ConditionType.LESS.getValue()),"<");
							put(String.valueOf(ConditionType.MORE.getValue()),">");
							put(String.valueOf(ConditionType.LESS_OR_EQUAL.getValue()),"<=");
							put(String.valueOf(ConditionType.MORE_OR_EQUAL.getValue()),">=");
							put(String.valueOf(ConditionType.NOT_EQUAL.getValue()),"<>");
							put(String.valueOf(ConditionType.EMPTY.getValue()),"пусто");
							put(String.valueOf(ConditionType.NOT_EMPTY.getValue()),"не пусто");
						}});							
					}}
				));
 	    	 
	     }},
	     new EditorTreeContentProvider() {
	    	 
				@Override
				protected boolean hasItemNodeChildren(Object element) {
					if(element instanceof StoredWorkflowTransition){
						return !((StoredWorkflowTransition)element).assignments.isEmpty() || 
								!((StoredWorkflowTransition)element).conditions.isEmpty();
					}
					return false;
				}
				
				@Override
				protected Object[] getItemNodeChildren(Object item) {
					if(item instanceof StoredWorkflowTransition){
						return new Object[]{"Присвоение","Условия"};
					}
					return null;
				}
				
				@Override
				protected Object[] getItemGroupChildren(Object item, String group) {
					if(group.equals("Присвоение")){
						return ((StoredWorkflowTransition)item).assignments.toArray();
					}
					if(group.equals("Условия")){
						return ((StoredWorkflowTransition)item).conditions.toArray();
					}
					return null;
				}
			},
			new EditorTreeLabelProvider() {
				
				@Override
				protected String getEditorItemNodeText(Object item) {
					if(item instanceof StoredWorkflowTransition){
						return ((StoredWorkflowTransition)item).caption;
					}
					if(item instanceof StoredKeyValue){
						return ((StoredKeyValue)item).key;
					}
					if(item instanceof StoredCondition){
						return ((StoredCondition)item).property;
					}
					return null;
				}
				
				@Override
				protected Image getEditorItemNodeImage(Object item) {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				protected Image getEditorItemGroupImage(String code) {
					// TODO Auto-generated method stub
					return null;
				}
			},
			new IMenuListener() {
				
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					manager.add(new Action("Новый переход"){
						@Override
						public void run() {
							try {
								//TODO
								IWizardDescriptor wd = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(NewWorkflowTransitionWizard.REGISTRY_ID);
								NewWorkflowTransitionWizard w = (NewWorkflowTransitionWizard)wd.createWizard();
								w.init(PlatformUI.getWorkbench(), me.collectionSelection("transitions"));
								WizardDialog dialog = new WizardDialog(w.getShell(), w);
								dialog.setTitle(w.getWindowTitle());
								w.setContext((StoredWorkflowModel)model);
								w.setCaller(me);
								dialog.open();									
							} catch (CoreException e) {
								e.printStackTrace();
							}	
						}
					});
					final IStructuredSelection selection = me.collectionSelection("transitions");
					if(selection != null){
						if(selection.getFirstElement() instanceof EditorItemGroup){
 							EditorItemGroup selected = (EditorItemGroup) selection.getFirstElement();
 							if(((EditorItemNode)selected.Parent).Item instanceof StoredWorkflowTransition){
 	 							final StoredWorkflowTransition swt = (StoredWorkflowTransition)((EditorItemNode)selected.Parent).Item;
 	 							
 	 							if (selected.Type.equals("Условия")){
 									manager.add(new Action("Добавить условие"){
 										@Override
 										public void run() {
 											me.setDirty();
 											swt.conditions.add(new StoredCondition());
 											me.refreshCollection("transitions");
 										}
 									});
 	 							}
 	 							
 	 							if (selected.Type.equals("Присвоение")){
 									manager.add(new Action("Добавить присвоение"){
 										@Override
 										public void run() {
 											me.setDirty();
 											swt.assignments.add(new StoredKeyValue());
 											me.refreshCollection("transitions");
 										}
 									});
 	 							}
 	 						}
							
						}	else if (selection.getFirstElement() instanceof EditorItemNode){
							EditorItemNode selected = (EditorItemNode) selection.getFirstElement();
							Object item = selected.Item;
							
							if(item instanceof StoredWorkflowTransition){
								final StoredWorkflowTransition swt = (StoredWorkflowTransition)item;
								
								manager.add(new Action("Добавить присвоение"){
									@Override
									public void run() {
										me.setDirty();
										swt.assignments.add(new StoredKeyValue());
										me.refreshCollection("transitions");
									}
								});
								
								manager.add(new Action("Добавить условие"){
									@Override
									public void run() {
										me.setDirty();
										swt.conditions.add(new StoredCondition());
										me.refreshCollection("transitions");
									}
								});
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										((StoredWorkflowModel)model).transitions.remove(swt);
										me.refreshCollection("transitions");
									}
								});
								
							}
							
							if(item instanceof StoredKeyValue){
								final StoredKeyValue skv = (StoredKeyValue)item;
								final StoredWorkflowTransition swt = (StoredWorkflowTransition)((EditorItemNode)selected.Parent.Parent).Item;
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										swt.assignments.remove(skv);
										me.refreshCollection("transitions");
									}
								});
							}
							
							if(item instanceof StoredCondition){
								final StoredCondition sc = (StoredCondition)item;
								final StoredWorkflowTransition swt = (StoredWorkflowTransition)((EditorItemNode)selected.Parent.Parent).Item;
								
								manager.add(new Action("Удалить"){
									@Override
									public void run() {
										me.setDirty();
										swt.conditions.remove(sc);
										me.refreshCollection("transitions");
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
