package ion.core.workflow.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ion.core.IWorkflowMeta;
import ion.core.IWorkflowStateMeta;
import ion.core.IWorkflowTransitionMeta;

public class WorkflowMeta implements IWorkflowMeta {
	
	protected String name;
	
	protected String className;
	
	protected IWorkflowStateMeta start;
	
	protected Map<String, IWorkflowStateMeta> states;
	
	protected Map<String, IWorkflowTransitionMeta> transitions;

	public WorkflowMeta(String name, String className){
		this(name, className, null);
	}	
	
	public WorkflowMeta(String name, String className, IWorkflowStateMeta start){
		this.name = name;
		this.className = className;
		this.start = start;
		this.states = new HashMap<String, IWorkflowStateMeta>();
		this.transitions = new HashMap<String, IWorkflowTransitionMeta>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getApplicationClassName() {
		return className;
	}

	@Override
	public IWorkflowStateMeta getStartState() {
		return start;
	}

	@Override
	public Collection<IWorkflowStateMeta> getStates() {
		return states.values();
	}
	
	public void addState(IWorkflowStateMeta state){
		states.put(state.getName(),state);
	}

	@Override
	public Collection<IWorkflowTransitionMeta> getTransitions() {
		return transitions.values();
	}
	
	public void addTransition(IWorkflowTransitionMeta t){
		transitions.put(t.getName(), t);
	}

	@Override
	public IWorkflowStateMeta getState(String name) {
		return states.get(name);
	}

	@Override
	public IWorkflowTransitionMeta getTransition(String name) {
		return transitions.get(name);
	}
}
