package ion.viewmodel.view;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Action {
	protected String id;
	protected String caption;

  public String getId() { return id; }
  public String getCaption() { return caption; }
	
	public Action(String id, String caption) {
		this.id = id;
		this.caption = caption;
	}
	
  public static Set<ActionType> IntToActionTypeSet(Integer actions) {
		Set<ActionType> result = new LinkedHashSet<ActionType>();
		if (actions != null) {
			for (ActionType at: ActionType.values()){
				if ((at.getValue() & actions) > 0)
					result.add(at);
			}
		}
		return result;
	}	
}
