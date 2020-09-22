package ion.viewmodel.view;

public class ListColumn extends DataField implements IListColumn {
	
	protected Boolean sortable;
	
	public ListColumn(String caption, String property, FieldType type, FieldSize size, Boolean sortable, Integer order_number, Boolean readonly, String hint) {
		super(caption, property, type, size, order_number, hint, readonly);
		this.sortable = sortable;
	}	
	
	public ListColumn(String caption, String property, FieldType type, Boolean sortable, Integer order_number, Boolean readonly, String hint) {
		super(caption, property, type, null, order_number, hint, readonly);
		this.sortable = sortable;
	}	
		
	public ListColumn(String caption, String property, Boolean sortable, Integer order_number, Boolean readonly, String hint) {
		this(caption, property, FieldType.TEXT, sortable, order_number, readonly, hint);
	}

	public Boolean getSortable() {
		return sortable;
	}


}
