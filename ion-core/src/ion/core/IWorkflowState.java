package ion.core;

public interface IWorkflowState {
	IWorkflowTransition[] getNextTransitions(IUserContext user);
	
	boolean checkItemPermission(IUserContext user, DACPermission permission) throws IonException;
	
	boolean checkPropertyPermission(String property, IUserContext user, DACPermission permission) throws IonException;
	
	ISelectionProvider getSelectionProvider(String property, IUserContext user) throws IonException;
}
