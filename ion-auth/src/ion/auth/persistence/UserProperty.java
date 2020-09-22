package ion.auth.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table (name="user_property")
@IdClass(UserPropertyId.class)
public class UserProperty {

	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")	
	private User user;
	
	@Id
	private String name;
	
	private String value;
	
	public UserProperty(){
		
	}
	
	public UserProperty(User u, String n, String v){
		user = u;
		name = n;
		value = v;
	}	

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
