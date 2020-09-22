package ion.modeler.view;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredUserTypeMeta;
import ion.framework.meta.plain.StoredValidatorMeta;
import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.modeler.Composer;
import ion.modeler.resources.IonEntityResource;
import ion.modeler.resources.IonFormViewResource;
import ion.modeler.resources.IonListViewResource;
import ion.modeler.resources.IonListViewValidators;
import ion.modeler.resources.IonModelResource;
import ion.modeler.resources.IonNodeResource;
import ion.modeler.resources.IonPropertyTemplateResource;
import ion.modeler.resources.IonSectionResource;
import ion.modeler.resources.IonValidatorResource;
import ion.modeler.resources.IonViewsResource;
import ion.modeler.resources.IonUserTypeResource;
import ion.modeler.resources.IonViewResource;
import ion.modeler.resources.IonWorkflowResource;
import ion.modeler.resources.ModelProjectAttrTemplates;
import ion.modeler.resources.ModelProjectMeta;
import ion.modeler.resources.ModelProjectUserTypes;
import ion.modeler.resources.ModelProjectViews;
import ion.modeler.resources.ModelProjectWorkflow;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredNavSection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

public class ContentProvider implements ITreeContentProvider, IResourceChangeListener {

    private static final Object[]   NO_CHILDREN = {};
    
    private Map<String,Object[]> cache;

    private Map<String, AttrTplCache> attrTemplates;    
    
    private Map<String, EntitiesCache> entities;

    private Map<String, NodesCache> nodes;

    private Map<String, SectionsCache> sections;

    private Map<String, ViewsCache> views; 

    /** имя проекта -> кэш типов */
    private Map<String, UserTypesCache> userTypes;
    
    private Map<String, ValidatorsCache> validators;
    
    private Map<String, WorkflowCache> workflows;
    
    private Viewer _viewer;
    
    private boolean needRefresh;
    
