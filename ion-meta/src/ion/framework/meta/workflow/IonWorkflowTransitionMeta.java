package ion.framework.meta.workflow;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.workflow.meta.WorkflowTransitionMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.workflow.plain.StoredWorkflowTransition;

public class IonWorkflowTransitionMeta extends WorkflowTransitionMeta {
	public IonWorkflowTransitionMeta(StoredWorkflowTransition model, IonWorkflowStateMeta source, IonWorkflowStateMeta dest){
		super(model.name, model.caption, source, dest, model.roles, model.signBefore, model.signAfter);
		
		source.addNextTransition(this);
		
		for (StoredKeyValue a: model.assignments)
			addAssignment(a.key, a.value);
		
		for (StoredCondition c: model.conditions)
			addCondition(new Condition(c.property, ConditionType.fromInt(c.operation), c.value));
	}
}
