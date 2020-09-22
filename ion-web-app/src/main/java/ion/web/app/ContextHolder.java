package ion.web.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ContextHolder {
	
	private Map<String, Object> paramDefs = new HashMap<>();
	
	protected HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession sess = attr.getRequest().getSession();
        return sess;
	}
	
	public Object getValue(String param) {
		param = param.toLowerCase();
        HttpSession sess = getSession();
        Object value = sess.getAttribute(param);
		if ((value == null) && (paramDefs.containsKey(param))) {
			return paramDefs.get(param);
		}
		return value;
	}

	public void setValue(String param, Object value) {
        HttpSession sess = getSession();
        sess.setAttribute(param.toLowerCase(), value);
	}
	
	public void setDefault(String param, Object value) {
		paramDefs.put(param.toLowerCase(), value);
	}
	
	public void setDefaults(Map<Object, Object> map) {
		for(Entry<Object, Object> entry: map.entrySet()) {
			if (entry.getKey() instanceof String) {
				paramDefs.put(((String)entry.getKey()).toLowerCase(), entry.getValue());
			}
		}
	}
}
