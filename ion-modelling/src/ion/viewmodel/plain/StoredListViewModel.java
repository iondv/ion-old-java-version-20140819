package ion.viewmodel.plain;

import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.ListAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class StoredListViewModel extends StoredViewModel{
	
	public Collection<StoredColumn> columns;
	
	/** заменено на commands */
	@Deprecated
	public Integer actions;
	
	public Collection<StoredListAction> commands;
	
	public Boolean allowSearch = false;
	
	public Integer pageSize;
	
	/** можно ли брать в качестве детализации модель представления формы изменения при отсутствии модели представления детализации */
	public Boolean useEditModels;

	public StoredListViewModel(Collection<StoredColumn> columns, Set<ActionType> actions, Boolean allowSearch, Integer pageSize, Integer overrideType, Boolean useEditModels){
		this.columns = columns;
		this.commands = new ArrayList<StoredListAction>();
		for (ListAction action: ListAction.CreateDefaultActions(actions).values()) {
			this.commands.add(new StoredListAction(action.getId(), action.getCaption(), action.getNeedSelectedItem(), action.getIsBulk()));
		}
		this.allowSearch = allowSearch;
		this.pageSize = pageSize;
		this.overrideMode = overrideType;
		this.useEditModels = useEditModels;
	}
	
	public StoredListViewModel(Collection<StoredColumn> columns, Set<ActionType> actions, Boolean allowSearch, Integer overrideType){
		this(columns,actions,allowSearch,null,overrideType, true);
	}
	
	public StoredListViewModel(Collection<StoredColumn> columns, Set<ActionType> actions, Integer overrideType){
		this(columns,actions,false,overrideType);
	}

	public StoredListViewModel(Collection<StoredColumn> columns, Integer overrideType){
		this(columns, ListAction.IntToActionTypeSet(ActionType.CREATE.getValue() | ActionType.EDIT.getValue() | ActionType.DELETE.getValue()), overrideType);
	}
	
	public StoredListViewModel(Collection<StoredColumn> columns, Integer overrideType, Boolean useEditModels){
		this(columns, ListAction.IntToActionTypeSet(ActionType.CREATE.getValue() | ActionType.EDIT.getValue() | ActionType.DELETE.getValue()), overrideType);
		this.useEditModels = useEditModels;
	}
		
	public StoredListViewModel(Integer overrideType){
		this(new ArrayList<StoredColumn>(), overrideType);
	}
	
	public StoredListViewModel(){
		this(new ArrayList<StoredColumn>(),null);
	}
}
