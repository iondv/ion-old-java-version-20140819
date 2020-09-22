package ion.framework.acl.entity;

import java.io.Serializable;

public class AccessRecordId  implements Serializable {
	private static final long serialVersionUID = 1L;

	private String ouid = null;

	private String actor = null;	
	
	public AccessRecordId(){
		
	}

	public AccessRecordId(String ouid, String actor){
		this.ouid = ouid;
		
		this.actor = actor;
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
	
	@Override
	public int hashCode() {
		if (actor != null && ouid != null)
			return actor.hashCode() + ouid.hashCode();
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccessRecordId){
			if (actor != null && ouid != null)
				if (actor.equals(((AccessRecordId)obj).actor) && ouid.equals(((AccessRecordId)obj).ouid))
					return true;
		}
			
		return false;
	}
}