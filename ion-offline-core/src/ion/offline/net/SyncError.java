package ion.offline.net;

public class SyncError {
	public String user;
	
	public String message;
	
	public String objectId;
	
	public SyncError(String user, String object, String message){
		this.user = user;
		this.objectId = object;
		this.message = message;
	}
}
