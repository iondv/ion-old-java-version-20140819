package ion.framework.meta;

import ion.core.data.SimpleSelectionProvider;
import ion.framework.meta.plain.StoredKeyValue;

import java.util.Collection;
import java.util.LinkedHashMap;

public class IonMetaSimpleSelectionProvider extends SimpleSelectionProvider {
	
	public IonMetaSimpleSelectionProvider(Collection<StoredKeyValue> list){
		super(new LinkedHashMap<String, String>());
		for (StoredKeyValue kv: list){
			this.selection.put(kv.key, kv.value);
		}
	}
}
