package ion.web.app.util;

import java.util.List;

import ion.core.DACPermission;
import ion.core.IAcl;
import ion.core.IAuthContext;

public class AclManager {
	
	private IAcl acl;
	private IAuthContext authContext;
	
	private static final String CLASS_PREFIX = "c:::";
	private static final String OBJECT_PREFIX = "o:::";
	private static final String NODE_PREFIX = "n:::";
	
	public IAcl getAcl() {
		return acl;
	}
	public void setAcl(IAcl acl) {
		this.acl = acl;
	}
	public IAuthContext getAuthContext() {
		return authContext;
	}
	public void setAuthContext(IAuthContext authContext) {
		this.authContext = authContext;
	}
	
	public List<String> getClassAccessList(DACPermission[] permissions){
		List<String> result = acl.GetGranted(authContext.CurrentUser(), permissions, CLASS_PREFIX);
		return result;
	}
	
	public List<String> getObjectAccessList(DACPermission[] permissions){
		List<String> result = acl.GetGranted(authContext.CurrentUser(), permissions, OBJECT_PREFIX);
		return result;
	}
	
	public boolean checkClassAccess(String classname, DACPermission[] permissions){
		return acl.CheckAccess(CLASS_PREFIX+classname, authContext.CurrentUser(), permissions);
	}
	
	public boolean checkObjectAccess(String classname, String id, DACPermission[] permissions){
		return acl.CheckAccess(OBJECT_PREFIX+classname+"@"+id, authContext.CurrentUser(), permissions);
	}
	
	public boolean checkObjectAccess(String objectId, DACPermission[] permissions){
		return acl.CheckAccess(OBJECT_PREFIX+objectId, authContext.CurrentUser(), permissions);
	}
	
	public boolean checkNodeAccess(String node, DACPermission[] permissions){
		return acl.CheckAccess(NODE_PREFIX+node, authContext.CurrentUser(), permissions);
	}
	
}
