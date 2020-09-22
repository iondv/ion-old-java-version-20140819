package ion.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IWorkflowTransitionMeta {
	String getName();
	
	String getCaption();
	
	IWorkflowStateMeta getSource();
	
	IWorkflowStateMeta getDestination();
	
	Collection<Condition> getConditions();
	
	Map<String, Object> getPropertyAssignments();
	
	Set<String> getPermittedRoles();
	
	boolean getSignBefore();
	
	boolean getSignAfter();
}
