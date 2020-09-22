package ion.viewmodel.view;

import ion.core.HistoryMode;

import java.util.Collection;
import java.util.Map;

public interface IFormViewModel {
	String getVersion();
	Collection<IFormTab> getTabs();
	Map<String, FormAction> getActions();
	Collection<FormAction> getActionList();
	Boolean ActionAvailable(String code);
	ViewApplyMode getMode();
	HistoryDisplayMode getHistoryDisplayMode();
	HistoryMode getHistoryMode();
	SiblingNavigation getSiblingNavigation();
	Collection<CollectionFilter> getCollectionFilters();
}
