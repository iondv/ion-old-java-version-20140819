package ion.modeler.view;

import ion.modeler.resources.IonNodeResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class NodesCache {

	public boolean Dirty;
	
	public Map<String, IonNodeResource> nodes;
 	
	public NodesCache() {
		Dirty = true;
		nodes = new LinkedHashMap<String, IonNodeResource>();
	}

}
