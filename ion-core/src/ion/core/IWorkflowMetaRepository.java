package ion.core;

import java.util.Collection;

public interface IWorkflowMetaRepository {
	IWorkflowMeta getWorkflow(String className, String name);
	
	Collection<IWorkflowMeta> getClassWorkflows(String className);
	
	IWorkflowMeta getGlobalWorkflow(String name);
	
	Collection<IWorkflowMeta> getGlobalWorkflows();
	
}
