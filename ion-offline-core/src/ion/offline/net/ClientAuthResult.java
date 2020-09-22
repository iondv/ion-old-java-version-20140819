package ion.offline.net;

public class ClientAuthResult {
	public String login;
	
	public boolean result;
	
	public String resultMessage = "";
	
	public ClientAuthResult(String login, boolean result, String msg){
		this.login = login;
		this.result = result;
		this.resultMessage = msg;
	}
	
	public ClientAuthResult(String login, boolean result){
		this(login,result,"");
	}
}
