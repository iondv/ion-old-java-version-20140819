package ion.core.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ion.core.IUserContext;

public class UserContext implements IUserContext {
	private String uid;
	
	private String login;
	
	private Collection<String> groups;
	
	private Collection<String> global_roles;
	
	private Map<String, Object> properties;
		
	public UserContext(String uid, String login, Collection<String> groups, Collection<String> roles, Map<String, Object> properties){
		this.uid = uid;
		this.login = login;
		this.groups = groups;
		this.global_roles = roles;
		this.properties = properties;
	}
	
	public UserContext(String uid, String login){
		this(uid,login,new ArrayList<String>(),new ArrayList<String>(), new HashMap<String, Object>());
	}
		
	@Override
	public String getUid() {
		return uid;
	}
	
	@Override
	public String toString() {
		return login;
	}
	
	public Boolean IsCollaborator(String uid){
		return groups.contains(uid);
	}

	@Override
	public Boolean CheckGlobalRoles(Collection<String> roles) {
		Collection<String> tmp = new ArrayList<String>();
		tmp.addAll(roles);
		tmp.retainAll(global_roles);
		return !tmp.isEmpty();
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public Collection<String> getCollaborators() {
		Collection<String> r = new LinkedList<String>();
		r.addAll(groups);
		r.addAll(global_roles);
		return r;
	}
}
