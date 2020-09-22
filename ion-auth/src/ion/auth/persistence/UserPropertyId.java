package ion.auth.persistence;

import java.io.Serializable;

public class UserPropertyId implements Serializable {
	private static final long serialVersionUID = 1L;

	private User user = null;

	private String name = null;	
	
	public UserPropertyId(){
		
	}
	
	public UserPropertyId(User u, String name){
		this.user = u;
		this.name = name;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public int hashCode() {
		if (user != null && name != null)
			return user.getId().hashCode() + name.hashCode();
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserPropertyId){
			if (user != null && name != null)
				if (user.getId().equals(((UserPropertyId)obj).user.getId()) && name.equals(((UserPropertyId)obj).name))
					return true;
		}
		return false;
	}
}
