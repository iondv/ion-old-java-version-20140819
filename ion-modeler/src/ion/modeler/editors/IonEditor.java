package ion.modeler.editors;

import ion.core.ConditionType;
import ion.core.MetaPropertyType;
import ion.core.OperationType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredUserTypeMeta;
import ion.framework.meta.plain.StoredValidatorMeta;
import ion.modeler.Composer;
import ion.modeler.forms.FormSettings;
import ion.modeler.forms.ModelItemForm;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
//import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.MultiPageEditorPart;

public abstract class IonEditor extends MultiPageEditorPart implements IResourceChangeListener, Listener {
	
	protected Object model = null;
	 
	protected IEditorInput input = null;
	
	protected boolean dirty = true;
	
	protected Map<String, FormSettings> formSettings;
	
	protected Map<String, Object[]> lists;
	
	protected Map<String, ModelItemForm> details;
	
	protected Map<String, TreeViewer> masters;
	
	ModelItemForm frm;
	
	Composite mainPage;
	
	boolean needRefresh;
	
	boolean needReload;
	
	boolean supressDirty = false;
	
	protected Composer composer;
	
	private boolean canRefresh = true;
	
	public Composer getComposer(){
		if (composer == null)
			composer = new Composer(((IFileEditorInput)input).getFile().getProject());
		return composer;
	}
	
	protected void masterSetup(String name, TreeViewer viewer){}
	
