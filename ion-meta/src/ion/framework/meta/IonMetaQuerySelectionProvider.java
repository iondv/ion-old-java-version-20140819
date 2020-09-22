package ion.framework.meta;

import ion.core.data.QuerySelectionProvider;
import ion.framework.meta.plain.StoredKeyValue;

import java.util.Collection;
import java.util.LinkedHashMap;

public class IonMetaQuerySelectionProvider extends QuerySelectionProvider {
	public IonMetaQuerySelectionProvider(String query, Collection<StoredKeyValue> parameters){
		super(query, new LinkedHashMap<String, String>());
		for (StoredKeyValue kv: parameters){
			this.parameters.put(kv.key, kv.value);
		}
	}
}
