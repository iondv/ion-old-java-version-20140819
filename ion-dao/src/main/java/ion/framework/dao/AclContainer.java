package ion.framework.dao;

import ion.core.DACPermission;
import ion.core.IUserContext;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

@Entity
public class AclContainer implements IAclContainer {
	
	private String owner;
	
	private String group;
	
	private Integer uperm;
	
	private Integer gperm;
	
	private Integer operm;
	
	public String getOwner(){
		return owner;
	}
	
	public void setOwner(String uid){
		owner = uid;
	}
	
	public String getGroup(){
		return group;
	}
	
	public void setGroup(String uid){
		group = uid;
	}
	
	public Integer getUperm(){
		return uperm;
	}
	
	public void setUperm(Integer p){
		uperm = p;
	}
	
	public Integer getGperm(){
		return gperm;
	}
	
	public void setGperm(Integer p){
		gperm = p;
	}

	public Integer getOperm(){
		return operm;
	}
	
	public void setOperm(Integer p){
		operm = p;
	}	
	
	public Set<DACPermission> InstancePermissions(IUserContext u) {
		Set<DACPermission> result = new HashSet<DACPermission>();
		for (DACPermission p : DACPermission.values())
			if (
				((p.getValue() & operm.intValue()) != 0)
				||
				((owner == u.getUid()) && ((p.getValue() & uperm.intValue()) != 0))
				||
				(u.IsCollaborator(group) && ((p.getValue() & gperm.intValue()) != 0))
			)
				result.add(p);		
		return result;
	}

}
