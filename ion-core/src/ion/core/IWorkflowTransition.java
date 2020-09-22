package ion.core;

import java.util.Map;

public interface IWorkflowTransition {
	String getActionName();
	String getCaption();
	Map<String, Object> getAssignments();
	boolean getSignBefore();
	boolean getSignAfter();
}
