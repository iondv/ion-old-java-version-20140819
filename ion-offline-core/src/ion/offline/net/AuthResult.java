package ion.offline.net;

public class AuthResult extends ClientAuthResult {	
	public String token;
	
	public AuthResult(String login, boolean result, String msg, String token){
		super(login,result,msg);
		this.token = token;
	}
	
	public AuthResult(String login, boolean result, String token){
		this(login,result,"",token);
	}	
}
