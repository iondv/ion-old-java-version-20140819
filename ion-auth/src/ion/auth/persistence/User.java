package ion.auth.persistence;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table (name="users")
public class User {

	@Id @GeneratedValue
	private Integer id;
	private Integer version;
	private Boolean account_expired;
	private Boolean account_locked;
	private Boolean enabled;
	private String password;
	private Boolean password_expired;
	private String username;
	
	@OneToMany(fetch=FetchType.EAGER)
	@JoinTable(name="user_authority",
			joinColumns=@JoinColumn(name="user_id"),
			inverseJoinColumns=@JoinColumn(name="authority_id")
			)
	private Set<Authority> listOfAuthorities = new LinkedHashSet<Authority>();

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="user")
	private Set<UserProperty> properties = new LinkedHashSet<UserProperty>();
	
	public Set<Authority> getListOfAuthorities() {
		return listOfAuthorities;
	}
	
	public void setListOfAuthorities(Set<Authority> listOfAuthorities) {
		this.listOfAuthorities = listOfAuthorities;
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
	public Boolean getAccount_expired() {
		return account_expired;
	}
	public void setAccount_expired(Boolean account_expired) {
		this.account_expired = account_expired;
	}
	public Boolean getAccount_locked() {
		return account_locked;
	}
	public void setAccount_locked(Boolean account_locked) {
		this.account_locked = account_locked;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Boolean getPassword_expired() {
		return password_expired;
	}
	public void setPassword_expired(Boolean password_expired) {
		this.password_expired = password_expired;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public Set<UserProperty> getProperties() {
		return properties;
	}

	public void setProperties(Set<UserProperty> properties) {
		this.properties = properties;
	}	
	
}
