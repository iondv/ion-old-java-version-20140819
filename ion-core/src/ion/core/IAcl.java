package ion.core;

import java.util.List;

public interface IAcl {
	public boolean CheckAccess(String ouid, IUserContext user, DACPermission[] permissions);
	public List<String> GetGranted(IUserContext user, DACPermission[] permissions);
	public List<String> GetGranted(IUserContext user, DACPermission[] permissions, String prefix);
}
