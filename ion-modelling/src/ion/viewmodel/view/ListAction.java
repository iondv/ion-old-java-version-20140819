package ion.viewmodel.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ListAction extends Action {
	
	protected boolean needSelectedItem = false;
	
	protected boolean isBulk = false;
	
	public ListAction(String id, String caption) {
	  super(id, caption);
  }
	
	public ListAction(String id, String caption, boolean isBulk) {
	  super(id, caption);
	  this.isBulk = isBulk;
  }

	public ListAction(String id, String caption, boolean needSelectedItem, boolean isBulk) {
	  super(id, caption);
	  this.needSelectedItem = needSelectedItem;
	  this.isBulk = isBulk;
  }
	
	public boolean getIsBulk(){ return this.isBulk; }

  public boolean getNeedSelectedItem() { return needSelectedItem; }
	
	public static Map<String, ListAction> CreateDefaultActions(Set<ActionType> actions) {
		Map<String, ListAction> res = new LinkedHashMap<String, ListAction>();
		if (actions != null) {
			for (ActionType actiontype: actions) {
				String typename = actiontype.toString();
				if((actiontype == ActionType.DELETE) || (actiontype == ActionType.REMOVE))
					res.put(typename, new ListAction(typename, actiontype.getCaption(), false, true));
				else if ((actiontype == ActionType.EDIT) || (actiontype == ActionType.SAVE)) 
					res.put(typename, new ListAction(typename, actiontype.getCaption(), true, false));
				else
					res.put(typename, new ListAction(typename, actiontype.getCaption()));
			}
		}
		return res;
	}

}