	public Map<String, String> getClassSelection(boolean pure, boolean structs){
		Map<String, String> classSelection = new LinkedHashMap<String,String>();
		Map<String, Object[]> classes;
		try {
			classes = getComposer().ClassMetas(true);
			for (Object[] v: classes.values()){
				if ((((StoredClassMeta)v[1]).is_struct && structs) || (!((StoredClassMeta)v[1]).is_struct && pure))
					classSelection.put(((StoredClassMeta)v[1]).name, ((StoredClassMeta)v[1]).caption);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classSelection;
	}
	
	public Map<String, String> getUserTypeList() {
		Map<String, String> result = new LinkedHashMap<String,String>();
		try {
			Map<String, Object[]> types = getComposer().UserTypes(true);
			for (Object[] v: types.values()){
				result.put(((StoredUserTypeMeta)v[1]).name, ((StoredUserTypeMeta)v[1]).caption);
			}
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public Map<String, String> getValidatorsList() {
		Map<String, String> result = new LinkedHashMap<String,String>();
		try {
			Map<String, Object[]> types = getComposer().Validators();
			for (Object[] v: types.values()){
				result.put(((StoredValidatorMeta)v[1]).name, ((StoredValidatorMeta)v[1]).caption);
			}
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public Map<String, String> getClassSelection(){
		return getClassSelection(true, true);
	}
		
	public IonEditor() {
		super();
		formSettings = new HashMap<String, FormSettings>();
		lists = new LinkedHashMap<String,Object[]>();
		masters = new HashMap<String, TreeViewer>();
		details = new HashMap<String, ModelItemForm>();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	protected void formLists(){}	
	
	protected void formProperties(){}
	
	protected Map<String,Object> getSelections(String classname){
		Map<String,Object> selections = new HashMap<String, Object>();
		return selections;
	}
	
	protected void refreshSelections(String classname){
		formSettings.get(classname).selections.clear();
		formSettings.get(classname).selections.putAll(getSelections(classname));
	}	
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}	

	protected abstract Object loadModel(IFile f) throws IOException;
	
	protected abstract String mainPageText();
	
	protected abstract String formPartName();

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		this.input = input;
		if (input instanceof IFileEditorInput){
			try {
				model = loadModel(((IFileEditorInput) input).getFile());
				formProperties();
				formLists();
				dirty = false;
				supressDirty = false;
				frm = new ModelItemForm(formSettings);
				frm.setModel(model);
				setPartName(formPartName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, input);
	}
	
	protected void refresh(Display d){
		d.syncExec(new Runnable() {
			@Override
			public void run() {
				refreshSelections(model.getClass().getSimpleName());
				frm.RefreshSelectionLists();
				
				for (ModelItemForm f: details.values())
					f.RefreshSelectionLists();				
			}
		});
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	public Object getModel(){
		return model;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	private Display getDisplay(){
	 /*   Display d = Display.getCurrent();
	    if (d == null)
	    	d = Display.getDefault();*/
	    return PlatformUI.getWorkbench().getDisplay();
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
	    Display d = getDisplay();
	    if (event.getType() == IResourceChangeEvent.POST_CHANGE){
		    needRefresh = false;
		    needReload = false;
		    
		    try {
				event.getDelta().accept(new IResourceDeltaVisitor() {
					@Override
					public boolean visit(IResourceDelta delta) throws CoreException {
						if (delta.getResource() instanceof IFile){
							IFile f = (IFile)delta.getResource();
							if (f.getName().endsWith(".json")){
								if (!isDirty() && f.equals(((IFileEditorInput)input).getFile()))
									needReload = true;
								
								//if (f.getName().endsWith(".class.json") || f.getName().endsWith(".wf.json"))
									needRefresh = true;
					    		return false;
							}
						}
						return true;
					}
				});
				
				if (needReload){
					try {
						model = loadModel(((IFileEditorInput)input).getFile());
						d.syncExec(new Runnable() {
							
							@Override
							public void run() {
								frm.setModel(model);
								for (Map.Entry<String, TreeViewer> vp: masters.entrySet()){
									try {
										vp.getValue().setInput(model.getClass().getDeclaredField(vp.getKey()).get(model));
									} catch (IllegalArgumentException e) {
										e.printStackTrace();
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									} catch (NoSuchFieldException e) {
										e.printStackTrace();
									} catch (SecurityException e) {
										e.printStackTrace();
									}
								}								
							}
						});
					} catch (IOException e){
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}	
					refresh(d);
				} else if (needRefresh)
					refresh(d);
			} catch (CoreException e) {
				e.printStackTrace();
			}			
		}
		
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			final IResource res = event.getResource();
			d.asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((IFileEditorInput)input).getFile().getProject().equals(res)){
							IEditorPart editorPart = pages[i].findEditor(input);
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}
	
	private void createMainPage(){
		if (frm != null){
			mainPage = frm.Build(getContainer());
			frm.SetChangeListener(this);
			addPage(0,mainPage);
			setPageText(0, mainPageText());		
		}
	}
	
	protected String condToString(StoredCondition c){
		String r = "";
		if (c.property != null && !c.property.isEmpty()){
			r = r + c.property+" ";
			if (c.operation != null){
				ConditionType t = ConditionType.fromInt(c.operation);
				switch (t){
					case EQUAL:r = r + "=";break;
					case LESS:r = r + "<";break;
					case MORE:r = r + ">";break;
					case LESS_OR_EQUAL:r = r + "<=";break;
					case MORE_OR_EQUAL:r = r + ">=";break;
					case NOT_EQUAL:r = r + "<>";break;
					case EMPTY:r = r + "пусто";break;
					case NOT_EMPTY:r = r + "не пусто";break;
					case CONTAINS:r = r + "содержит";break;
					case IN:r = r+ "входит в";break;
					case LIKE:r = r+ "похож на";break;
					default:break;			
				}
			}
		} else if (c.operation != null){
			OperationType t = OperationType.fromInt(c.operation);
			switch (t){
				case AND:r = r+"и";break;
				case NOT:r = r+"не";break;
				case OR:r = r+"или";break;
				case MIN:r = r+"мин из";break;
				case MAX:r = r+"макс из";break;
			}
		}
		if (c.value != null && !c.value.isEmpty())
			r = r + " " + c.value;		
		return r;
	}	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void createListPages(){
		FormToolkit toolkit;
		Form frm;
		Composite dtls;
		toolkit = new FormToolkit(getContainer().getDisplay());
		int ind = 0;
		if (mainPage != null)
			ind = 1;
		for (Map.Entry<String, Object[]> pair: lists.entrySet()){
			frm = toolkit.createForm(getContainer());
			GridLayout layout = new GridLayout();
			layout.horizontalSpacing = 5;
			layout.verticalSpacing = 5;
			layout.numColumns = 2;
			frm.getBody().setLayout(layout);
			
			GridData gd = new GridData(GridData.FILL_BOTH);
			Tree t = toolkit.createTree(frm.getBody(), SWT.BORDER | SWT.FLAT | SWT.CAP_SQUARE);
			t.setLayoutData(gd);
			
			String caption = (String)pair.getValue()[0];
			
			final ModelItemForm ifrm = new ModelItemForm((Map)pair.getValue()[1]);
			details.put(pair.getKey(), ifrm);
			dtls = ifrm.Build(frm.getBody());
			dtls.setLayoutData(gd);
			ifrm.SetChangeListener(this);
			
			TreeViewer viewer = new TreeViewer(t);

			if (pair.getValue().length > 4 && (pair.getValue()[4] instanceof IMenuListener)){
				MenuManager menuManager = new MenuManager("#popup");
				menuManager.add(new Action("wtf?") {});//XXX:WTF?!
				menuManager.setRemoveAllWhenShown(true);
				menuManager.addMenuListener((IMenuListener)pair.getValue()[4]);
				menuManager.createContextMenu(getContainer());			
				viewer.getControl().setMenu(menuManager.getMenu());
			}
			
			if (pair.getValue().length > 5 && (pair.getValue()[5] instanceof IDragDropProvider)){
				int operations = DND.DROP_COPY| DND.DROP_MOVE;
				Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
				viewer.addDragSupport(operations, transferTypes , new IonDragListener(viewer, (IDragDropProvider)pair.getValue()[5]));
				viewer.addDropSupport(operations, transferTypes, new IonDropListener(this, viewer, ifrm, (IDragDropProvider)pair.getValue()[5]));
			}
			
			masters.put(pair.getKey(), viewer);
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					Object mdl = ((IStructuredSelection)event.getSelection()).getFirstElement();
					if (((IStructuredSelection)event.getSelection()).getFirstElement() instanceof EditorItemNode)
						mdl = ((EditorItemNode)((IStructuredSelection)event.getSelection()).getFirstElement()).Item;
					ifrm.setModel(mdl);					
				}
			});
			viewer.setContentProvider((IContentProvider) pair.getValue()[2]);
			viewer.setLabelProvider((IBaseLabelProvider)pair.getValue()[3]);

		    try {
		    	Object lst = model.getClass().getField(pair.getKey()).get(model);
		    	masterSetup(pair.getKey(), viewer);
		    	viewer.setInput(lst);
		    	
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			addPage(ind,frm);
			setPageText(ind, caption);
			ind++;
		}
	}
	
	
	public IStructuredSelection collectionSelection(String property){
		if (masters.containsKey(property))
			return (IStructuredSelection)masters.get(property).getSelection();
		return null;
	}
	
	public void setCollectionInput(String property, Object input){
		if (masters.containsKey(property)){
			masters.get(property).setInput(input);
			refresh(getDisplay());
		}
	}
	
	public void refreshCollection(String property){
		if (masters.containsKey(property)){
			TreeViewer viewer = masters.get(property);
			if (viewer.getInput() == null){
				try {
		    	Object lst = model.getClass().getField(property).get(model);
		    	viewer.setInput(lst);
				} catch (Exception e) {
					
				}
			}
			TreePath[] treePaths = viewer.getExpandedTreePaths();
			viewer.refresh();
			viewer.setExpandedTreePaths(treePaths);		
		}
	}

	@Override
	protected void createPages() {
		createMainPage();
		createListPages();
	}
	
	protected void performSaving(Composer c) throws IOException, CoreException{
		c.save(model);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (dirty){
			supressDirty = true;
			IFile f = ((IFileEditorInput)input).getFile();
			Composer c = new Composer(f.getProject());
			try {
				performSaving(c);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			dirty = false;
			firePropertyChange(PROP_DIRTY);			
			supressDirty = false;
		}
	}

	@Override
	public void doSaveAs() {
		return;
	}
	
	public void setDirty(){
		dirty = true;
		firePropertyChange(PROP_DIRTY);		
	}

	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.Modify && !supressDirty){
			if (event.widget instanceof Text){
				if (canRefresh){
					canRefresh = false;
					final Display d = getDisplay();
					d.timerExec(1500, new Runnable() {
			    		public void run() {
			    			for (TreeViewer v: masters.values()){
			    				v.refresh();
			    			}
			    			refresh(d);
			    			canRefresh = true;
			    		}
			    	});
				}
			} else {
    			for (TreeViewer v: masters.values()){
    				v.refresh();
    			}
    			refresh(getDisplay());				
			}
			setDirty();
		}
	}
	
	protected Map<String, String> getClassProperties(Composer composer, StoredClassMeta classmeta, MetaPropertyType typefilter, Integer depth, int level) {
		Map<String,String> result = new LinkedHashMap<String, String>();
		StoredClassMeta meta = classmeta;
		
		if (depth == null) {
			IScopeContext projectScope = new ProjectScope(composer.getProject());
			IEclipsePreferences projectNode = projectScope.getNode("ion.modeler");
			depth = projectNode.getInt("referenceDrillDepth",0);
		}
		
		while (meta != null) {
			for (StoredPropertyMeta pm: meta.properties)
			if (typefilter == null || (pm.type == typefilter.getValue())){
				result.put(pm.name,pm.caption);
				if (pm.type == MetaPropertyType.STRUCT.getValue() ||
						pm.type == MetaPropertyType.REFERENCE.getValue() ||
						pm.type == MetaPropertyType.COLLECTION.getValue()){

					StoredClassMeta struct = null;
					try {
						struct = composer.getClass((pm.type == MetaPropertyType.COLLECTION.getValue())?pm.items_class:pm.ref_class);
					} catch (IOException e) {
						e.printStackTrace();
					}

					if (struct != null){
						switch (MetaPropertyType.fromInt(pm.type)) {
						case STRUCT:{
							while(struct != null){
								for (StoredPropertyMeta spm: struct.properties)
									result.put(pm.name + "$" + spm.name, pm.caption + "." + spm.caption);
								try {
									struct = composer.getClass(struct.ancestor);
								} catch (IOException e) {
									struct = null;
									e.printStackTrace();
								}	 
							}						
						}break;
						case COLLECTION:
						case REFERENCE:{
							if (level < depth) {
								Map<String, String> ref_props = getClassProperties(composer, struct, typefilter, depth, level + 1);
								for (Map.Entry<String, String> kv: ref_props.entrySet()){
									result.put(pm.name+"."+kv.getKey(), pm.caption+"."+kv.getValue());
								}
							}
						}break;
						default:break;
						}
					}
					
				}
			}
			try {
				meta = composer.getClass(meta.ancestor);
			} catch (IOException e) {
				meta = null;
				e.printStackTrace();
				break;
			}
		}

		return result;
	}
	
	public void setPageDetailModel(String pageName, Object model){
		this.details.get(pageName).setModel(model);
	}
}
