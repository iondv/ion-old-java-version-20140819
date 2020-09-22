package ion.offline.server.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User {
	@Id
	private String nickname = null;
	
	@ManyToOne
	@JoinColumn(name = "point")
	private Point point = null;
	
	private String token = null;
	
	public User(){
		
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
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public User(String nickname, Point point, String token) {
		super();
		this.nickname = nickname;
		this.point = point;
		this.token = token;
	}	
}
