package ion.offline.adapter.util;

import ion.core.IUserContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class AdapterUserContext implements IUserContext {
	
	String uid;
	Collection<String> roles;
	
	public AdapterUserContext(UserDetails d) {
		uid = d.getUsername();
		roles = new ArrayList<String>();	
		for (GrantedAuthority a: d.getAuthorities())
			roles.add(a.getAuthority());
  }

	@Override
  public String getUid() {
		return uid;
  }

	@Override
  public Boolean IsCollaborator(String uid) {
		return false;
  }

	@Override
  public Collection<String> getCollaborators() {
		return this.roles;
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
    return new HashMap<String, Object>();
  }
	
	public String toString(){
		return uid;
	}
	
}
