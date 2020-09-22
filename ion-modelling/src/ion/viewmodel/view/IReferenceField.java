package ion.viewmodel.view;

import ion.core.HistoryMode;

import java.util.Collection;

public interface IReferenceField extends IDataField {
	ReferenceFieldMode getMode();
	Collection<IField> getFields();
	Boolean isSelectionPaginated();
	HistoryDisplayMode getHistoryDisplayMode();
	HistoryMode getHistoryMode();
	Collection<String> getHierarchyAttributes();
}
