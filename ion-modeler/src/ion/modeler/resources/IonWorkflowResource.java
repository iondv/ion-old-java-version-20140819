package ion.modeler.resources;

import ion.framework.workflow.plain.StoredWorkflowModel;

import org.eclipse.core.resources.IFile;

public class IonWorkflowResource extends IonFileBasedResource {
	//ModelProjectWorkflow parent;

	public IonWorkflowResource(IFile src/*, ModelProjectWorkflow parent*/, StoredWorkflowModel meta) {
	  super(src);
	  //this.parent = parent;
	  refresh(meta);
	}
	/*
	public ModelProjectWorkflow getParent(){
		return parent;
	}
	*/
	public void refresh(StoredWorkflowModel meta){
		name = meta.name;
		displayName = meta.caption;
	}

}
