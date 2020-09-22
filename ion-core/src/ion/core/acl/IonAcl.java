package ion.core.acl;

import java.util.LinkedList;
import java.util.List;

import ion.core.DACPermission;
import ion.core.IAcl;
import ion.core.IUserContext;

public abstract class IonAcl implements IAcl {
	
	protected abstract boolean check(String ouid, String[] uids, int permissions);
	protected abstract List<String> getGrantedList(String[] uids, int permissions);
	protected abstract List<String> getGrantedList(String[] uids, int permissions, String prefix);
	
	@Override
	public boolean CheckAccess(String ouid, IUserContext user, DACPermission[] permissions) {
		List<String> uids = new LinkedList<String>(user.getCollaborators());
		uids.add(user.getUid());
		
		int permask = 0;
		for (DACPermission p: permissions)
			permask = permask | p.getValue();
		
		if (permask != 0)
			return check(ouid, uids.toArray(new String[uids.size()]), permask);
		return true;
	}
	
	@Override
	public List<String> GetGranted(IUserContext user, DACPermission[] permissions) {
		List<String> uids = new LinkedList<String>(user.getCollaborators());
		uids.add(user.getUid());
		
		int permask = 0;
		for (DACPermission p: permissions)
			permask = permask | p.getValue();
		
		if (permask != 0)
			return getGrantedList(uids.toArray(new String[uids.size()]),permask);
		return null;
	}
	
	@Override
	public List<String> GetGranted(IUserContext user, DACPermission[] permissions, String prefix) {
		List<String> uids = new LinkedList<String>(user.getCollaborators());
		uids.add(user.getUid());
		
		int permask = 0;
		for (DACPermission p: permissions)
			permask = permask | p.getValue();
		
		if (permask != 0)
			return getGrantedList(uids.toArray(new String[uids.size()]),permask,prefix);
		return null;
	}
	
	

}
