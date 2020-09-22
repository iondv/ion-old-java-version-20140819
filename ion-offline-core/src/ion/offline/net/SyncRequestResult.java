package ion.offline.net;

public class SyncRequestResult {
	public String uploadToken;
	
	public ClientAuthResult[] auth;
	
	public VolumeDownloadInfo[] volumes;
	
	public SyncRequestResult(String token, ClientAuthResult[] auth, VolumeDownloadInfo[] volumes){
		uploadToken = token;
		this.auth = auth;
		this.volumes = volumes;
	}
}
