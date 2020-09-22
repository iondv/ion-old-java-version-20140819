package ion.viewmodel.view;

import java.util.Collection;
import java.util.Map;

public interface IListViewModel {
	String getVersion();	
	Map<String, ListAction> getActions();
	Collection<IListColumn> getColumns();
	Integer getPageSize();
	Boolean getAllowSearch();
	Boolean ActionAvailable(String code);
	ViewApplyMode getMode();
	Boolean getUseEditModels();
	Boolean hasBulkActions();
}
