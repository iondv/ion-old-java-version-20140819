package ion.framework.changelog.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class ChangeLogId implements Serializable {
	private static final long serialVersionUID = -4648626093171772183L;
		
	public String type;
		
	public String actor;
		
	public Date time;
		
	public String objectClass;
		
	public String objectId;	
	
	@Override
	public int hashCode() {
		if (type != null && actor != null && time != null && objectClass != null && objectId != null)
			return type.hashCode() + actor.hashCode() + time.hashCode() + objectClass.hashCode() + objectId.hashCode();
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ChangeLogId){
			if (type == null || !type.equals(((ChangeLogId) obj).type))
				return false;
			if (actor == null || !actor.equals(((ChangeLogId) obj).actor))
				return false;
			if (time == null || !time.equals(((ChangeLogId) obj).time))
				return false;
			if (objectClass == null || !objectClass.equals(((ChangeLogId) obj).objectClass))
				return false;
			if (objectId == null || !objectId.equals(((ChangeLogId) obj).objectId))
				return false;
			return true;
		}
		return false;
	}
}
