package ion.core.data;

import ion.core.IItem;
import ion.core.IQuerySelectionProvider;
import ion.core.IonException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class QuerySelectionProvider implements IQuerySelectionProvider {

	protected String query;
	
	protected Map<String, String> parameters;
	
	public QuerySelectionProvider(String query, Map<String, String> parameters){
		this.query = query;
		this.parameters = parameters;
	}
	
	@Override
	public Map<String, String> SelectList() throws IonException {
		return null;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public Map<String, Object> getParameters(IItem item) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map.Entry<String,String> kv: parameters.entrySet()){
			Object v = kv.getValue();
			if (kv.getValue().startsWith("$")){
				v = item.Get(kv.getValue().substring(1));
				if (v instanceof IItem)
					v = ((IItem)v).getItemId();
			}
			result.put(kv.getKey(), v);
		}			
		return result;
	}

	@Override
	public Collection<String> Dependencies() {
		Collection<String> result = new LinkedList<String>();
		for (String pv: parameters.values()){
			if (pv.startsWith("$"))
				result.add(pv.replace("$", ""));
		}
		return result;
	}
}