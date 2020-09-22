package ion.core.workflow;

import java.util.Map;

import ion.core.IWorkflowTransition;

public class WorkflowTransition implements IWorkflowTransition {
	private String name;
	private String caption;
	
	private boolean signBefore;
	
	private boolean signAfter;
	
	private Map<String, Object>	 assignments;
	
	public WorkflowTransition(String name, String caption, boolean signBefore, boolean signAfter, Map<String, Object> assignments){
		this.name = name;
		this.caption = caption;
		this.signBefore = signBefore;
		this.signAfter = signAfter;
		this.assignments = assignments;
	}
	
	@Override
	public String getActionName(){
		return name;
	}
	
	@Override
	public Map<String, Object> getAssignments(){
		return assignments;
	}

	@Override
	public boolean getSignBefore() {
		return signBefore;
	}
	
	@Override
	public boolean getSignAfter() {
		return signAfter;
	}
	
	@Override
  public String getCaption() {
	  return caption;
  }
}
