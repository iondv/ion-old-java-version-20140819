package ion.framework.dao.jdbc;

import java.util.Map;

import ion.core.IStructMeta;
import ion.core.data.Item;

public class JdbcItem extends Item {

	public JdbcItem(String id, Map<String, Object> base, IStructMeta itemClass, JdbcDataRepository rep) {
		super(id, base, itemClass, rep);
	}

	@Override
	public Object Get(String name) {
		@SuppressWarnings("unchecked")
		Map<String, Object> v = (Map<String, Object>)base;
		if (v.containsKey(name))
			return v.get(name);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void Set(String name, Object value) {
		((Map<String, Object>)base).put(name, value);
	}
}
