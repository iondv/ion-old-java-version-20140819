package ion.viewmodel.view;

import ion.core.HistoryMode;

import java.util.Collection;

public interface ICollectionField extends IDataField {
	CollectionFieldMode getMode();
	Collection<IListColumn> getColumns();
	HistoryDisplayMode getHistoryDisplayMode();
	HistoryMode getHistoryMode();
}
