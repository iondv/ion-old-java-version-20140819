package ion.framework.meta.workflow;

import ion.core.workflow.meta.WorkflowMeta;
import ion.framework.workflow.plain.StoredWorkflowModel;
import ion.framework.workflow.plain.StoredWorkflowState;
import ion.framework.workflow.plain.StoredWorkflowTransition;

public class IonWorkflowMeta extends WorkflowMeta {
	public IonWorkflowMeta(StoredWorkflowModel model){
		super(model.name, model.wfClass);
		
		for (StoredWorkflowState s: model.states){
			states.put(s.name, new IonWorkflowStateMeta(s));
		}
		
		if(model.startState != null)
			start = states.get(model.startState);
		
		for (StoredWorkflowTransition t: model.transitions){
			transitions.put(t.name, new IonWorkflowTransitionMeta(t, (IonWorkflowStateMeta)states.get(t.startState), (IonWorkflowStateMeta)states.get(t.finishState)));
		}
	}
}
