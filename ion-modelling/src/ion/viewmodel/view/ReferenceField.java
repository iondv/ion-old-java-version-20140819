package ion.viewmodel.view;

import ion.core.HistoryMode;

import java.util.Collection;
import java.util.LinkedList;

public class ReferenceField extends DataField implements IReferenceField {

	protected ReferenceFieldMode mode;
	
	protected Collection<String> hierarchy;
	
	protected Collection<IField> fields;
	
	protected boolean selectionPaginated = true;
	
	protected HistoryDisplayMode historyDisplayMode = HistoryDisplayMode.BYCLASS;
	
	protected HistoryMode historyMode = HistoryMode.NONE;	
	
	public ReferenceField(String caption, String property, ReferenceFieldMode mode, Collection<IField> fields, Collection<String> hierarchy, Integer order_number, Boolean readonly, String hint, HistoryDisplayMode hdm) {
		super(caption, property, FieldSize.MEDIUM, null, FieldType.REFERENCE, order_number, hint, readonly);
		this.mode = mode;
		this.fields = fields;
		this.historyDisplayMode = hdm;
		this.hierarchy = hierarchy;
	}
	
	public ReferenceField(String caption, String property, ReferenceFieldMode mode, Collection<IField> fields, Collection<String> hierarchy, Integer order_number, Boolean required, Boolean readonly, String hint, HistoryDisplayMode hdm) {
		this(caption, property, mode, fields, hierarchy, order_number, readonly, hint, hdm);
		this.required = required;
	}
	
	public ReferenceField(String caption, String property, ReferenceFieldMode mode, Boolean readonly, String hint, HistoryDisplayMode hdm) {
		this(caption, property, mode, new LinkedList<IField>(), new LinkedList<String>(), 0, readonly, hint, hdm);
	}
	
	public ReferenceField(String caption, String property, boolean selectionPaginated, ReferenceFieldMode mode, Collection<IField> fields, Integer order_number, Boolean readonly, String hint, HistoryDisplayMode hdm) {
		super(caption, property, FieldSize.MEDIUM, null, FieldType.REFERENCE, order_number, hint, readonly);
		this.mode = mode;
		this.fields = fields;
		this.selectionPaginated = selectionPaginated;
		this.historyDisplayMode = hdm;
	}
	
	public ReferenceField(String caption, String property, boolean selectionPaginated, ReferenceFieldMode mode, Collection<IField> fields, Collection<FieldAction> actions, Collection<String> hierarchy, Integer order_number, Boolean required, Boolean readonly, String hint, HistoryDisplayMode hdm) {
		this(caption, property, mode, fields, hierarchy, order_number, readonly, hint, hdm);
		this.required = required;
		this.selectionPaginated = selectionPaginated;
		this.actions = actions;
	}	

	@Override
	public ReferenceFieldMode getMode() {
		return mode;
	}
	
	public void addField(IField f){
		fields.add(f);
	}

	@Override
	public Collection<IField> getFields() {
		return fields;
	}

	@Override
  public Boolean isSelectionPaginated() {
	  return selectionPaginated;
  }
	
	public void setSelectionPaginated(Boolean value){
		this.selectionPaginated = value;
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
	
	public Collection<String> getHierarchyAttributes(){
		return hierarchy;
	}
}

