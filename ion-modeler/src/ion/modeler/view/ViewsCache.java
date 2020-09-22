package ion.modeler.view;

import ion.modeler.resources.IonViewResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class ViewsCache {
	
	public boolean Dirty;
	
	public Map<String, IonViewResource> views;	

	public ViewsCache() {
		Dirty = true;
		views = new LinkedHashMap<String, IonViewResource>();
	}

}
