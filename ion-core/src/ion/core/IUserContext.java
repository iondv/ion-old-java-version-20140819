package ion.core;

import java.util.Collection;
import java.util.Map;

public interface IUserContext {
	String getUid();
		
	Boolean IsCollaborator(String uid);
	
	Collection<String> getCollaborators();
	
	Boolean CheckGlobalRoles(Collection<String> roles);
	
	Map<String, Object> getProperties();
}
