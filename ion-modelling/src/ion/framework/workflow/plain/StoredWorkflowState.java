package ion.framework.workflow.plain;

import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredPermissions;
import ion.framework.meta.plain.StoredPropertyPermissions;

import java.util.ArrayList;
import java.util.List;

public class StoredWorkflowState {

	public String name;
	public String caption;
	public List<StoredCondition> conditions;
	public List<StoredPermissions> itemPermissions;
	public List<StoredPropertyPermissions> propertyPermissions;
	public List<StoredWorkflowSelectionProvider> selectionProviders;
	
	public StoredWorkflowState(String name,
	                           String caption,
	                           List<StoredCondition> conditions,
	                           List<StoredPermissions> itemPermissions, 
	                           List<StoredPropertyPermissions> propertyPermissions,
	                           List<StoredWorkflowSelectionProvider> selectionProviders) {
	  this.name = name;
	  this.caption = caption;
	  this.conditions = conditions;
	  this.itemPermissions = itemPermissions;
	  this.propertyPermissions = propertyPermissions;
	  this.selectionProviders = selectionProviders;
  }

	public StoredWorkflowState() {
		this(null,null,
		     new ArrayList<StoredCondition>(),
		     new ArrayList<StoredPermissions>(),
		     new ArrayList<StoredPropertyPermissions>(),
		     new ArrayList<StoredWorkflowSelectionProvider>());
	}
	
}
