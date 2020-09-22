package ion.modeler.view;

import ion.modeler.resources.IonSectionResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class SectionsCache {

	public boolean Dirty;
	
	public Map<String, IonSectionResource> sections;
 	
	public SectionsCache() {
		Dirty = true;
		sections = new LinkedHashMap<String, IonSectionResource>();
	}
	
}
