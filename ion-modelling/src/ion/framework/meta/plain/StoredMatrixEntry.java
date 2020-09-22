package ion.framework.meta.plain;

import java.util.ArrayList;
import java.util.Collection;

public class StoredMatrixEntry {
	
	public String comment;
	
	public Collection<StoredCondition> conditions;
	
	public Collection<StoredKeyValue> result;	

	public StoredMatrixEntry(){
		this("",new ArrayList<StoredCondition>(), new ArrayList<StoredKeyValue>());
	}
	
	public StoredMatrixEntry(String comment, Collection<StoredCondition> conditions, Collection<StoredKeyValue> result) {
		this.comment = comment;
		this.conditions = conditions;
		this.result = result;
	}

}
