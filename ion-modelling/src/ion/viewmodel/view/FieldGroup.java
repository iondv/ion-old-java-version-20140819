package ion.viewmodel.view;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FieldGroup extends Field implements IFieldGroup {

	List<IField> fields;

	public FieldGroup(String caption, List<IField> fields, String name, Integer order_number, Boolean readonly, String hint) {
		super(caption, FieldType.GROUP, name, order_number, hint, readonly);
		this.fields = fields;
		Collections.sort(this.fields);
	}	
	
	public FieldGroup(String caption, String name, Boolean readonly, String hint) {
		this(caption, new ArrayList<IField>(), name, 0, readonly, hint);
	}
	
	public void add(IField f){
		fields.add(f);
		Collections.sort(this.fields);
	}

	@Override
	public Collection<IField> getFields() {
		return fields;
	}
}
