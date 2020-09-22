package ion.offline.net;

import java.util.HashMap;
import java.util.Map;

public class DataUnit {
	public String className;
	
	public String id;
	
	public Map<String, Object> data;
	
	public DataUnit(){
		data = new HashMap<String, Object>();
	}	

	public DataUnit(String id, String classname, Map<String, Object> data){
		this.id = id;
		className = classname;
		this.data = data;
	}
}
