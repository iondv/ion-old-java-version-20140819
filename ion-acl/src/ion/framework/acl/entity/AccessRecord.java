package ion.framework.acl.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table (name="acl")
@IdClass(AccessRecordId.class)
public class AccessRecord {
	@Id
	private String ouid;
	
	@Id
	private String actor;
	
	private Integer permissions;
	
	public AccessRecord(){
		
	}
	
	public AccessRecord(String ouid, String actor, Integer p){
		this.ouid = ouid;
		this.setActor(actor);
		setPermissions(p);
	}
	
	public String getOuid() {
		return ouid;
	}

	public void setOuid(String ouid) {
		this.ouid = ouid;
	}	

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public Integer getPermissions() {
		return permissions;
	}

	public void setPermissions(Integer permissions) {
		this.permissions = permissions;
	}	
}