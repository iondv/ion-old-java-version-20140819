package ion.core.workflow.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ion.core.Condition;
import ion.core.DACPermission;
import ion.core.ISelectionProvider;
import ion.core.IWorkflowStateMeta;
import ion.core.IWorkflowTransitionMeta;

public class WorkflowStateMeta implements IWorkflowStateMeta {
	
	protected String name;

	protected Collection<Condition> conditions;
	
	protected Map<String, Set<DACPermission>> itemPermissions;
	
	protected Map<String, Map<String, Set<DACPermission>>> propertyPermissions;
	
	protected Map<String, Map<String, ISelectionProvider>> selectionProviders;
	
	protected Set<IWorkflowTransitionMeta> nextTransitions;
	
	public WorkflowStateMeta(String name){
		this.name = name;
		conditions = new LinkedList<Condition>();
		itemPermissions = new HashMap<String, Set<DACPermission>>();
		propertyPermissions = new HashMap<String, Map<String,Set<DACPermission>>>();
		selectionProviders = new HashMap<String, Map<String,ISelectionProvider>>();
		nextTransitions = new LinkedHashSet<IWorkflowTransitionMeta>();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<Condition> getConditions() {
		return conditions;
	}
	
	public void addCondition(Condition c){
		conditions.add(c);
	}

	@Override
	public Map<String, Set<DACPermission>> getItemPermissions() {
		return itemPermissions;
	}
	
	public void addItemPermissions(String role, int permissionMask){
		if (!itemPermissions.containsKey(role))
			itemPermissions.put(role, new HashSet<DACPermission>());
		itemPermissions.get(role).addAll(DACPermission.parseInt(permissionMask));
	}

	@Override
	public Map<String, Map<String, Set<DACPermission>>> getPropertyPermissions() {
		return propertyPermissions;
	}
	
	public void addPropertyPermissions(String role, String property, int permissionMask){
		if (!propertyPermissions.containsKey(role))
			propertyPermissions.put(role, new HashMap<String, Set<DACPermission>>());
		if (!propertyPermissions.get(role).containsKey(property))
			propertyPermissions.get(role).put(property, new HashSet<DACPermission>());
		propertyPermissions.get(role).get(property).addAll(DACPermission.parseInt(permissionMask));
	}

	@Override
	public Map<String, Map<String, ISelectionProvider>> getSelectionProviders() {
		return selectionProviders;
	}
	
	public void setSelectionProvider(String role, String property, ISelectionProvider provider){
		if (!selectionProviders.containsKey(role))
			selectionProviders.put(role, new HashMap<String, ISelectionProvider>());
		selectionProviders.get(role).put(property, provider);
		
	}

	@Override
	public Set<IWorkflowTransitionMeta> getNextTransitions() {
		return nextTransitions;
	}
	
	public void addNextTransition(IWorkflowTransitionMeta t){
		nextTransitions.add(t);
	}
}
