package ion.viewmodel.plain;

import ion.viewmodel.view.CollectionFieldMode;
import ion.viewmodel.view.FieldSize;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.HistoryDisplayMode;
import ion.viewmodel.view.ReferenceFieldMode;

import java.util.Collection;
import java.util.LinkedList;

public class StoredField implements Comparable<StoredField>{

	public String caption;
	
	public Integer type;
	
	public String property;
	
	public Integer size;
	
	public String maskName;
	
	public String mask;
	
	public Integer mode;
	
	public Collection<StoredField> fields;
	
	public Collection<String> hierarchyAttributes;
	
	public Collection<StoredColumn> columns;
	
	@Deprecated
	public Integer actions;
	
	public Collection<StoredAction> commands;
	
	public Integer order_number; 
	
	public Boolean required;
	
	public String visibility;
	
	public String enablement;
	
	public String obligation;
	
	public Boolean readonly;
	
	public Boolean selection_paginated;
	
	public Collection<String> validators;
	
	public String hint;
	
	public int historyDisplayMode = 0;
	
	public StoredField(String caption, Integer type, String property, 
			Integer size, String maskName, Integer mode,
			Collection<StoredField> fields, Collection<StoredColumn> columns, 
			Collection<StoredAction> actions, Integer order_number, Boolean required, String mask,
			String visibility, String enablement, String obligation, Boolean readonly, 
			Boolean selection_paginated, Collection<String> validators, String hint, Integer hdm) {
		this.caption = caption;
		this.type = type;
		this.property = property;
		this.size = size;
		this.maskName = maskName;
		this.mask = mask;
		this.mode = mode;
		this.fields = fields;
		this.columns = columns;
		this.commands = actions;
		this.required = required;
		this.order_number = order_number;
		this.visibility = visibility;
		this.enablement = enablement;
		this.obligation = obligation;
		this.readonly = readonly;
		this.validators = validators;
		this.selection_paginated = selection_paginated;
		this.hint = hint;
		this.historyDisplayMode = (hdm == null)?HistoryDisplayMode.BYCLASS.getValue():hdm;
	}
	
	public StoredField(String caption, Integer type, String property, Integer size, String maskName, Integer mode,
			Collection<StoredField> fields, Collection<StoredColumn> columns, Collection<StoredAction> actions, Integer order_number, Boolean selection_paginated, String hint, Integer hdm) {
		this(caption, type, property, size, maskName, mode, fields, columns, actions, order_number, false, null, null, null, null, false, selection_paginated, null, hint, hdm);
	}
	
	public StoredField(String caption, String property, Integer mode, Collection<StoredColumn> columns, Collection<StoredAction> actions, Boolean selection_paginated, String hint, Integer hdm) {
		this(caption,FieldType.COLLECTION.getValue(),property,null,null,mode,new LinkedList<StoredField>(),columns,actions,0, false, null,null,null,null, false, selection_paginated, null, hint, hdm);
	}
	
	public StoredField(String caption, String property, CollectionFieldMode mode, Collection<StoredColumn> columns, Collection<StoredAction> actions, String hint, Integer hdm) {
		this(caption,property,mode.getValue(),columns,actions, true, hint, hdm);
	}
	
	public StoredField(String caption, String property, Collection<StoredColumn> columns, Collection<StoredAction> actions, String hint, Integer hdm) {
		this(caption,property,CollectionFieldMode.LINK,columns,actions, hint, hdm);
	}
	
	public StoredField(String caption, String property, Integer mode, Collection<StoredField> fields, String hint, Integer hdm) {
		this(caption,FieldType.REFERENCE.getValue(),property,null,null,mode,fields,new LinkedList<StoredColumn>(),new LinkedList<StoredAction>(), 0, false, null, null, null, null, false, true, null, hint, hdm);
	}
	
	public StoredField(String caption, String property, ReferenceFieldMode mode, Collection<StoredField> fields, String hint, Integer hdm) {
		this(caption,property,mode.getValue(),fields, hint, hdm);
	}
	
	public StoredField(String caption, String property, Collection<StoredField> fields, String hint, Integer hdm) {
		this(caption,property,ReferenceFieldMode.LINK,fields, hint, hdm);
	}	
	
	public StoredField(String caption, Collection<StoredField> fields, String hint) {
		this(caption,FieldType.GROUP.getValue(),null,null,null,null,fields,new LinkedList<StoredColumn>(), new LinkedList<StoredAction>(), 0, false, null, null, null, null, false, true, null, hint, null);
	}
	
	public StoredField(String caption, String property, Integer size, String maskName, String hint) {
		this(caption,FieldType.TEXT.getValue(),property,size,maskName,null,new LinkedList<StoredField>(),new LinkedList<StoredColumn>(), new LinkedList<StoredAction>(), 0, false, null, null, null, null, false, true, null, hint, null);
	}
	
	public StoredField(String caption, String property, FieldSize size, String maskName, String hint) {
		this(caption,property,(size != null)?size.getValue():null,maskName);
	}
	
	public StoredField(String caption, String property, FieldType type, FieldSize size, Integer order_number, String hint) {
		this(caption,type.getValue(),property,(size != null)?size.getValue():null,null,null,new LinkedList<StoredField>(),new LinkedList<StoredColumn>(), new LinkedList<StoredAction>(), order_number, false, null, null, null, null, false, true, null, hint, null);
	}
	

	public StoredField(String caption, String property, FieldSize size, String hint) {
		this(caption,property,size,null, hint);
	}
	
	public StoredField(String caption, String property, String hint) {
		this(caption,property,FieldSize.MEDIUM,null, hint);
	}
	
	public StoredField(String caption, String property, FieldType type, Integer order_number, String hint, Integer hdm) {
		this(caption, type.getValue(), property, 
			FieldSize.MEDIUM.getValue(), null, null,
			new LinkedList<StoredField>(), new LinkedList<StoredColumn>(), new LinkedList<StoredAction>(), 
			order_number, false, null, null, null, null, false, true, null, hint, hdm);
	}	
	
	public StoredField(String caption, String property, Integer order_number, String hint) {
		this(caption, FieldType.TEXT.getValue(), property, 
			FieldSize.MEDIUM.getValue(), null, null,
			new LinkedList<StoredField>(), new LinkedList<StoredColumn>(), 
			new LinkedList<StoredAction>(), 
			order_number, false, null, null, null, null, false, true, null, hint, null);
	}
	
	public StoredField(String caption, String property, FieldType type, int order_number, boolean required, String hint) {
		this(caption, property, type, order_number, hint, null);
		this.required = required;
	}
	
	public StoredField(String caption, String property, FieldType type, int order_number, boolean required, boolean readonly, String hint) {
		this(caption, property, type, order_number, required, hint);
		this.readonly = readonly;
	}
	

	@Override
	public int compareTo(StoredField field) {
		int n1 = this.order_number == null?0:this.order_number;
		int n2 = field.order_number == null?0:field.order_number;
		return n1 - n2;
	}		
}
