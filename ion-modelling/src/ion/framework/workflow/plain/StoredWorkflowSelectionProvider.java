package ion.framework.workflow.plain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredMatrixEntry;
import ion.framework.meta.plain.StoredSelectionProvider;

public class StoredWorkflowSelectionProvider extends StoredSelectionProvider {
	
	public String role;
	public String property;
	
	public StoredWorkflowSelectionProvider(){
		this.role = null;
		this.property = null;
		this.hq = null;
		this.list = new ArrayList<StoredKeyValue>();
		this.matrix = new ArrayList<StoredMatrixEntry>();
		this.parameters = new ArrayList<StoredKeyValue>();
	}
	
	public StoredWorkflowSelectionProvider(String role, String property) {
	  super(new ArrayList<StoredMatrixEntry>());
	  this.role = role;
	  this.property = property;
  }
	
	public StoredWorkflowSelectionProvider(String role, String property, String q, Collection<StoredKeyValue> p) {
	  super(q, p);
	  this.role = role;
	  this.property = property;
  }

	public StoredWorkflowSelectionProvider(String role, String property, Collection<StoredMatrixEntry> matrix) {
	  super(matrix);
	  this.role = role;
	  this.property = property;
  }

	public StoredWorkflowSelectionProvider(String role, String property, Map<String, String> list) {
	  super(list);
	  this.role = role;
	  this.property = property;
  }
	
	public StoredWorkflowSelectionProvider(String role, String property, List<StoredKeyValue> list) {
	  super(list);
	  this.role = role;
	  this.property = property;
  }
}
