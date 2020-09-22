package ion.core;

import java.util.Collection;

public interface IWorkflowMeta {
	String getName();
	
	String getApplicationClassName();
	
	IWorkflowStateMeta getStartState();
	
	Collection<IWorkflowStateMeta> getStates();
	
	IWorkflowStateMeta getState(String name);
	
	Collection<IWorkflowTransitionMeta> getTransitions();
	
	IWorkflowTransitionMeta getTransition(String name);
}
