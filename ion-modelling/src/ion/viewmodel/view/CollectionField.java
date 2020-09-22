package ion.viewmodel.view;

import ion.core.HistoryMode;

import java.util.Collection;
import java.util.LinkedList;

public class CollectionField extends DataField implements ICollectionField {

	CollectionFieldMode mode;
	
	Collection<IListColumn> columns;
		
	HistoryDisplayMode historyDisplayMode = HistoryDisplayMode.BYCLASS;
	HistoryMode historyMode = HistoryMode.NONE;	
	
	public CollectionField(String caption, String property, CollectionFieldMode mode, Collection<IListColumn> columns, Collection<FieldAction> actions, Integer order_number, Boolean readonly, Boolean required, String hint, HistoryDisplayMode hdm) {
		super(caption, property, FieldSize.MEDIUM, null, FieldType.COLLECTION, order_number, hint, required, null, readonly);
		this.mode = mode;
		this.actions = actions;
		this.columns = columns;
		this.historyDisplayMode = hdm;
	}	

	public CollectionField(String caption, String property, CollectionFieldMode mode, Collection<IListColumn> columns, Collection<FieldAction> actions, Integer order_number, Boolean readonly, String hint, HistoryDisplayMode hdm) {
		super(caption, property, FieldSize.MEDIUM, null, FieldType.COLLECTION, order_number, hint, readonly);
		this.mode = mode;
		this.actions = actions;
		this.columns = columns;
		this.historyDisplayMode = hdm;
	}
	
	public CollectionField(String caption, String property, CollectionFieldMode mode, Boolean readonly, String hint, HistoryDisplayMode hdm) {
		this(caption, property, mode,new LinkedList<IListColumn>(),new LinkedList<FieldAction>(),0,readonly, hint, hdm);
	}

	@Override
	public CollectionFieldMode getMode() {
		return mode;
	}
	
	public void addColumn(IListColumn c){
		columns.add(c);
	}

	@Override
	public Collection<IListColumn> getColumns() {
		return columns;
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
}
