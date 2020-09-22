package ion.framework.meta.plain;

import java.util.List;

public class StoredPropertyPermissions {
	
	public String property;
	public List<StoredPermissions> permissions;
	
	public StoredPropertyPermissions(String property,List<StoredPermissions> permissions) {
	  this.property = property;
	  this.permissions = permissions;
  }
	
	
}
