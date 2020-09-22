package ion.modeler.view;

import ion.modeler.resources.IonEntityResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntitiesCache {
	
	public boolean Dirty;
	
	public Map<String, IonEntityResource> entities; 

	public EntitiesCache() {
		Dirty = true;
		entities = new LinkedHashMap<String, IonEntityResource>();
	}
}