    public ContentProvider(){
    	ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    private void refreshAttrTemplates(IProject project){
		if (attrTemplates == null)
			attrTemplates = new HashMap<String, AttrTplCache>();
    	
		if (!attrTemplates.containsKey(project.getName()))
			attrTemplates.put(project.getName(), new AttrTplCache());
		
    	if (attrTemplates.get(project.getName()).Dirty){
    		Map<String, IonPropertyTemplateResource> ec = attrTemplates.get(project.getName()).templates;
    		try {
	    		Map<String, IonPropertyTemplateResource> result = new LinkedHashMap<String, IonPropertyTemplateResource>();
	    		Composer c = new Composer(project);
	    		Map<String, Object[]> metas = c.AttrTemplates(false);
	    		IonPropertyTemplateResource r;
	    		for (Map.Entry<String, Object[]> pair: metas.entrySet()){
	    			if (ec.containsKey(pair.getKey())){
	    				r = ec.get(pair.getKey());
	    				r.refresh((StoredPropertyMeta)pair.getValue()[1]);
	    			} else
	    				r = new IonPropertyTemplateResource((IFile)pair.getValue()[0], (StoredPropertyMeta)pair.getValue()[1]);
	    			result.put(pair.getKey(),r);
	    		}
	    		attrTemplates.get(project.getName()).templates = result;
	    		attrTemplates.get(project.getName()).Dirty = false;
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}    	
    }    
    
    private void refreshEntities(IProject project){
		if (entities == null)
			entities = new HashMap<String, EntitiesCache>();
    	
		if (!entities.containsKey(project.getName()))
			entities.put(project.getName(), new EntitiesCache());
		
    	if (entities.get(project.getName()).Dirty){
    		Map<String, IonEntityResource> ec = entities.get(project.getName()).entities;
    		try {
	    		Map<String, IonEntityResource> result = new LinkedHashMap<String, IonEntityResource>();
	    		Composer c = new Composer(project);
	    		Map<String, Object[]> metas = c.ClassMetas(false);
	    		IonEntityResource r;
	    		for (Map.Entry<String, Object[]> pair: metas.entrySet()){
	    			if (ec.containsKey(pair.getKey())){
	    				r = ec.get(pair.getKey());
	    				r.refresh((StoredClassMeta)pair.getValue()[1]);
	    			} else
	    				r = new IonEntityResource((IFile)pair.getValue()[0], (StoredClassMeta)pair.getValue()[1]);
	    			result.put(pair.getKey(),r);
	    		}
	    		entities.get(project.getName()).entities = result;
	    		entities.get(project.getName()).Dirty = false;
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}    	
    }
    
    private void refreshSections(IProject project){
  		if (sections == null)
  			sections = new HashMap<String, SectionsCache>();
      	
  		if (!sections.containsKey(project.getName()))
  			sections.put(project.getName(), new SectionsCache());
  		
      	if (sections.get(project.getName()).Dirty){
      		Map<String, IonSectionResource> ec = sections.get(project.getName()).sections;
      		try {
  	    		Map<String, IonSectionResource> result = new LinkedHashMap<String, IonSectionResource>();
  	    		Composer c = new Composer(project);
  	    		Map<String, Object[]> metas = c.Sections();
  	    		IonSectionResource r;
  	    		for (Map.Entry<String, Object[]> pair: metas.entrySet()){
  	    			if (ec.containsKey(pair.getKey())){
  	    				r = ec.get(pair.getKey());
  	    				r.refresh((StoredNavSection)pair.getValue()[1]);
  	    			} else
  	    				r = new IonSectionResource((IFile)pair.getValue()[0], (StoredNavSection)pair.getValue()[1]);
  	    			result.put(pair.getKey(),r);
  	    		}
  	    		sections.get(project.getName()).sections = result;
  	    		sections.get(project.getName()).Dirty = false;
      		} catch (Exception e){
      			e.printStackTrace();
      		}
      	}    	
      }
    
    private void refreshNodes(IProject project){
  		if (nodes == null)
  			nodes = new HashMap<String, NodesCache>();
      	
  		if (!nodes.containsKey(project.getName()))
  			nodes.put(project.getName(), new NodesCache());
		
    	if (nodes.get(project.getName()).Dirty){
    		Map<String, IonNodeResource> ec = nodes.get(project.getName()).nodes;
    		try {
	    		Map<String, IonNodeResource> result = new LinkedHashMap<String, IonNodeResource>();
	    		Composer c = new Composer(project);
	    		for(IonSectionResource section : sections.get(project.getName()).sections.values()){
	    			Map<String, Object[]> metas = c.Nodes(section.getName());
	    			IonNodeResource r;
		    		for (Map.Entry<String, Object[]> pair: metas.entrySet()){
		    			if (ec.containsKey(pair.getKey())){
		    				r = ec.get(pair.getKey());
		    				r.refresh((StoredNavNode)pair.getValue()[1], section);
		    			} else
		    				r = new IonNodeResource((IFile)pair.getValue()[0], (StoredNavNode)pair.getValue()[1], section);
		    			result.put(pair.getKey(),r);
		    		}
	    		}	    		
	    		nodes.get(project.getName()).nodes = result;
	    		nodes.get(project.getName()).Dirty = false;
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}    	
    }    
    
    private void refreshViews(IProject project){
		if (views == null)
			views = new HashMap<String, ViewsCache>();
    	
		if (!views.containsKey(project.getName()))
			views.put(project.getName(), new ViewsCache());
		
    	if (views.get(project.getName()).Dirty){
    		Map<String, IonEntityResource> es = entities.get(project.getName()).entities;    		
    		Map<String, IonNodeResource> ns = nodes.get(project.getName()).nodes;
    		Map<String, IonViewResource> ec = views.get(project.getName()).views;
    		try {
	    		Map<String, IonViewResource> result = new LinkedHashMap<String, IonViewResource>();
	    		Composer c = new Composer(project);
	    		// Object[0..2], где 0 - IFile, 1 - node name, 2 - folder name
	    		Collection<Object[]> metas = c.Views();
	    		IonViewResource r;
	    		String code;
	    		for (Object[] pair: metas){	    			
	    			IonViewsResource nv;
	    			if(!pair[1].equals("") && ns.containsKey(pair[1])){
	    				nv = ns.get(pair[1]).Views();
	    			} else {
	    				ModelProjectViews mpv = (ModelProjectViews)cache.get(project.getName())[1];
	    				nv = mpv.views;
	    			}
	    			code = pair[1]+(!pair[1].equals("")?".":"")+pair[2]+"."+((IFile)pair[0]).getName();
	    			if (ec.containsKey(code)){
	    				r = ec.get(code);
	    				r.refresh(nv, es.get(pair[2]));
	    				result.put(code,r);
	    			} else if ((pair[1].equals("") || ns.containsKey(pair[1])) && es.containsKey(pair[2])){	    				
	    				if (((IFile)pair[0]).getName().equals("list.json"))
	    					r = new IonListViewResource((IFile)pair[0], nv, es.get(pair[2]));
	    				else 
	    					r = new IonFormViewResource((IFile)pair[0], nv, es.get(pair[2]));
	    				result.put(code,r);
	    			}
	    		}
	    		views.get(project.getName()).views = result;
	    		views.get(project.getName()).Dirty = false;
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}    	
    }    
    
    private void refreshUserTypes(IProject project) {
    	if (userTypes == null)
    		userTypes = new HashMap<String, UserTypesCache>();
		String prname = project.getName();
		// добавляем проект в список, если его нет
    	if (!userTypes.containsKey(prname))
			userTypes.put(prname, new UserTypesCache());

    	UserTypesCache cache = userTypes.get(prname);
    	if (cache.Dirty) {
    		// получаем список типов из кэша
    		Map<String, IonUserTypeResource> curritems = userTypes.get(prname).items;
    		try {
	    		Map<String, IonUserTypeResource> newitems = new LinkedHashMap<String, IonUserTypeResource>();
    			// получаем список типов из фс
	    		Composer composer = new Composer(project);
	    		Map<String, Object[]> types = composer.UserTypes(false);
	    		IonUserTypeResource res;
	    		// обходим все типы из фс
	    		for (Map.Entry<String, Object[]> pair: types.entrySet()) {
	    			// если тип существует, обновляем его, иначе - создаем
	    			if (curritems.containsKey(pair.getKey())){
	    				res = curritems.get(pair.getKey());
	    				res.refresh((StoredUserTypeMeta)pair.getValue()[1]);
	    			} else
	    				res = new IonUserTypeResource((IFile)pair.getValue()[0], (StoredUserTypeMeta)pair.getValue()[1]);
	    			// помещаем тип в новый список
	    			newitems.put(pair.getKey(), res);
	    		}
	    		cache.items = newitems;
	    		cache.Dirty = false;
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
    private void refreshValidators(IProject project) {
    	if (validators == null)
    		validators = new HashMap<String, ValidatorsCache>();
		String prname = project.getName();
		// добавляем проект в список, если его нет
    	if (!validators.containsKey(prname))
    		validators.put(prname, new ValidatorsCache());

    	ValidatorsCache cache = validators.get(prname);
    	if (cache.Dirty) {
    		// получаем список типов из кэша
    		Map<String, IonValidatorResource> curritems = validators.get(prname).items;
    		try {
	    		Map<String, IonValidatorResource> newitems = new LinkedHashMap<String, IonValidatorResource>();
    			// получаем список типов из фс
	    		Composer composer = new Composer(project);
	    		Map<String, Object[]> valids = composer.Validators();
	    		IonValidatorResource res;
	    		// обходим все типы из фс
	    		for (Map.Entry<String, Object[]> pair: valids.entrySet()) {
	    			// если тип существует, обновляем его, иначе - создаем
	    			if (curritems.containsKey(pair.getKey())){
	    				res = curritems.get(pair.getKey());
	    				res.refresh((StoredValidatorMeta)pair.getValue()[1]);
	    			} else
	    				res = new IonValidatorResource((IFile)pair.getValue()[0], (StoredValidatorMeta)pair.getValue()[1]);
	    			// помещаем тип в новый список
	    			newitems.put(pair.getKey(), res);
	    		}
	    		cache.items = newitems;
	    		cache.Dirty = false;
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
    private void refreshWorkflows(IProject project) {
    	if (workflows == null)
    		workflows = new HashMap<String, WorkflowCache>();
		String prname = project.getName();
		// добавляем проект в список, если его нет
    	if (!workflows.containsKey(prname))
    		workflows.put(prname, new WorkflowCache());

    	WorkflowCache cache = workflows.get(prname);
    	if (cache.Dirty) {
    		// получаем список типов из кэша
    		Map<String, IonWorkflowResource> curritems = workflows.get(prname).workflows;
    		try {
	    		Map<String, IonWorkflowResource> newitems = new LinkedHashMap<String, IonWorkflowResource>();
    			// получаем список типов из фс
	    		Composer composer = new Composer(project);
	    		Map<String, Object[]> wfs = composer.Workflows();
	    		IonWorkflowResource res;
	    		// обходим все типы из фс
	    		for (Map.Entry<String, Object[]> pair: wfs.entrySet()) {
	    			// если тип существует, обновляем его, иначе - создаем
	    			if (curritems.containsKey(pair.getKey())){
	    				res = curritems.get(pair.getKey());
	    				res.refresh((StoredWorkflowModel)pair.getValue()[1]);
	    			} else
	    				res = new IonWorkflowResource((IFile)pair.getValue()[0], (StoredWorkflowModel)pair.getValue()[1]);
	    			// помещаем тип в новый список
	    			newitems.put(pair.getKey(), res);
	    		}
	    		cache.workflows = newitems;
	    		cache.Dirty = false;
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
    private Object[] entityChildren(IonEntityResource p){
    	Collection<IonEntityResource> result = new LinkedList<IonEntityResource>();
    	Map<String, IonEntityResource> ec = entities.get(p.Source().getProject().getName()).entities;
    	for (IonEntityResource r: ec.values()){
    		if (r.getAncestor() != null && r.getAncestor().equals(p.getName()))
    			result.add(r);
    	}
    	return result.toArray();    	
    }
    
    private Object[] sectionChildren(IonSectionResource p){
    	List<IonNodeResource> result = new LinkedList<IonNodeResource>();
    	Map<String, IonNodeResource> ec = nodes.get(p.Source().getProject().getName()).nodes;
    	for (IonNodeResource r: ec.values()){
    		if (r.getSection().equals(p) && r.getParentCode().isEmpty()){
    			result.add(r);
    		}
    	}
    	Collections.sort(result);
    	return result.toArray();
    }
    
    private Object[] nodeChildren(IonNodeResource p){
    	List<IonModelResource> result = new LinkedList<IonModelResource>();
    	List<IonNodeResource> subnodes = new LinkedList<IonNodeResource>();
    	IonViewsResource vs = p.Views();
    	if (nodeViewsHasChildren(vs))
    		result.add(vs);
    	Map<String, IonNodeResource> ec = nodes.get(p.Source().getProject().getName()).nodes;
    	for (IonNodeResource r: ec.values()){
    		if (r.getParentCode().equals(p.getName())){
    			subnodes.add(r);
    		}
    	}
    	Collections.sort(subnodes);
    	result.addAll(subnodes);
    	return result.toArray();    	
    }
    
    private Object[] nodeViewsChildren(IonViewsResource p){
    	Collection<Object> result = new LinkedList<Object>();
    	Collection<IonViewResource> ec = views.get(p.Source().getProject().getName()).views.values();
    	for (IonViewResource r: ec){
    		if (r.getParent() == p){
    			result.add(r);
    		}
    	}
    	return result.toArray();    	
    }     
    
    private boolean entityHasChildren(IonEntityResource p){
    	Map<String, IonEntityResource> ec = entities.get(p.Source().getProject().getName()).entities;
    	for (IonEntityResource r: ec.values()){
    		if (r.getAncestor() != null && r.getAncestor().equals(p.getName()))
    			return true;
    	}
    	return false;    	
    }
    
    private boolean sectionHasChildren(IonSectionResource p){
    	Map<String, IonNodeResource> ec = nodes.get(p.Source().getProject().getName()).nodes;
    	for (IonNodeResource r: ec.values()){
    		if (r.getSection().equals(p) && r.getParentCode().isEmpty())
    			return true;
    	}
    	return false;
    }
    
    private boolean nodeHasChildren(IonNodeResource p){
    	if (nodeViewsHasChildren(p.Views()))
    		return true;
    	Map<String, IonNodeResource> ec = nodes.get(p.Source().getProject().getName()).nodes;
    	for (IonNodeResource r: ec.values()){
    		if (r.getParentCode().equals(p.getName()))
    			return true;
    	}
    	return false;
    }
    
    private boolean nodeViewsHasChildren(IonViewsResource p){
    	Map<String, IonViewResource> ec = views.get(p.Source().getProject().getName()).views;
    	for (IonViewResource r: ec.values()){
    		if (r.getParent() == p)
    			return true;
    	}
    	return false;    	
    }
    
    private Object[] attrTplRoots(IonModelResource p){
    	Collection<IonModelResource> result = new LinkedList<IonModelResource>();
    	// получаем список всех сущностей
    	Map<String, IonPropertyTemplateResource> ec = attrTemplates.get(p.Source().getProject().getName()).templates;
    	for (IonPropertyTemplateResource r: ec.values()){
    		result.add(r);
    	}
    	return result.toArray();
    }    
        
    private Object[] metaRoots(IonModelResource p){
    	Collection<IonModelResource> result = new LinkedList<IonModelResource>();
    	// создаем узел дерева "пользовательские типы" и добавляем в список
    	IonModelResource rc = new ModelProjectUserTypes(p.Source().getProject());
    	result.add(rc);
    	// получаем список всех сущностей
    	Map<String, IonEntityResource> ec = entities.get(p.Source().getProject().getName()).entities;
    	for (IonEntityResource r: ec.values()){
    		if (r.getAncestor() == null || r.getAncestor().length() == 0)
    			result.add(r);
    	}
    	return result.toArray();
    }
    
    /* понадобится, если типы будут не в папке меты в дереве проекта
    private boolean metaRootsExist(IonModelResource p) {
    	Map<String, IonEntityResource> ec = entities.get(p.Source().getProject().getName()).entities;
    	for (IonEntityResource r: ec.values()){
    		if (r.getAncestor() == null || r.getAncestor().length() == 0)
    			return true;
    	}
    	return false;
    }
    */
    
    private Object[] viewsRoots(IonModelResource p){
    	Collection<Object> result = new LinkedList<Object>();
    	IonListViewValidators ivc = new IonListViewValidators(p.Source().getProject());
    	result.add(ivc);
//    	IonWorkflowResource wfs = new IonWorkflowResource(p.Source().getProject());
//    	result.add(wfs);
    	Map<String, IonSectionResource> isr = sections.get(p.Source().getProject().getName()).sections;
    	result.addAll(isr.values());
    	Map<String, IonViewResource> vc = views.get(p.Source().getProject().getName()).views;
    	for(IonViewResource v:vc.values()) {
    		IonViewsResource vs = v.getParent();
    		if(vs.Parent() instanceof ModelProjectViews)
    			result.add(vs);
    	}
    	return result.toArray();
    }
    
    private boolean viewsRootsExist(IonModelResource p){
    	//if(!sections.get(p.Source().getProject().getName()).sections.isEmpty())
    		return true;
    	//return false;
    }     
    
    private boolean userTypesExist(IonModelResource p) {
    	Map<String, IonUserTypeResource> types = userTypes.get(p.Source().getProject().getName()).items;
    	return (types.size() > 0);
    }
    
    private boolean validatorsExist(IonModelResource p) {
    	Map<String, IonValidatorResource> valids = validators.get(p.Source().getProject().getName()).items;
    	return (valids.size() > 0);
    }
    
    private boolean workflowsExist(IonModelResource p) {
    	Map<String, IonWorkflowResource> wfs = workflows.get(p.Source().getProject().getName()).workflows;
    	return (wfs.size() > 0);
    }
    
    private boolean attrTplExist(IonModelResource p) {
    	Map<String, IonPropertyTemplateResource> tpls = attrTemplates.get(p.Source().getProject().getName()).templates;
    	return (tpls.size() > 0);
    }
    	
    private Object[] userTypes(IonModelResource p){
    	Map<String, IonUserTypeResource> types = userTypes.get(p.Source().getProject().getName()).items;
    	return types.values().toArray();
    }
    
    private Object[] validators(IonModelResource p){
    	Map<String, IonValidatorResource> valids = validators.get(p.Source().getProject().getName()).items;
    	return valids.values().toArray();
    }
    
    private Object[] workflows(IonModelResource p){
    	Map<String, IonWorkflowResource> wfs = workflows.get(p.Source().getProject().getName()).workflows;
			return wfs.values().toArray();
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
     * Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
    	if (parentElement instanceof IonEntityResource)
    		return entityChildren((IonEntityResource)parentElement);
    	
    	if (parentElement instanceof IonViewResource)
    		return NO_CHILDREN;
    	
    	if (parentElement instanceof IonSectionResource)
    		return sectionChildren((IonSectionResource)parentElement);
    	
    	if (parentElement instanceof IonNodeResource)
    		return nodeChildren((IonNodeResource)parentElement);
    	
    	if (parentElement instanceof IonViewsResource)
    		return nodeViewsChildren((IonViewsResource)parentElement);
    	
        if (parentElement instanceof IProject){
        	if (cache == null)
        		cache = new HashMap<String, Object[]>();
        	if (!cache.containsKey(((IProject) parentElement).getName()))	
        		cache.put(((IProject) parentElement).getName(),new Object[]{
				   new ModelProjectMeta((IProject)parentElement),
				   new ModelProjectViews((IProject)parentElement),
				   new ModelProjectWorkflow((IProject)parentElement),
				   new ModelProjectAttrTemplates((IProject)parentElement)
        		});
        	return cache.get(((IProject) parentElement).getName());
        }

        // узел "модель"
        if (parentElement instanceof ModelProjectMeta){
        	refreshEntities(((IonModelResource)parentElement).Source().getProject());
            return metaRoots((ModelProjectMeta)parentElement);
        }
        // узел "пользовательские типы"
        if (parentElement instanceof ModelProjectUserTypes){
        	refreshUserTypes(((IonModelResource)parentElement).Source().getProject());
            return userTypes((IonModelResource)parentElement);
        }
        
        if (parentElement instanceof ModelProjectViews){
        	refreshEntities(((IonModelResource)parentElement).Source().getProject());
        	refreshSections(((IonModelResource)parentElement).Source().getProject());
        	refreshNodes(((IonModelResource)parentElement).Source().getProject());
        	refreshViews(((IonModelResource)parentElement).Source().getProject());
        	refreshWorkflows(((IonModelResource)parentElement).Source().getProject());
        	return viewsRoots((ModelProjectViews)parentElement);
        }
        
        if (parentElement instanceof IonListViewValidators){
        	refreshValidators(((IonModelResource)parentElement).Source().getProject());
            return validators((IonListViewValidators)parentElement);
        }
        
        if(parentElement instanceof ModelProjectWorkflow){
        	refreshWorkflows(((IonModelResource)parentElement).Source().getProject());
        	return workflows((ModelProjectWorkflow)parentElement);
        }
        
        // узел "шаблоны атрибутов"
        if (parentElement instanceof ModelProjectAttrTemplates){
        	refreshAttrTemplates(((IonModelResource)parentElement).Source().getProject());
            return attrTplRoots((ModelProjectAttrTemplates)parentElement);
        }        
        
        return NO_CHILDREN;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
     * )
     */
    @Override
    public Object getParent(Object element) {
        Object parent = null;	
        if (IonEntityResource.class.isInstance(element)) {
            if (((IonEntityResource)element).getAncestor() != null || ((IonEntityResource)element).getAncestor().length() > 0)
            	return entities.get(((IonEntityResource)element).Source().getProject().getName()).entities.get(((IonEntityResource)element).getAncestor());
            return cache.get(((IonModelResource)element).Source().getProject().getName())[0];
        }
        
		if (element instanceof IonSectionResource) {
			return cache.get(((IonModelResource) element).Source().getProject().getName())[1];
		}

		if (element instanceof IonNodeResource) {
			if (((IonNodeResource) element).getParentCode().length() > 0) {
				return nodes.get(((IonEntityResource) element).Source().getProject().getName()).nodes.get(((IonNodeResource) element).getParentCode());
			} else {
				return sections.get(((IonEntityResource) element).Source().getProject().getName()).sections.get(((IonNodeResource) element).getSection().getName());
			}
		}
		
		if (element instanceof IonPropertyTemplateResource)
			return cache.get(((IonModelResource)element).Source().getProject().getName())[3];
        
        if (element instanceof IonViewResource)
        	return ((IonViewResource) element).getParent();
                
        if (element instanceof IonViewsResource)
        	return ((IonViewsResource) element).Parent();
        /*
        if (element instanceof IonWorkflowResource)
        	return ((IonWorkflowResource) element).getParent();        
        */
        if (ModelProjectMeta.class.isInstance(element) || ModelProjectViews.class.isInstance(element)){       	
        	return ((IonModelResource)element).Source().getProject();
        }
        	
        return parent;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
     * Object)
     */
    @Override
    public boolean hasChildren(Object element) {
    	if (element instanceof IonEntityResource)
    		return entityHasChildren((IonEntityResource)element);
    	
    	if (element instanceof IonViewResource)
    		return false;
    	
    	if(element instanceof IonSectionResource)
    		return sectionHasChildren((IonSectionResource)element);
    	
    	if (element instanceof IonNodeResource)
    		return nodeHasChildren((IonNodeResource)element);
    	
    	if (element instanceof IonViewsResource)
    		return nodeViewsHasChildren((IonViewsResource)element);
    	
    	if (element instanceof ModelProjectAttrTemplates) {
    		refreshAttrTemplates(((IonModelResource)element).Source().getProject());
    		return attrTplExist((IonModelResource)element);
    	}
    	        
        if (element instanceof ModelProjectMeta) {
        	// обновление списка сущностей
        	// refreshEntities(((IonModelResource)element).Source().getProject());
            // return metaRootsExist((ModelProjectMeta)element);
        	// в папке "модель" всегда имеется папка "пользовательские типы"
        	return true;
        }
        
        if (element instanceof ModelProjectUserTypes) {
        	refreshUserTypes(((IonModelResource)element).Source().getProject());
        	return userTypesExist((IonModelResource)element);
        }
        
        if (element instanceof IonListViewValidators){
        	refreshValidators(((IonModelResource)element).Source().getProject());
            return validatorsExist((IonListViewValidators)element);
        }
        
        if (element instanceof ModelProjectWorkflow){
        	refreshWorkflows(((IonModelResource)element).Source().getProject());
        	return workflowsExist((ModelProjectWorkflow)element);
        }
        
        if (element instanceof ModelProjectViews){
        	IProject project = ((IonModelResource)element).Source().getProject();
        	refreshEntities(project);
        	refreshSections(project);
        	refreshNodes(project);
        	refreshViews(project);
        	return viewsRootsExist((ModelProjectViews)element);
        }
        
        if (element instanceof IProject)
        	return true;
                
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
     * .lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    	ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	_viewer = viewer;
    }

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
	    final TreeViewer viewer = (TreeViewer) _viewer;
	    
	    needRefresh = false;
	    
	    try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					final IResource res = delta.getResource();
					if (res instanceof IFile){
						IFile f = (IFile)res;
						final String prname = f.getProject().getName();
						if (f.getName().endsWith(".json")){
							try {
								if (f.getName().endsWith(".class.json"))
									entities.get(prname).Dirty = true;
								else if (f.getName().endsWith(".atpl.json"))
									attrTemplates.get(prname).Dirty = true;
								else if (f.getName().equals("list.json") || f.getName().equals("create.json") || f.getName().equals("item.json") || f.getName().equals("detail.json"))
									views.get(prname).Dirty = true;
								else if (f.getName().endsWith(".type.json"))
									userTypes.get(prname).Dirty = true;
								else if (f.getName().endsWith(".wf.json"))
									workflows.get(prname).Dirty = true;
								else if (f.getName().endsWith(".valid.json"))
									validators.get(prname).Dirty = true;
								else if (f.getName().endsWith(".section.json"))
									sections.get(prname).Dirty = true;								
								else if (f.getFullPath().toPortableString().contains("/navigation/"))
									nodes.get(prname).Dirty = true;
								else
									assert(false);
							} catch (Exception e) {
								
							}
							needRefresh = true;
				    		return false;
						}
					}
						
					return true;
				}
			});
			
			if (needRefresh){
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						TreePath[] treePaths = viewer.getExpandedTreePaths();
						viewer.refresh();
						viewer.setExpandedTreePaths(treePaths);
					}
				});
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	    
	    
	}	

}
