package ion.modeler.forms;

import ion.framework.meta.plain.StoredKeyValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class ModelItemForm implements SelectionListener, ModifyListener, ICheckStateListener {
	
	Object model;
	
	Map<String, FormSettings> settings;
	
	Map<String, Control> controls;
	
	Map<String, Viewer> viewers;
	
	Map<String, Composite> containers;
	
	Listener changeListener;
	
	ScrolledForm main;
	
	//FormToolkit toolkit;
	
	boolean supressBinding = false;
	
	public ModelItemForm(Map<String,FormSettings> settings) {
		this.settings = settings;
		controls = new HashMap<String, Control>();
		containers = new HashMap<String, Composite>();
		viewers = new HashMap<String,Viewer>();
	}
	
	private String fieldKey(String field){
		return field;
	}
	
	private void checkComboValue(Combo combo, Object v){
		int selected = -1;
		String code;
		for (int i = 0; i < combo.getItems().length; i++){
			code = combo.getData(String.valueOf(i)).toString();
			if (code.equals(v != null?v.toString():"")){
				selected = i;
				break;
			}
		}
		if (selected >= 0)
			combo.select(selected);							
	}
	
	@SuppressWarnings("unchecked")
	private void checkSetValue(Table t, Object v){
		String fn = t.getData("field").toString();
		String cn = this.model.getClass().getSimpleName();
		Viewer ctv = viewers.get(cn+"."+fn);
		Map<String,String> selection;
		Object s = settings.get(cn).selections.get(fn);
		if (s instanceof IKeyValueProvider)
			selection = ((IKeyValueProvider) s).Provide(this.model);
		else if (s instanceof Map)
			selection = (Map<String, String>)s;
		else
			selection = new HashMap<String, String>();
		if(ctv instanceof CheckboxTableViewer){
			if(v instanceof Collection){
				((CheckboxTableViewer)ctv).setCheckedElements(parseSetSelectionList(selection, (Collection<String>) v));
			}else{
				((CheckboxTableViewer)ctv).setCheckedElements(parseSetSelection(selection, (Integer)v));
			}
		}else if(ctv instanceof TableViewer){
			if(v instanceof Set){
				((TableViewer)ctv).setInput((wrapRoles((Set<String>)v)).toArray());
			}
			if(v instanceof List){
				((TableViewer)ctv).setInput(((List<StoredKeyValue>)v).toArray());
			}
		}
	}
	
	private Object[] parseSetSelectionList(Map<String, String> selection,
			Collection<String> v) {
		if (v == null)
			return new Object[]{};
		Collection<String> selected = new ArrayList<String>();
		for (Map.Entry<String,String> pair: selection.entrySet()){
			if (v.contains(pair.getKey()))
				selected.add(pair.getValue());
		}		
		return selected.toArray();
	}

	private void checkControlValue(Object v, Control editor){
		if (editor instanceof Table)
			checkSetValue((Table)editor, v);
		else if (editor instanceof Combo)
			checkComboValue((Combo)editor, v);
		else if (editor instanceof Button)
			((Button)editor).setSelection((v != null)?(Boolean)v:false);					
		else 
			((Text)editor).setText(v != null?v.toString():"");
	}
	
	private static Field getField(Class<?>  type, String fieldName)
		        throws NoSuchFieldException {
	    try {
	      return type.getDeclaredField(fieldName);
	    } catch (NoSuchFieldException e) {
	    	Class<?> superClass = type.getSuperclass();
	      if (superClass == null) {
	        throw e;
	      } else {
	        return getField(superClass, fieldName);
	      }
	    }
	}
	  
	private Object getModelValue(String fld){
		if (model != null)
		try {
			FormSettings fs = settings.get(model.getClass().getSimpleName());
			String typeName = fs.types.containsKey(fld)?fs.types.get(fld):"String";
			if (typeName.equalsIgnoreCase("commaSeparated")){
				Field field = getField(model.getClass(), fld);
				String[] v = (String[]) field.get(model);
				String result = "";
				int i = 0;
				if (v != null)
					for (String s: v){
						result = result + (i > 0?", ":"") + s;
						i++;
					}
				return result;
			} else {
				Field field = getField(model.getClass(), fld);
				return field.get(model);
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void checkControlValue(String fld, Control editor){
		if (model != null)
			checkControlValue(getModelValue(fld),editor);
	}
	
	public void setModel(Object model){
		supressBinding = true;
		this.model = null;
		
		if (model != null){
			if (settings.containsKey(model.getClass().getSimpleName()))
				this.model = model;
		}
		
		for (Map.Entry<String, Composite> pair: containers.entrySet()){
			if (this.model == null || !pair.getKey().equals(this.model.getClass().getSimpleName()))
				pair.getValue().setVisible(false);
		}
		
		if (this.model != null){
			if (containers.containsKey(this.model.getClass().getSimpleName()))
				((Composite)containers.get(this.model.getClass().getSimpleName())).setVisible(true);
			
			RefreshSelectionLists();
			
			Class<?> mt = model.getClass();
			for (String f: getAllFields(mt)) {
				if (controls.containsKey(mt.getSimpleName()+"."+f)){
					checkControlValue(getModelValue(f), controls.get(mt.getSimpleName()+"."+f));				
				}
			}
		}
		supressBinding = false;
	}
	
	private static List<String> getAllFields(Class<?> type) {
		List<String> fields = new LinkedList<String>();
		getAllFields(fields, type);
		return fields;
	}
	
	private static void getAllFields(List<String> fields, Class<?> type) {
		for (Field f: type.getDeclaredFields())
			fields.add(f.getName());
	    if (type.getSuperclass() != null)
	        getAllFields(fields, type.getSuperclass());
	}
	
	private void checkComboSelection(Combo combo, Map<String, String> selection, Object v){
		int i = 0;
		int selected = -1;
		for (Map.Entry<String,String> pair: selection.entrySet()){
			combo.setData(Integer.toString(i), pair.getKey());
			combo.add(pair.getValue(), i);
			if (pair.getKey().equals(v != null?v.toString():"")){
				selected = i;
			}			
			i++;
		}						
		
		if (selected >= 0)
			combo.select(selected);			
	}
	
	@SuppressWarnings("unchecked")
	private Object[] parseSetSelection(Map<String, String> selection, Object v){
		if (v == null)
			return new Object[]{};
		Collection<String> selected = new ArrayList<String>();
		for (Map.Entry<String,String> pair: selection.entrySet()){
			if (v instanceof Integer){
				if ((Integer.parseInt(pair.getKey()) & (Integer)v) > 0)
					selected.add(pair.getValue());
			} if (v instanceof Collection){
				if (((Collection<String>) v).contains(pair.getKey()))
					selected.add(pair.getValue());
			}
		}		
		return selected.toArray();
	}
	
	private void checkSetSelection(CheckboxTableViewer viewer, Map<String, String> selection, Object v){
		viewer.add(selection.values().toArray());	
		viewer.setCheckedElements(parseSetSelection(selection, v));
	}	
	
	@SuppressWarnings("unchecked")
	private Map<String,String> getSelectionList(String classname, String property){
		if (settings.containsKey(classname)){
			FormSettings fs = settings.get(classname);
			if (fs.selections.containsKey(fieldKey(property))){
				Object sl = fs.selections.get(fieldKey(property));
				if (sl instanceof IKeyValueProvider && (model != null))
					return ((IKeyValueProvider) sl).Provide(model);
				else if (sl instanceof Map)
					return (Map<String,String>)sl;
			}
		}
		return new HashMap<String,String>();
	}
	
	private String[] parsePropertyName(String pn){
		if (pn.contains("."))
			return pn.split("\\.");
		return new String[]{"",pn};
	}
	
	public void RefreshSelectionLists(){
		boolean allowBinding = !supressBinding;
		supressBinding = true;
		if (model != null){
			Combo combo;
			String[] pn;
			for (Map.Entry<String, Control> pair: controls.entrySet()){
				if (pair.getValue() instanceof Combo){
					pn = parsePropertyName(pair.getKey());
					if (model.getClass().getSimpleName().equals(pn[0])){
						combo = (Combo)pair.getValue();
						combo.removeAll();				
						checkComboSelection(combo, getSelectionList(pn[0],pn[1]), getModelValue(pn[1]));
					}
				} else if (pair.getValue() instanceof Table) {
					pn = parsePropertyName(pair.getKey());
					if (model.getClass().getSimpleName().equals(pn[0])){
						CheckboxTableViewer ctv = (CheckboxTableViewer)viewers.get(pair.getKey());
						((Table)pair.getValue()).removeAll();
						checkSetSelection(ctv,getSelectionList(pn[0],pn[1]), getModelValue(pn[1]));
					}
				}
			}
		}
		if (allowBinding)
			supressBinding = false;
	}
	
	public void SetChangeListener(Listener listener){
		changeListener = listener;
	}
	
	class RoleWrapper {
		String role;
		
		public RoleWrapper(String role) {
	    this.role = role;
    }
	}
/*	
	private Set<String> unwrapRoles(Set<RoleWrapper> wrappedRoles){
		Set<String> result = new HashSet<String>();
		for(RoleWrapper r : wrappedRoles){
			result.add(r.role);
		}
		return result;
	}
*/	
	private Set<RoleWrapper> wrapRoles(Set<String> roles){
		Set<RoleWrapper> result = new HashSet<RoleWrapper>();
		for(String s : roles){
			result.add(new RoleWrapper(s));
		}
		return result;
	}
	
	class ComboBoxEditingSupport extends EditingSupport {

		String classname;
		String property;
		String[] selArray;
		ComboBoxCellEditor editor;

		public ComboBoxEditingSupport(ColumnViewer viewer, String classname, String property) {
	    super(viewer);
	    this.classname = classname;
	    this.property = property;
	    
	    Map<String,String> selectionMap = getSelectionList(classname,property);
	    selArray = new String[selectionMap.size()];
	    selArray = selectionMap.keySet().toArray(selArray);
	    editor = new ComboBoxCellEditor((Composite) getViewer().getControl(), selArray, SWT.READ_ONLY);
    }

		@Override
    protected boolean canEdit(Object arg0) {
			if (changeListener != null){
				Event ne = new Event();
				ne.type = SWT.Modify;
				ne.widget = controls.get(classname+"."+property);
				changeListener.handleEvent(ne);
			}
			return true;
    }

		@Override
    protected CellEditor getCellEditor(Object arg0) {
			return editor;
    }

		@Override
    protected Object getValue(Object element) {
			if(element instanceof RoleWrapper){
				if(element != null){
					return getArrayIndex(selArray,((RoleWrapper)element).role);
				}
			} else if(element instanceof StoredKeyValue){
				if(((StoredKeyValue) element).key != null){
				  return getArrayIndex(selArray,((StoredKeyValue) element).key);
				}
			}
			return 0;
    }

	@SuppressWarnings("unchecked")
	@Override
    protected void setValue(Object element, Object value) {
			if(element instanceof RoleWrapper){
				String oldValue = ((RoleWrapper) element).role;
				((RoleWrapper) element).role = selArray[(int) value];
				((Set<String>)getModelValue(property)).remove(oldValue);
				((Set<String>)getModelValue(property)).add(selArray[(int) value]);
			}else if(element instanceof StoredKeyValue){
				((StoredKeyValue) element).key = selArray[(int) value];
			}
			getViewer().update(element, null);
    }
		
	}
	
	private int getArrayIndex(String[] array, String element){
		for(int i=0;i<array.length;i++){
			if(array[i].equals(element)){
				return i;
			}
		}
		return 0;
	}
	
	class TableContentProvider implements ITableLabelProvider{
		
		//ITableLabelProvider
		@Override
    public void addListener(ILabelProviderListener arg0) {
	    // TODO Auto-generated method stub
	    
    }

		@Override
    public boolean isLabelProperty(Object arg0, String arg1) {
	    // TODO Auto-generated method stub
	    return false;
    }

		@Override
    public void removeListener(ILabelProviderListener arg0) {
	    // TODO Auto-generated method stub
	    
    }

		@Override
    public Image getColumnImage(Object arg0, int arg1) {
	    // TODO Auto-generated method stub
	    return null;
    }

		@Override
    public String getColumnText(Object element, int index) {
			if(element instanceof StoredKeyValue){
				StoredKeyValue skv = (StoredKeyValue)element;
				switch (index) {
					case 0: return skv.key;
					case 1: return skv.value;
					default: return "";
				}
			} else if (element instanceof RoleWrapper){
				return ((RoleWrapper) element).role;
			}
			return "";
    }

		@Override
    public void dispose() {
	    // TODO Auto-generated method stub
	    
    }
	}
	
	public Composite Build(Composite parent){
		supressBinding = true;
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		main = toolkit.createScrolledForm(parent);
		main.getBody().setLayout(new FormLayout());
				
		Form form;
		GridLayout layout;
		String classname;
		
		FormSettings fs;
		
		Label label;
		Control editor;
		
		String typeName;
		GridData leftcol;
		GridData rightcol;
		
		String fk;
		
		Collection<Control> tablist = new ArrayList<Control>();
		
		for (Map.Entry<String, FormSettings> fsp: settings.entrySet()){
			classname = fsp.getKey();
			fs = fsp.getValue();
			form = toolkit.createForm(main.getBody());
			containers.put(classname, form);
			layout = new GridLayout();
			layout.horizontalSpacing = 5;
			layout.verticalSpacing = 5;
			layout.numColumns = 2;
			form.getBody().setLayout(layout);
	
			try {		
				tablist.clear();
				for (Map.Entry<String, String> f1: fs.captions.entrySet()){
					final Map.Entry<String, String> f = f1;
					
					leftcol = new GridData(GridData.FILL,GridData.FILL,false,false);
					rightcol = new GridData(GridData.FILL,GridData.FILL,true,false);
					rightcol.grabExcessHorizontalSpace = true;
					rightcol.widthHint = 300;
						
					fk = fieldKey(f.getKey());
					label = toolkit.createLabel(form.getBody(), f.getValue());
					//new Label(form.getBody(), SWT.NONE);
					//label.setText(captions.containsKey(f.getName())?captions.get(f.getName()):f.getName());		
					label.setLayoutData(leftcol);
					label.pack();
					if (fs.selections.containsKey(fk)){
						typeName = fs.types.containsKey(fk)?fs.types.get(fk):"String"; 
						if (typeName.equalsIgnoreCase("set")){
							editor = new Table(form.getBody(), SWT.CHECK | SWT.BORDER | SWT.FLAT);
							CheckboxTableViewer ctv = new CheckboxTableViewer((Table)editor);
							ctv.setData("field", f.getKey());
							viewers.put(classname+"."+f.getKey(), ctv);
							ctv.addCheckStateListener(this);
							rightcol.heightHint = 100;
							checkSetSelection(ctv,getSelectionList(classname, f.getKey()), getModelValue(f.getKey()));
						} else if (typeName.equalsIgnoreCase("role_table")){
							editor = new Table(form.getBody(), SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
							((Table)editor).setHeaderVisible(true);
							((Table)editor).setLinesVisible(true);
							final TableViewer ctv = new TableViewer((Table)editor);
							ctv.setData("field", f.getKey());
							viewers.put(classname+"."+f.getKey(), ctv);
							ctv.setUseHashlookup(true);
							
							TableViewerColumn viewerColumn1 = new TableViewerColumn(ctv, SWT.LEFT);
							viewerColumn1.getColumn().setText("Роль");
							viewerColumn1.getColumn().setWidth(150);
							viewerColumn1.setEditingSupport(new ComboBoxEditingSupport(ctv, classname, f.getKey()));
							
							ctv.setContentProvider(new ArrayContentProvider());
							ctv.setLabelProvider(new TableContentProvider());
							final List<Object> currentSelection = new ArrayList<Object>();
							ctv.addSelectionChangedListener(new ISelectionChangedListener() {
								@SuppressWarnings("unchecked")
								@Override
								public void selectionChanged(SelectionChangedEvent event) {
									StructuredSelection selection = (StructuredSelection)event.getSelection();
									currentSelection.clear();
									currentSelection.addAll(selection.toList());
								}
							});
							
							// Label dummyLabel = new Label(form.getBody(), SWT.NONE); 
							
							Group buttonGroup = new Group(form.getBody(), SWT.CENTER);
							buttonGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
							
							Button add = new Button(buttonGroup, SWT.PUSH | SWT.CENTER);
							add.setText("Добавить");
							
							add.addSelectionListener(new SelectionAdapter() {
								@SuppressWarnings("unchecked")
								public void widgetSelected(SelectionEvent e) {
									((Set<String>)getModelValue(f.getKey())).add(new String());
									((TableViewer)ctv).setInput((wrapRoles((Set<String>)getModelValue(f.getKey()))).toArray());
								}
							});
							
							Button delete = new Button(buttonGroup, SWT.PUSH | SWT.CENTER);
							delete.setText("Удалить"); 
							delete.addSelectionListener(new SelectionAdapter() {
								@SuppressWarnings("unchecked")
								@Override
								public void widgetSelected(SelectionEvent e) {
									if(!currentSelection.isEmpty()){
										for(Object o :currentSelection){
											((Set<String>)getModelValue(f.getKey())).remove(o);
										}
										((TableViewer)ctv).setInput(((Set<String>)getModelValue(f.getKey())).toArray());
									}
								}
							});
							rightcol.heightHint = 100;
						} else if (typeName.equalsIgnoreCase("table")){ 
							editor = new Table(form.getBody(), SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
							((Table)editor).setHeaderVisible(true);
							((Table)editor).setLinesVisible(true);
							final TableViewer ctv = new TableViewer((Table)editor);
							ctv.setData("field", f.getKey());
							viewers.put(classname+"."+f.getKey(), ctv);
							ctv.setUseHashlookup(true);
							
							TableViewerColumn viewerColumn1 = new TableViewerColumn(ctv, SWT.LEFT);
							viewerColumn1.getColumn().setText("Атрибут");
							viewerColumn1.getColumn().setWidth(150);
							viewerColumn1.setEditingSupport(new ComboBoxEditingSupport(ctv, classname, f.getKey()));
							
							TableViewerColumn viewerColumn2 = new TableViewerColumn(ctv, SWT.LEFT);
							viewerColumn2.getColumn().setText("Значение");
							viewerColumn2.getColumn().setWidth(150);
							viewerColumn2.setEditingSupport(new EditingSupport(ctv) {
								
								@Override
								protected void setValue(Object element, Object value) {
									((StoredKeyValue) element).value = value.toString();
									getViewer().update(element, null);
								}
								
								@Override
								protected Object getValue(Object element) {
									StoredKeyValue skv = (StoredKeyValue)element;
									if(skv.value != null){
										return skv.value;
									}
									return "";
								}
								
								@Override
								protected CellEditor getCellEditor(Object arg0) {
									TextCellEditor editor = new TextCellEditor((Composite) getViewer().getControl());
									((Text) editor.getControl()).setTextLimit(60);
									return editor;
								}
								
								@Override
								protected boolean canEdit(Object arg0) {
									if (changeListener != null){
										Event ne = new Event();
										ne.type = SWT.Modify;
										ne.widget = controls.get(model.getClass().getSimpleName()+"."+f.getKey());
										changeListener.handleEvent(ne);
									}
									return true;
								}
							});
							
							ctv.setContentProvider(new ArrayContentProvider());
							ctv.setLabelProvider(new TableContentProvider());
							final List<Object> currentSelection = new ArrayList<Object>();
							ctv.addSelectionChangedListener(new ISelectionChangedListener() {
								@SuppressWarnings("unchecked")
								@Override
								public void selectionChanged(SelectionChangedEvent event) {
									StructuredSelection selection = (StructuredSelection)event.getSelection();
									currentSelection.clear();
									currentSelection.addAll(selection.toList());
								}
							});
							
							//Label dummyLabel = new Label(form.getBody(), SWT.NONE);
							
							Group buttonGroup = new Group(form.getBody(), SWT.CENTER);
							buttonGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
							
							Button add = new Button(buttonGroup, SWT.PUSH | SWT.CENTER);
							add.setText("Добавить");

							add.addSelectionListener(new SelectionAdapter() {
								@SuppressWarnings("unchecked")
								public void widgetSelected(SelectionEvent e) {
									((List<StoredKeyValue>)getModelValue(f.getKey())).add(new StoredKeyValue());
									((TableViewer)ctv).setInput(((List<StoredKeyValue>)getModelValue(f.getKey())).toArray());
								}
							});
							
							Button delete = new Button(buttonGroup, SWT.PUSH | SWT.CENTER);
							delete.setText("Удалить");
							delete.addSelectionListener(new SelectionAdapter() {
								@SuppressWarnings("unchecked")
								@Override
								public void widgetSelected(SelectionEvent e) {
									if(!currentSelection.isEmpty()){
										for(Object o :currentSelection){
											((List<StoredKeyValue>)getModelValue(f.getKey())).remove(o);
										}
										((TableViewer)ctv).setInput(((List<StoredKeyValue>)getModelValue(f.getKey())).toArray());
									}
								}
							});
							
							rightcol.heightHint = 100;
						} else {
							editor = new Combo(form.getBody(), SWT.FLAT | SWT.BORDER | SWT.CAP_SQUARE | SWT.SINGLE | SWT.DROP_DOWN);
							((Combo)editor).addModifyListener(this);
							toolkit.adapt(editor, true, true);
							checkComboSelection((Combo)editor, getSelectionList(classname, f.getKey()), getModelValue(f.getKey()));
						}
					} else {
						typeName = fs.types.containsKey(fk)?fs.types.get(fk):"String"; 
						if (typeName.equalsIgnoreCase("Boolean")){
							editor = toolkit.createButton(form.getBody(),"",SWT.CHECK | SWT.FLAT | SWT.CAP_SQUARE);
							((Button)editor).addSelectionListener(this);
						} else if (typeName.equalsIgnoreCase("multiLine") || typeName.equalsIgnoreCase("commaSeparated")){ 
							editor = toolkit.createText(form.getBody(),"", SWT.FLAT | SWT.BORDER | SWT.CAP_SQUARE | SWT.MULTI);
							rightcol.heightHint = 100;
							((Text)editor).addModifyListener(this);						
						} else {
							editor = toolkit.createText(form.getBody(),"", SWT.FLAT | SWT.BORDER | SWT.CAP_SQUARE | SWT.SINGLE);
							((Text)editor).addModifyListener(this);
						}
						checkControlValue(f.getKey(), editor);
					}		
					editor.setData("field", f.getKey());
					editor.setLayoutData(rightcol);
					editor.pack();
					tablist.add(editor);
					controls.put(classname+"."+f.getKey(), editor);				
				}
				
				form.getBody().setTabList((Control[]) tablist.toArray(new Control[]{}));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			
			 if (this.model != null)
				form.setVisible(this.model.getClass().getSimpleName().equals(classname));
			 else
				 form.setVisible(false);
		}
		supressBinding = false;
		//main.setVisible(this.model != null);
		return main;
	}
	
	private Object newFieldValue(Field f, Object v){
		if (v == null)
			return null;
		
		String tn = f.getType().getSimpleName();
		if (tn.equalsIgnoreCase("boolean"))
			return (Boolean)v;
		if (tn.equalsIgnoreCase("String[]"))
			return v.toString().trim().split("\\s*,\\s*");
		
		if (
			tn.equalsIgnoreCase("int")
			|| 
			tn.equalsIgnoreCase("Integer")		
		)
			return (v.toString().trim().length() > 0)?Integer.parseInt(v.toString()):null;
			
		if (tn.equalsIgnoreCase("short"))
			return (v.toString().trim().length() > 0)?Short.parseShort(v.toString()):null;

					
		if (tn.equalsIgnoreCase("long"))
			return (v.toString().trim().length() > 0)?Long.parseLong(v.toString()):null;
					
		if (tn.equalsIgnoreCase("byte"))
			return (v.toString().trim().length() > 0)?Byte.parseByte(v.toString()):null;
		
		return v.toString();
	}

	/*
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object mdl = ((IStructuredSelection)event.getSelection()).getFirstElement();
		if (((IStructuredSelection)event.getSelection()).getFirstElement() instanceof EditorItemNode)
			mdl = ((EditorItemNode)((IStructuredSelection)event.getSelection()).getFirstElement()).Item;
		setModel(mdl);
	}
	*/
	
	
	@SuppressWarnings("rawtypes")
	private boolean checkNumber(Class c){
		if (c.equals(Integer.class) 
			|| c.equals(Integer.TYPE)
			|| c.equals(Short.class)
			|| c.equals(Short.TYPE)
			|| c.equals(Long.class)
			|| c.equals(Long.TYPE)
			|| c.equals(Double.class)
			|| c.equals(Double.TYPE)
			|| c.equals(Float.class)
			|| c.equals(Float.TYPE)
			)
			return true;
		return false;
	}
	
	private boolean setModelField(String fld, Object v){
		try {
			Field f = getField(model.getClass(), fld);
			Object nv = new Object();
			if(v instanceof Collection){
				nv = v;
			} else {
				nv = newFieldValue(f, v);
			}
			Object ov = getModelValue(fld);
			if ((nv == null || nv == "") && f.getType().isPrimitive()){
				if (checkNumber(f.getType()))
					nv = 0;
				else if (f.getType().isAssignableFrom(String.class))
					nv = "";
			}
			f.set(model, nv);
			if (nv == null && ov != null || nv != null && ov == null)
				return true;
			if (nv != null && ov != null && !ov.equals(nv))
				return true;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private Integer buildSetValue(String fld){
		String cn = model.getClass().getSimpleName();
		CheckboxTableViewer ctv = (CheckboxTableViewer)viewers.get(cn+"."+fld);
		Map<String,String> selection = getSelectionList(cn, fld); 
		Integer result = 0;
		for (Map.Entry<String,String> pair: selection.entrySet()){
			if (ctv.getChecked(pair.getValue()))
				result = result | Integer.parseInt(pair.getKey());
		}
		return result;
	}
	
	private Collection<String> buildSetValueAsList(String fld) {
		String cn = model.getClass().getSimpleName();
		CheckboxTableViewer ctv = (CheckboxTableViewer)viewers.get(cn+"."+fld);
		Map<String,String> selection = getSelectionList(cn, fld); 
		Collection<String> result = new ArrayList<String>();
		for (Map.Entry<String,String> pair: selection.entrySet()){
			if (ctv.getChecked(pair.getValue()))
				result.add(pair.getKey());
		}
		return result;
	}	
	
	private void handleChanges(TypedEvent event){
		if (!supressBinding){
			String fld = (String)event.widget.getData("field");
			if (fld != null && model != null){
				boolean changed = false;
				if (event.widget instanceof Text){
					changed = setModelField(fld, ((Text)event.widget).getText());
				} else if (event.widget instanceof Button){
					changed = setModelField(fld, ((Button)event.widget).getSelection());
				} else if (event.widget instanceof Combo){
					Combo editor = (Combo)event.widget;
					if (editor.getSelectionIndex() == -1 && editor.getText() != null && !editor.getText().isEmpty())
						changed = setModelField(fld, editor.getText());
					else
						changed = setModelField(fld, event.widget.getData(String.valueOf(editor.getSelectionIndex())));
				} 
				if (changed){
					//RefreshSelectionLists();
					if (changeListener != null){
						Event ne = new Event();
						ne.type = SWT.Modify;
						ne.widget = event.widget;
						changeListener.handleEvent(ne);
					}
				}
			}
		}		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		handleChanges(e);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void modifyText(ModifyEvent e) {
		handleChanges(e);		
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		if (!supressBinding){
			String fld = ((Viewer)event.getCheckable()).getData("field").toString();
			Object value = new Object();
			try {
				Field field = getField(model.getClass(), fld);
				if(field.getType().equals(Collection.class)){
					value = buildSetValueAsList(fld);
				}else{
					value = buildSetValue(fld);
				}
				
				if (setModelField(fld, value)){
					RefreshSelectionLists();
					if (changeListener != null){
						Event ne = new Event();
						ne.type = SWT.Modify;
						ne.widget = controls.get(model.getClass().getSimpleName()+"."+fld);
						changeListener.handleEvent(ne);
					}
				}
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
