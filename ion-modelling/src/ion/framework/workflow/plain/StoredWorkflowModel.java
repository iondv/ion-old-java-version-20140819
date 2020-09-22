package ion.framework.workflow.plain;

import java.util.ArrayList;
import java.util.List;


public class StoredWorkflowModel {
	public String name;
	public String caption;
	public String wfClass;
	public String startState;
	public List<StoredWorkflowState> states;
	public List<StoredWorkflowTransition> transitions;
	
	public StoredWorkflowModel(){
		this(null, null, null, null, new ArrayList<StoredWorkflowState>(), new ArrayList<StoredWorkflowTransition>());
	}
	
	public StoredWorkflowModel(String name, String caption, String wfClass,
	                           String startState,
                             List<StoredWorkflowState> states,
                             List<StoredWorkflowTransition> transitions) {
	  this.name = name;
	  this.caption = caption;
	  this.wfClass = wfClass;
	  this.startState = startState;
	  this.states = states;
	  this.transitions = transitions;
  }
	
}
