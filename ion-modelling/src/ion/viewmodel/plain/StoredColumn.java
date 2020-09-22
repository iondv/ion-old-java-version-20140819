package ion.viewmodel.plain;

import ion.viewmodel.view.FieldSize;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.HistoryDisplayMode;


public class StoredColumn extends StoredField {
	
	public Boolean sorted = false;

	public StoredColumn(String caption, String property, FieldType type, FieldSize size, Boolean read_only, Boolean sorted, Integer order_number, String hint) {
		super(caption, property, type, size, order_number, hint);
		this.readonly = read_only;
		this.sorted = sorted;
	}

	
	public StoredColumn(String caption, String property, FieldType type, FieldSize size, Boolean sorted, Integer order_number, String hint) {
		super(caption, property, type, size, order_number, hint);
		this.sorted = sorted;
	}
		
	public StoredColumn(String caption, String property, FieldType type, Boolean sorted, Integer order_number, String hint) {
		super(caption, property, type, order_number, hint, HistoryDisplayMode.HIDE.getValue());
		this.sorted = sorted;
	}
	
	
	public StoredColumn(String caption, String property, Boolean sorted, Integer order_number, String hint) {
		super(caption,property,order_number, hint);
		this.sorted = sorted;
	}

	public StoredColumn(String caption, String property, String hint) {
		this(caption,property,true,0,hint);
	}	
}
