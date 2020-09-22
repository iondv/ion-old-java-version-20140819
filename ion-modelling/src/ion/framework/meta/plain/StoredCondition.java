package ion.framework.meta.plain;

import java.util.LinkedList;
import java.util.List;

public class StoredCondition {
	public String property;
	public Integer operation;
	public String value;
	public List<StoredCondition> nestedConditions;

	public StoredCondition(){
		nestedConditions = new LinkedList<StoredCondition>();
	}
	
	public StoredCondition(String p, Integer o, String v){
		property = p;
		operation = o;
		value = v;
		nestedConditions = new LinkedList<StoredCondition>();
	}
}