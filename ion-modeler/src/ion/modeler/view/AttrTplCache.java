package ion.modeler.view;

import ion.modeler.resources.IonPropertyTemplateResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttrTplCache {
	public boolean Dirty;
	
	public Map<String, IonPropertyTemplateResource> templates; 

	public AttrTplCache() {
		Dirty = true;
		templates = new LinkedHashMap<String, IonPropertyTemplateResource>();
	}
}
