package ion.viewmodel.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FormAction extends Action {

	protected String visibilityExpression = null;
	
	protected String enablementExpression = null;
	
	protected boolean signBefore = false;
	
	protected boolean signAfter = false;

	public FormAction(String id, String caption) {
	  super(id, caption);
  }

	public FormAction(String id, String caption, String visibilityCondition,
                    String enableCondition, boolean signBefore,
                    boolean signAfter) {
	  super(id, caption);
	  this.visibilityExpression = visibilityCondition;
	  this.enablementExpression = enableCondition;
	  this.signAfter = signAfter;
	  this.signBefore = signBefore;
  }
	
  public String getVisibilityExpression() { return visibilityExpression; }
  public String getEnablementExpression() { return enablementExpression; }
  public boolean getSignBefore(){return signBefore;}
  public boolean getSignAfter(){return signAfter;}	

	public static Map<String, FormAction> CreateDefaultActions(Set<ActionType> actions) {
		Map<String, FormAction> res = new LinkedHashMap<String, FormAction>();
		if (actions != null) {
			for (ActionType actiontype: actions) {
				String typename = actiontype.toString();
				FormAction action = new FormAction(typename, actiontype.getCaption());
				res.put(typename, action);
			}
		}
		return res;
	}

}
