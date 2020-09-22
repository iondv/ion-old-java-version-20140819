package ion.core.workflow.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ion.core.Condition;
import ion.core.IWorkflowStateMeta;
import ion.core.IWorkflowTransitionMeta;

public class WorkflowTransitionMeta implements IWorkflowTransitionMeta {
	protected String name;
	
	protected String caption;
	
	protected IWorkflowStateMeta source;
	
	protected IWorkflowStateMeta destination;
	
	protected Collection<Condition> conditions;
	
	protected Map<String, Object> assignments;
	
	protected Set<String> roles;
	
	protected boolean signBefore;
	
	protected boolean signAfter;
	
	public WorkflowTransitionMeta(String name, String caption, 
	                              IWorkflowStateMeta source, IWorkflowStateMeta dest, 
	                              Set<String> roles, boolean signBefore, boolean signAfter){
		this.name = name;
		this.caption = caption;
		this.source = source;
		this.destination = dest;
		this.roles = roles;
		this.signBefore = signBefore;
		this.signAfter = signAfter;
		this.conditions = new LinkedList<Condition>();
		this.assignments = new HashMap<String, Object>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public IWorkflowStateMeta getSource() {
		return source;
	}

	@Override
	public IWorkflowStateMeta getDestination() {
		return destination;
	}

	@Override
	public Collection<Condition> getConditions() {
		return conditions;
	}
	
	public void addCondition(Condition c){
		conditions.add(c);
	}

	@Override
	public Map<String, Object> getPropertyAssignments() {
		return assignments;
	}
	
	public void addAssignment(String property, Object value){
		assignments.put(property, value);
	}

	@Override
	public Set<String> getPermittedRoles() {
		return roles;
	}

	@Override
	public boolean getSignBefore() {
		return signBefore;
	}
	
	public boolean getSignAfter() {
		return signAfter;
	}
}
