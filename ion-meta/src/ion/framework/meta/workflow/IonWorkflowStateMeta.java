package ion.framework.meta.workflow;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.ISelectionProvider;
import ion.core.workflow.meta.WorkflowStateMeta;
import ion.framework.meta.IonMetaConditionalSelectionProvider;
import ion.framework.meta.IonMetaQuerySelectionProvider;
import ion.framework.meta.IonMetaSimpleSelectionProvider;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredPermissions;
import ion.framework.meta.plain.StoredPropertyPermissions;
import ion.framework.meta.plain.StoredSelectionProvider;
import ion.framework.workflow.plain.StoredWorkflowSelectionProvider;
import ion.framework.workflow.plain.StoredWorkflowState;

public class IonWorkflowStateMeta extends WorkflowStateMeta {
	public IonWorkflowStateMeta(StoredWorkflowState model){
		super(model.name);
		
		for (StoredCondition c: model.conditions){
			addCondition(new Condition(c.property, ConditionType.fromInt(c.operation), c.value));
		}
		
		for (StoredPermissions p: model.itemPermissions){
			addItemPermissions(p.role, p.permissions);
		}
		
		for (StoredPropertyPermissions p: model.propertyPermissions){
			for (StoredPermissions sp: p.permissions)
				addPropertyPermissions(sp.role, p.property, sp.permissions);
		}
		
		for (StoredWorkflowSelectionProvider sp: model.selectionProviders){
			setSelectionProvider(sp.role, sp.property, createSelectionProvider(sp));
		}
	}
	
	private ISelectionProvider createSelectionProvider(StoredWorkflowSelectionProvider selectionProvider){
		if(selectionProvider != null){
			switch (selectionProvider.type){
				case StoredSelectionProvider.TYPE_HQL:
					return new IonMetaQuerySelectionProvider(selectionProvider.hq, selectionProvider.parameters);
				case StoredSelectionProvider.TYPE_MATRIX:
					return new IonMetaConditionalSelectionProvider(selectionProvider.matrix);
				case StoredSelectionProvider.TYPE_SIMPLE:
					return new IonMetaSimpleSelectionProvider(selectionProvider.list);
			}
		}
		return null;
	}	
}
