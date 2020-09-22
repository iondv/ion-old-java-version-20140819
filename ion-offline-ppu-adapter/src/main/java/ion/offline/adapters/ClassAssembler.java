package ion.offline.adapters;

import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Собирает все объявленные и наследованные свойства в один список properties */
public class ClassAssembler {
	public String name;
	public String key;
	public String ancestor;
	public Map<String, StoredPropertyMeta> properties;
	
	private void addProperties(Collection<StoredPropertyMeta> props){
		for (StoredPropertyMeta pm: props)
			properties.put(pm.name, pm);
	}
	
	public ClassAssembler(StoredClassMeta cm, Map<String, StoredClassMeta> classes){
		name = cm.name;
		properties = new LinkedHashMap<String, StoredPropertyMeta>();
		key = cm.key.toArray(new String[1])[0];
		ancestor = cm.ancestor;
		StoredClassMeta c = cm;
		while ((key == null || key.isEmpty()) && c.ancestor != null && classes.containsKey(c.ancestor)){
			c = classes.get(c.ancestor);
			key = c.key.toArray(new String[1])[0];;
			addProperties(c.properties);
		}
		addProperties(cm.properties);
	}		
}
