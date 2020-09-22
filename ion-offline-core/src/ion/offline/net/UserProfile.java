package ion.offline.net;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {
	public String login;
	
	public String name;
	
	public String lastname;
	
	public String patronymic;
	
	public Map<String, Object> properties;
	
	public Map<String, Integer> access;
	
	public UserProfile(String login, String name, String lastname, String patronymic, Map<String, Object> properties, Map<String, Integer> access){
		this.login = login;
		this.name = name;
		this.lastname = lastname;
		this.patronymic = patronymic;
		
		this.properties = properties;
		this.access = access;
	}
	
	public UserProfile(String login, String name, String lastname, String patronymic, Map<String, Object> properties){
		this(login,name,lastname,patronymic,properties,new HashMap<String, Integer>());
	}
		
	public UserProfile(String login, String name, String lastname, String patronymic){
		this(login,name,lastname,patronymic,new HashMap<String, Object>());
	}
	
	public UserProfile(String login){
		this(login,"","","");
	}	
}
