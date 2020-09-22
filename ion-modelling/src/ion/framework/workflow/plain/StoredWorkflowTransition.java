package ion.framework.workflow.plain;

import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoredWorkflowTransition {

	public String name;
	public String caption;
	public String startState;
	public String finishState;
	public boolean signBefore;
	public boolean signAfter;
	public Set<String> roles;
	public List<StoredKeyValue> assignments;
	public List<StoredCondition> conditions;
	
	public StoredWorkflowTransition(String name, String caption,
	                                String startState,
                                  String finishState,
                                  boolean signBefore,
                                  boolean signAfter,
                                  Set<String> roles,
                                  List<StoredKeyValue> assignments,
                                  List<StoredCondition> conditions) {
	  this.name = name;
	  this.caption = caption;
	  this.startState = startState;
	  this.finishState = finishState;
	  this.signBefore = signBefore;
	  this.signAfter = signAfter;
	  this.roles = roles;
	  this.assignments = assignments;
	  this.conditions = conditions;
  }
	
	public StoredWorkflowTransition(){
		this(null,null, null, null, false, false, new HashSet<String>(), new ArrayList<StoredKeyValue>(), new ArrayList<StoredCondition>());
	}
}
