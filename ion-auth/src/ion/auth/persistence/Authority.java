package ion.auth.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (name= "authority")
public class Authority {
	
	@Id @GeneratedValue
	private Integer id;
	private Integer version;
	private String authority;
	
	
	public String getAuthority() {
		return authority;
	}
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	@Override
	public int hashCode() {
		return new Integer(id).hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		 if (obj == null) {
	            return false;
	        }
	        if (! (obj instanceof Authority)) {
	            return false;
	        }
	        return this.id == ((Authority)obj).getId();
	}


	
	

}
