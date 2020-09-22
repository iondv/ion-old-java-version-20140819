package ion.modeler.view;

import ion.modeler.resources.IonValidatorResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValidatorsCache {
	
	public boolean Dirty;
	
	public Map<String, IonValidatorResource> items;
 	
	public ValidatorsCache() {
		Dirty = true;
		items = new LinkedHashMap<String, IonValidatorResource>();
	}

}
