package ion.web.app.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ion.core.IUserContext;

public class S2UserContext implements IUserContext{

	String uid;
	Collection<String> roles;
	Map<String, Object> properties;
	
	public S2UserContext(UserDetails d, Map<String, Object> properties){
		uid = d.getUsername();
		roles = new ArrayList<String>();	
		for (GrantedAuthority a: d.getAuthorities())
			roles.add(a.getAuthority());
		this.properties = properties;
	}
	
	@Override
	public String getUid() {
		return uid;
	}
	
	public String toString(){
		return uid;
	}

	@Override
	public Boolean IsCollaborator(String uid) {
		return false;
	}

	@Override
	public Boolean CheckGlobalRoles(Collection<String> roles) {
		Collection<String> check = new ArrayList<String>();
		check.addAll(roles);
		check.retainAll(this.roles);
		return check.size() > 0;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public Collection<String> getCollaborators() {
		return roles;
	}

}
