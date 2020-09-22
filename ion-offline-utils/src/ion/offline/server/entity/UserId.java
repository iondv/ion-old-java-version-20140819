package ion.offline.server.entity;

import java.io.Serializable;

public class UserId implements Serializable {
	
	private static final long serialVersionUID = -4648626093171772183L;
	
	private String nickname = null;	

	private Point point = null;

	public UserId(){
		
	}
	
	public UserId(Point point, String nickname){
		this.point = point;
		this.nickname = nickname;
	}	
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserId){
			if (point != null && nickname != null)
				if (point.equals(((UserId)obj).point) && nickname.equals(((UserId)obj).nickname))
					return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (point != null && nickname != null)
			return point.getId().hashCode() + nickname.hashCode();
		return super.hashCode();
	}
}
