package ion.offline.net;

import java.util.Map;

public class DataChange extends DataUnit {

	public String author;
	
	public String action;
	
	public DataChange(String auth, String act, String id, String classname, Map<String, Object> data) {
		super(id, classname, data);
		author = auth;
		action = act;
	}
}
