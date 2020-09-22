package ion.modeler.view;

import ion.modeler.resources.IonUserTypeResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserTypesCache {

	public boolean Dirty;
	
	public Map<String, IonUserTypeResource> items;
 	
	public UserTypesCache() {
		Dirty = true;
		items = new LinkedHashMap<String, IonUserTypeResource>();
	}

}
