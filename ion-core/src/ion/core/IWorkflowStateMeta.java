package ion.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IWorkflowStateMeta {
	String getName();
	
	Collection<Condition> getConditions();
	
	Map<String, Set<DACPermission>> getItemPermissions();
	
	Map<String, Map<String, Set<DACPermission>>> getPropertyPermissions();
	
	Map<String, Map<String, ISelectionProvider>> getSelectionProviders();
	
	Set<IWorkflowTransitionMeta> getNextTransitions();
}
