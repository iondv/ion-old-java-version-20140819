package ion.offline.net;

public class VolumeDownloadInfo {
	public String URL;
	
	public String hashSum;
	
	public VolumeDownloadInfo(String url, String hash){
		URL = url;
		hashSum = hash;
	}
}
