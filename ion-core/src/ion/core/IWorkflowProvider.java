package ion.core;

public interface IWorkflowProvider {
	boolean HasWorkflows(IStructMeta classMeta) throws IonException;
	
	IWorkflowState ProcessTransition(IItem item, String workflowName, String transitionName) throws IonException;
	
	IWorkflowState GetState(IItem item) throws IonException;
}