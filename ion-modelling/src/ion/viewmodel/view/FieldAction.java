package ion.viewmodel.view;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

public class FieldAction extends Action {

	protected String visibilityExpression = null;
	
	protected String enablementExpression = null;
		
	public FieldAction(String id, String caption, 
	                   String visibilityCondition,
	                    String enableCondition) {
		super(id, caption);
		this.visibilityExpression = visibilityCondition;
		this.enablementExpression = enableCondition;
	}
	
	public FieldAction(String id, String caption) {
		this(id, caption, null, null);
	}	

	public static Collection<FieldAction> CreateDefaultActions(Set<ActionType> actions) {
		Collection<FieldAction> res = new LinkedList<FieldAction>();
		if (actions != null) {
			for (ActionType actiontype: actions) {
				String typename = actiontype.toString();
				FieldAction action = new FieldAction(typename, actiontype.getCaption());
				res.add(action);
			}
		}
		return res;
	}	
}
