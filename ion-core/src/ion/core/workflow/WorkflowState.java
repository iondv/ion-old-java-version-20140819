package ion.core.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import ion.core.DACPermission;
import ion.core.IItem;
import ion.core.IProperty;
import ion.core.ISelectionProvider;
import ion.core.IUserContext;
import ion.core.IWorkflowState;
import ion.core.IWorkflowTransition;
import ion.core.IonException;
import ion.core.MetaPropertyType;

public class WorkflowState implements IWorkflowState {
	
	public static final String EVERYBODY = "---EVERYBODY---";

	private IItem item;
	
	private Map<String, WorkflowTransition[]> nextTransitions;
	
	private Map<String, Integer> itemPermissions;
	
	private Map<String, Map<String, Integer>> propertyPermissions;
	
	private Map<String, Map<String, ISelectionProvider>> selectionProviders;
	
	public WorkflowState(IItem item, Map<String, WorkflowTransition[]> next, Map<String, Integer> itemPermissions, 
                       Map<String, Map<String, Integer>> propertyPermissions, 
                       Map<String, Map<String, ISelectionProvider>> selectionProviders) throws IonException {
		this.item = item;
		adjustUserRoles(next, itemPermissions, propertyPermissions, selectionProviders);
	} 

	@Override
	public IWorkflowTransition[] getNextTransitions(IUserContext user) {
		IWorkflowTransition[] result = new IWorkflowTransition[0];
		
		if (nextTransitions.containsKey(EVERYBODY))
			result = nextTransitions.get(EVERYBODY);
		
		if (nextTransitions.containsKey(user.getUid()))
			result = (IWorkflowTransition[]) ArrayUtils.addAll(result, nextTransitions.get(user.getUid()));
		
		return result;
	}
	
	private void adjustUserRoles(Map<String, WorkflowTransition[]> next,
	                             Map<String, Integer> itemPermissions,
	                             Map<String, Map<String, Integer>> propertyPermissions,
	                             Map<String, Map<String, ISelectionProvider>> selectionProviders) throws IonException{
		Map<String, String> assignedRoles = new HashMap<String, String>();
		for (IProperty p: item.getProperties().values()){
			if (p.getType() == MetaPropertyType.USER){
				if (p.getValue() != null)
					assignedRoles.put(p.getName(), p.getValue().toString());
			}
		}
		
		nextTransitions = new HashMap<String, WorkflowTransition[]>();
		for (Map.Entry<String, WorkflowTransition[]> tr: next.entrySet())
			if (assignedRoles.containsKey(tr.getKey())){
				String uid = assignedRoles.get(tr.getKey());
				
				if (nextTransitions.containsKey(uid))
					nextTransitions.put(uid, (WorkflowTransition[])ArrayUtils.addAll(nextTransitions.get(uid), tr.getValue()));
				else
					nextTransitions.put(uid, tr.getValue());
			} else if (tr.getKey().equals(EVERYBODY)){
  			if (nextTransitions.containsKey(EVERYBODY))
  				nextTransitions.put(EVERYBODY, (WorkflowTransition[])ArrayUtils.addAll(nextTransitions.get(EVERYBODY), tr.getValue()));
  			else
  				nextTransitions.put(EVERYBODY, tr.getValue());
			}
		
		this.itemPermissions = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> tr: itemPermissions.entrySet()){
			if (assignedRoles.containsKey(tr.getKey())){
				String uid = assignedRoles.get(tr.getKey());
				if (this.itemPermissions.containsKey(uid))
					this.itemPermissions.put(uid, this.itemPermissions.get(uid) | tr.getValue());
				else
					this.itemPermissions.put(uid, tr.getValue());
			}
		}
		
		this.propertyPermissions = new HashMap<String, Map<String,Integer>>();
		for (Map.Entry<String, Map<String, Integer>> tr: propertyPermissions.entrySet()){
			for(Map.Entry<String, Integer> p : tr.getValue().entrySet())
				if (assignedRoles.containsKey(p.getKey())){
					String uid = assignedRoles.get(p.getKey());
					if (!this.propertyPermissions.containsKey(uid))
						this.propertyPermissions.put(uid, new HashMap<String, Integer>());
							
//					for (Map.Entry<String, Integer> tr2: tr.getValue().entrySet()){
						if (this.propertyPermissions.get(uid).containsKey(tr.getKey()))
							this.propertyPermissions.get(uid).put(tr.getKey(), 
								  this.propertyPermissions.get(uid).get(tr.getKey()) & p.getValue());
						else
							this.propertyPermissions.get(uid).put(tr.getKey(), p.getValue());
//					}
				}
		}
		
		this.selectionProviders = new HashMap<String, Map<String, ISelectionProvider>>();
		for (Map.Entry<String, Map<String, ISelectionProvider>> tr: selectionProviders.entrySet()){
			if (assignedRoles.containsKey(tr.getKey())){
				String uid = assignedRoles.get(tr.getKey());
				if (!this.selectionProviders.containsKey(uid))
					this.selectionProviders.put(uid, new HashMap<String, ISelectionProvider>());
				
				for (Map.Entry<String, ISelectionProvider> tr2: tr.getValue().entrySet()){
					if (!this.selectionProviders.get(uid).containsKey(tr2.getKey()))
						this.selectionProviders.get(uid).put(tr2.getKey(), tr2.getValue());
				}
			}
		}
	}

	@Override
	public boolean checkItemPermission(IUserContext user, DACPermission permission)
																																								 throws IonException {
		if(itemPermissions.isEmpty()){
			return true;
		} else {
			if (itemPermissions.containsKey(user.getUid())){
				if ((itemPermissions.get(user.getUid()).intValue() & permission.getValue()) != 0)
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean checkPropertyPermission(String property, IUserContext user,
																				 DACPermission permission)
																																	throws IonException {
		if (propertyPermissions.containsKey(user.getUid())){
			if (propertyPermissions.get(user.getUid()).containsKey(property)){
				if ((propertyPermissions.get(user.getUid()).get(property).intValue() & permission.getValue()) != 0)
					return true;
				return false;
			}
		}
		return true;
	}

	@Override
	public ISelectionProvider getSelectionProvider(String property, IUserContext user)
																																						 throws IonException {
		if (selectionProviders.containsKey(user.getUid()))
			if (selectionProviders.get(user.getUid()).containsKey(property))
				return selectionProviders.get(user.getUid()).get(property);
		return null;
	}

}
