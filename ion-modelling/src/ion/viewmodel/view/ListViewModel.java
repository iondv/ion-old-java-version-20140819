package ion.viewmodel.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListViewModel implements IListViewModel {
	
	String version;

	Map<String, ListAction> actions;
	
	List<IListColumn> columns;
	
	Integer pageSize;
	
	Boolean allowSearch;

	/** можно ли брать в качестве детализации модель представления формы изменения при отсутствии модели представления детализации */
	Boolean useEditModels;
	
	ViewApplyMode overrideMode;
	
	public ListViewModel(){
		this(new ArrayList<IListColumn>(), 20,true,new HashSet<ActionType>(),null,true);
	}
	
	public ListViewModel(String version, List<IListColumn> columns, Integer pageSize,
												Boolean allowSearch, Map<String, ListAction> actions,
												ViewApplyMode overrideMode, Boolean useEditModels) {
		this.version = version;
		this.columns = columns;
		this.pageSize = pageSize;
		this.allowSearch = allowSearch;
		this.actions = actions;
		this.overrideMode = overrideMode;
		this.useEditModels = useEditModels;
		Collections.sort(this.columns);
	}
	
	public ListViewModel(List<IListColumn> columns, Integer pageSize,
												Boolean allowSearch, Set<ActionType> actions,
												ViewApplyMode overrideMode, Boolean useEditModels) {
		this("",columns, pageSize, allowSearch, ListAction.CreateDefaultActions(actions), overrideMode, useEditModels);
	}

	@Override
	public Map<String, ListAction> getActions() {
		return actions;
	}

	@Override
	public Collection<IListColumn> getColumns() {
		return columns;
	}

	@Override
	public Integer getPageSize() {
		return pageSize;
	}

	@Override
	public Boolean getAllowSearch() {
		return allowSearch;
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
	public Boolean getUseEditModels() {
		return useEditModels;
	}

	@Override
  public Boolean hasBulkActions() {
	  for(Action a : actions.values()){
	  	if(((ListAction)a).isBulk == true)
	  		return true;
	  }
	  return false;
  }

	@Override
	public String getVersion() {
		return version;
	}

}
