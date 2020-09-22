package ion.viewmodel.view;

import ion.core.HistoryMode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class FormViewModel implements IFormViewModel {

	String version;
	Collection<IFormTab> tabs;
	Map<String, FormAction> actions;
	ViewApplyMode overrideMode;
	HistoryDisplayMode historyDisplayMode = HistoryDisplayMode.BYCLASS;
	HistoryMode historyMode = HistoryMode.NONE;
	SiblingNavigation siblingNav = null;
	Collection<CollectionFilter> collectionFilters = null;
	
	public FormViewModel(String version, Collection<IFormTab> tabs, Map<String, FormAction> actions, ViewApplyMode overrideMode, HistoryDisplayMode hdm, SiblingNavigation sn, Collection<CollectionFilter> cf) {
		this.version = version;
		this.tabs = tabs;
		this.actions = actions;
		this.overrideMode = overrideMode;
		this.historyDisplayMode = hdm;
		this.siblingNav = sn;
		this.collectionFilters = cf;
	}

	public FormViewModel(String version, Collection<IFormTab> tabs, Set<ActionType> actions, ViewApplyMode overrideMode, HistoryDisplayMode hdm) {
		this(version, tabs, FormAction.CreateDefaultActions(actions), overrideMode, hdm, null,new LinkedList<CollectionFilter>());
	}
	
	@Override
	public Collection<IFormTab> getTabs() {
		return tabs;
	}

	@Override
	public Map<String, FormAction> getActions() {
		return actions;
	}

	@Override
	public Collection<FormAction> getActionList() {
		return actions.values();
	}
	
	@Override
	public Boolean ActionAvailable(String code) {
		return (code != null) && actions.containsKey(code);
	}
	
	@Override
	public ViewApplyMode getMode() {
		return overrideMode;
	}

	@Override
	public HistoryDisplayMode getHistoryDisplayMode() {
		return historyDisplayMode;
	}

	@Override
	public HistoryMode getHistoryMode() {
		return historyMode;
	}
	
	public void setHistoryMode(HistoryMode hm){
		historyMode = hm;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public SiblingNavigation getSiblingNavigation() {
		return siblingNav;
	}

	@Override
	public Collection<CollectionFilter> getCollectionFilters() {
		return collectionFilters;
	}
}
