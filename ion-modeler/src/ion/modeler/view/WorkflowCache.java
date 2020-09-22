package ion.modeler.view;

import ion.modeler.resources.IonWorkflowResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowCache {
	
	public boolean Dirty;
	
	public Map<String, IonWorkflowResource> workflows;
	
	public WorkflowCache(){
		Dirty = true;
		workflows = new LinkedHashMap<String, IonWorkflowResource>();
	}

}
