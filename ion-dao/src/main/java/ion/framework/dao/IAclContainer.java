package ion.framework.dao;

import ion.core.DACPermission;
import ion.core.IUserContext;

import java.util.Set;

public interface IAclContainer {
	Set<DACPermission> InstancePermissions(IUserContext u);
}
