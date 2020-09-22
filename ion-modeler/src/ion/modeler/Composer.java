package ion.modeler;

import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredUserTypeMeta;
import ion.framework.meta.plain.StoredValidatorMeta;
import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.modeler.resources.IonEntityResource;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonSectionResource;
import ion.modeler.resources.IonUserTypeResource;
import ion.modeler.resources.IonValidatorResource;
import ion.modeler.resources.IonViewResource;
import ion.modeler.resources.IonWorkflowResource;
import ion.viewmodel.plain.StoredColumn;
import ion.viewmodel.plain.StoredFormViewModel;
import ion.viewmodel.plain.StoredListViewModel;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredNavSection;
import ion.viewmodel.plain.StoredTab;
import ion.viewmodel.view.Action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Composer {
	
	IProject _project;
	Gson gs;

	public Composer(IProject project) {
		_project = project;
		gs = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	}
	
	public IProject getProject(){
		return _project;
	}
	
	public void saveViewModel(Object o, String node, String className, String type) throws IOException, CoreException{
		IFolder f = _project.getFolder("views");
		String fn = f.getLocation().toString()+"/"+node.replace(".", "/")+"/"+className+"/"+type+".json";
		Writer w = new OutputStreamWriter(new FileOutputStream(new File(fn)),"UTF-8");
		gs.toJson(o, w);
		w.flush();
		w.close();		
		f.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	public void saveNavNode(Object o, String section) throws IOException, CoreException{
		IFolder f = _project.getFolder("navigation/"+section);
		ensureFolder(f);
		String fn = f.getLocation().toString()+"/"+((StoredNavNode) o).code+".json";
		Writer w = new OutputStreamWriter(new FileOutputStream(new File(fn)),"UTF-8");
		gs.toJson(o, w);
		w.flush();
		w.close();		
		f.refreshLocal(IResource.DEPTH_INFINITE, null);		
	}
	
	public void save(Object o) throws IOException, CoreException{
		String fn = null;
		IFolder f = null;
		if (o instanceof StoredClassMeta){
			f = _project.getFolder("meta");
			fn = f.getLocation().toString()+"/"+((StoredClassMeta) o).name+".class.json";
		}
		else if (o instanceof StoredPropertyMeta){
			f = _project.getFolder("attrtpl");
			fn = f.getLocation().toString()+"/"+((StoredPropertyMeta) o).name+".atpl.json";
		}
		else if (o instanceof StoredNavSection){
			f = _project.getFolder("navigation");
			fn = f.getLocation().toString()+"/"+((StoredNavSection) o).name+".section.json";
		}
		else if (o instanceof StoredUserTypeMeta) {
			f = _project.getFolder("meta/types");
			fn = f.getLocation().toString()+"/"+((StoredUserTypeMeta) o).name+".type.json";
		}
		else if (o instanceof StoredValidatorMeta) {
			f = _project.getFolder("validators");
			fn = f.getLocation().toString()+"/"+((StoredValidatorMeta) o).name+".valid.json";
		}
		else if (o instanceof StoredWorkflowModel) {
			f = _project.getFolder("workflows");
			fn = f.getLocation().toString()+"/"+((StoredWorkflowModel) o).name+".wf.json";
		}
		else
			assert false;
		
		if (!f.exists())
			f.create(true, true, null);
				
		Writer w = new OutputStreamWriter(new FileOutputStream(new File(fn)),"UTF-8");
		gs.toJson(o, w);
		w.flush();
		w.close();		
		f.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	private void ensureFolder(IFolder f) throws CoreException{
		if (!f.exists()){
			if (!f.getProjectRelativePath().isEmpty()){
				IFolder pf = f.getProject().getFolder(f.getProjectRelativePath().removeLastSegments(1));
				if (pf != null)
					ensureFolder(pf);
			}
			f.create(true, true, null);
		}
	}
	
	public void createView(String node, String className, String type, Integer overrideMode, Integer defaultActions) throws IOException, CoreException{
		
		IFolder f = _project.getFolder("views/"+node.replace(".", "/")+"/"+className);
		ensureFolder(f);
		String fn = f.getLocation().toString()+"/"+type+".json";
		Writer w = new OutputStreamWriter(new FileOutputStream(new File(fn)),"UTF-8");
		Object o = null;
		if (type.equals("list"))
			o = new StoredListViewModel(new ArrayList<StoredColumn>(),overrideMode);
		else
			o = new StoredFormViewModel(new ArrayList<StoredTab>(), Action.IntToActionTypeSet(defaultActions), overrideMode, 0);
		
		gs.toJson(o, w);
		w.flush();
		w.close();		
		f.refreshLocal(IResource.DEPTH_ONE, null);
	}
	
	public boolean deleteEntity(IonEntityResource entity) throws CoreException, IOException {
		Map<String, Object[]> children = ClassMetas(entity.getName(), false); 
		for (Object[] ch: children.values()){
			deleteEntity(new IonEntityResource((IFile)ch[0], (StoredClassMeta)ch[1]));
		}
		entity.Source().delete(true, null);
		return true;
	}
	
	public boolean deleteSection(IonSectionResource section) throws CoreException, IOException{
		section.Source().delete(true, null);
		return true;		
	}
	
	public boolean deleteNode(IonNodeResource node) throws CoreException, IOException{
		Map<String, Object[]> children = Nodes(node.getName()); 
		for (Object[] ch: children.values()){
			deleteNode(new IonNodeResource((IFile)ch[0], (StoredNavNode)ch[1], node.getSection()));
		}
		node.Source().delete(true, null);
		return true;		
	}
	
	public boolean deleteView(IonViewResource view) throws CoreException{
		view.Source().delete(true, null);
		return true;
	}
	
	public boolean deleteUserType(IonUserTypeResource type) throws CoreException {
		type.Source().delete(true, null);
		return true;
	}
	
	public boolean deleteValidator(IonValidatorResource validator) throws CoreException {
		validator.Source().delete(true, null);
		return true;
	}
	
	public boolean deleteWorkflow(IonWorkflowResource workflow) throws CoreException{
		workflow.Source().delete(true, null);
		return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object Read(String fn, Class c) throws IOException{
		Reader r = new InputStreamReader(new FileInputStream(new File(fn)),"UTF-8");
		Object result = gs.fromJson(r, c);
		r.close();
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object Read(IFile f, Class c) throws IOException, CoreException{
		Reader r = new InputStreamReader(f.getContents(true));
		Object result = gs.fromJson(r, c);
		r.close();
		return result;
	}	
	
	public StoredClassMeta getClass(String name) throws IOException{
		if (name != null && !name.isEmpty()){
			StoredClassMeta cm = (StoredClassMeta)Read(_project.getFolder("meta").getLocation().toString()+"/"+name+".class.json",StoredClassMeta.class);
			
			if (cm == null){
				try {
				for (IProject rp: _project.getReferencedProjects()){
					Composer c = new Composer(rp);
					cm = c.getClass(name);
					if (cm != null)
						break;
				}
				} catch (Exception e){
					e.printStackTrace(System.err);
				}
			}
			return cm;
		}
		return null;
	}
	
	public StoredPropertyMeta getAttrTemplate(String name) throws IOException{
		if (name != null && !name.isEmpty()){
			StoredPropertyMeta pm = (StoredPropertyMeta)Read(_project.getFolder("attrtpl").getLocation().toString()+"/"+name+".atpl.json",StoredPropertyMeta.class);
			if (pm == null){
				try {
					for (IProject rp: _project.getReferencedProjects()){
						Composer c = new Composer(rp);
						pm = c.getAttrTemplate(name);
						if (pm != null)
							break;
					}
				} catch (Exception e){
					e.printStackTrace(System.err);
				}
			}
			return pm;
		}
		return null;
	}	
	
	public Map<String, Object[]> ClassMetas(boolean global) throws CoreException, IOException{
    	return ClassMetas("", global);
	}
	
	public Map<String, Object[]> ClassMetas(String ancestor, boolean global) throws CoreException, IOException{
		Map<String, Object[]> result = new LinkedHashMap<String, Object[]>();
		if (global)
	    	for (IProject rp: _project.getReferencedProjects()){
	    		Composer c = new Composer(rp);
	    		result.putAll(c.ClassMetas(ancestor, false));
	    	}
				
    	IFolder mf = _project.getFolder("meta");
    	IResource[] content = mf.members();
    	for (IResource f: content){
    		if (f instanceof IFile){
    			if (f.getName().endsWith(".class.json")){
    				StoredClassMeta cm = (StoredClassMeta)Read(f.getLocation().toString(), StoredClassMeta.class);
    				if (ancestor.equals("") || (cm.ancestor != null && cm.ancestor.equals(ancestor)))
    					result.put(cm.name, new Object[]{f,cm});
    			}
    		}
    	}
    	    	
    	return result;
	}

	public Map<String, Object[]> AttrTemplates(boolean global) throws CoreException, IOException{
		Map<String, Object[]> result = new LinkedHashMap<String, Object[]>();

		if (global)
	    	for (IProject rp: _project.getReferencedProjects()){
	    		Composer c = new Composer(rp);
	    		result.putAll(c.AttrTemplates(false));
	    	}    	
				
		IFolder mf = _project.getFolder("attrtpl");
    	IResource[] content = mf.members();
    	for (IResource f: content){
    		if (f instanceof IFile){
    			if (f.getName().endsWith(".atpl.json")){
    				StoredPropertyMeta cm = (StoredPropertyMeta)Read(f.getLocation().toString(), StoredPropertyMeta.class);
    				result.put(cm.name, new Object[]{f,cm});
    			}
    		}
    	}
    	return result;
	}	
	
	public Map<String, Object[]> Nodes(String section) throws CoreException, IOException{
		return Nodes(section, "");
	}
		
	public Map<String, Object[]> Nodes(String section, String parent) throws CoreException, IOException{
		Map<String, Object[]> result = new LinkedHashMap<String, Object[]>();
    	IFolder mf = _project.getFolder("navigation/"+section);
    	if(mf.exists()){
      	IResource[] content = mf.members();
      	for (IResource f: content){
      		if (f instanceof IFile){
      			if (f.getName().endsWith(".json")){
      				StoredNavNode nn = (StoredNavNode)Read(f.getLocation().toString(),StoredNavNode.class);
      				if (parent.equals("") || (nn.code != null && nn.code.startsWith(parent) && !nn.code.equals(parent)))
      					result.put(nn.code, new Object[]{f,nn});
      			}
      		}
      	}
    	}
    	return result;		
	}
	
	public Collection<Object[]> Views() throws CoreException{
		return Views("");
	}
	
	private void fillViews(IFolder fl, Collection<Object[]> views, String node) throws CoreException{
		IResource[] content = fl.members();
		String curnode;
		
		for (IResource f: content){
    		if (f instanceof IFile){
    			if (f.getName().endsWith(".json")){
    				curnode = fl.getParent().getProjectRelativePath().makeRelativeTo(_project.getFolder("views").getProjectRelativePath()).toString();
    				curnode = curnode.replace("/", ".");
    				if (node.equals("") || curnode.equals(node))
    					views.add(new Object[]{f,curnode,fl.getName()});
    			}
    		} else if (f instanceof IFolder)
    			fillViews((IFolder)f, views, node);
    	}
	}
	
	public Collection<Object[]> Views(String node) throws CoreException{
		Collection<Object[]> result = new ArrayList<Object[]>();
    	fillViews(_project.getFolder("views"), result, node);
    	return result;		
	}	
	
	public Map<String, Object[]> UserTypes(boolean global) throws CoreException, IOException {
		Map<String, Object[]> result = new LinkedHashMap<String, Object[]>();
		
		if (global)
	    	for (IProject rp: _project.getReferencedProjects()){
	    		Composer c = new Composer(rp);
	    		result.putAll(c.UserTypes(false));
	    	}		
		
    	IFolder folder = _project.getFolder("meta/types");
    	if (folder.exists()) {
	    	IResource[] content = folder.members();
	    	for (IResource f: content) {
	    		if (f instanceof IFile) {
	    			if (f.getName().endsWith(".type.json")) {
						StoredUserTypeMeta container = (StoredUserTypeMeta) Read(
								f.getLocation().toString(),
								StoredUserTypeMeta.class);
	   					result.put(container.name, new Object[] { f, container });
	    			}
	    		}
	    	}
    	}
    	return result;
	}
	
	public Map<String, Object[]> Validators() throws CoreException, IOException {
		Map<String, Object[]> result = new LinkedHashMap<String, Object[]>();
    	IFolder folder = _project.getFolder("validators");
    	if (folder.exists()) {
	    	IResource[] content = folder.members();
	    	for (IResource f: content) {
	    		if (f instanceof IFile) {
	    			if (f.getName().endsWith(".valid.json")) {
						StoredValidatorMeta container = (StoredValidatorMeta) Read(
								f.getLocation().toString(),
								StoredValidatorMeta.class);
	   					result.put(container.name, new Object[] { f, container });
	    			}
	    		}
	    	}
    	}
    	return result;
	}
	
	public Map<String, Object[]> Workflows() throws CoreException, IOException{
		Map<String, Object[]> result = new LinkedHashMap<String, Object[]>();
		IFolder folder = _project.getFolder("workflows");
		if (folder.exists()) {
			IResource[] content = folder.members();
			for (IResource f: content) {
				if (f instanceof IFile) {
    			if (f.getName().endsWith(".wf.json")) {
					StoredWorkflowModel container = (StoredWorkflowModel) Read(
							f.getLocation().toString(),
							StoredWorkflowModel.class);
   					result.put(container.name, new Object[] { f, container });
    			}
    		}
			}
		}
		return result;
	}
	
	public Map<String, Object[]> Sections() throws CoreException, IOException {
		Map<String, Object[]> result = new LinkedHashMap<String, Object[]>();
    	IFolder folder = _project.getFolder("navigation");
    	if (folder.exists()) {
	    	IResource[] content = folder.members();
	    	for (IResource f: content) {
	    		if (f instanceof IFile) {
	    			if (f.getName().endsWith(".section.json")) {
	    				StoredNavSection container = (StoredNavSection) Read(
								f.getLocation().toString(),
								StoredNavSection.class);
	   					result.put(container.name, new Object[] { f, container });
	    			}
	    		}
	    	}
    	}
    	return result;
	}
	
}
