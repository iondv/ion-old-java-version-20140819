package ion.viewmodel.plain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.FormAction;
import ion.viewmodel.view.HistoryDisplayMode;


public class StoredFormViewModel extends StoredViewModel{

	public Collection<StoredTab> tabs;
	
	/** заменено на commands */
	@Deprecated
	public Integer actions;
	
	public Collection<StoredAction> commands;
	
	public Collection<String> siblingFixBy;
	
	public Collection<String> siblingNavigateBy;
	
	public int historyDisplayMode = 0;
	
	public Collection<StoredCollectionFilter> collectionFilters;

	public StoredFormViewModel() {
		this(null, null, null, null);
	}
	
	public StoredFormViewModel(Collection<StoredTab> tabs, Set<ActionType> actions, Integer overrideType, Integer hdm) {
		if (tabs == null)
			tabs = new ArrayList<StoredTab>();
		this.tabs = tabs;
		this.commands = new ArrayList<StoredAction>();
		for (FormAction action: FormAction.CreateDefaultActions(actions).values()) {
			this.commands.add(new StoredAction(action.getId(), action.getCaption(), action.getVisibilityExpression(), action.getEnablementExpression(), false, false, false));
		}
		this.overrideMode = overrideType;
		this.historyDisplayMode = (hdm == null)?HistoryDisplayMode.BYCLASS.getValue():hdm;
	}
	
	public StoredFormViewModel(Collection<StoredTab> tabs, Integer overrideType, Integer hdm) {
		this(tabs, FormAction.IntToActionTypeSet(ActionType.SAVE.getValue()), overrideType, hdm);
	}	
}
