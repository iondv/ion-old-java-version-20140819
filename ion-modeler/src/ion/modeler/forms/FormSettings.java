package ion.modeler.forms;

import java.util.HashMap;
import java.util.Map;

public class FormSettings {

	public Map<String,String> types;
	
	public Map<String, Object> selections;
	
	public Map<String,String> captions;
		
	public FormSettings(Map<String,String> captions, Map<String,String> types, Map<String, Object> selections) {
		if (types == null) {
			types = new HashMap<String,String>();
		}
		this.types = types;
		this.captions = captions;
		this.selections = selections;
	}
	
	public FormSettings(Map<String,String> captions, Map<String,String> types) {
		this(captions,types, new HashMap<String,Object>());
	}

	public FormSettings(Map<String,String> captions) {
		this(captions, new HashMap<String,String>());
	}
}
