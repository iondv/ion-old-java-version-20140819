package ion.offline.util.client;

import java.io.File;

import ion.core.IonException;
import ion.integration.core.UserCredentials;
import ion.offline.net.ClientAuthResult;
import ion.offline.net.SyncRequestResult;
import ion.offline.net.VolumeDownloadInfo;
import ion.offline.util.IClientSyncSession;

public class ClientSyncSessionMock implements IClientSyncSession {
	
	private String volumePath;
	
	public ClientSyncSessionMock(String volume){
		volumePath = volume;
	}

	@Override
	public SyncRequestResult Init(UserCredentials[] credentials, Integer syncHorizon)
			throws IonException {
		
		ClientAuthResult[] auth = new ClientAuthResult[credentials.length];
		int i = 0;
		for (UserCredentials u: credentials){
			auth[i] = new ClientAuthResult(u.login, true);
			i++;
		}
		
		VolumeDownloadInfo[] volumes = new VolumeDownloadInfo[1];
		volumes[0] = new VolumeDownloadInfo("http://www.fake.url", "hashsum");
		return new SyncRequestResult("aaa", auth, volumes);
	}

	@Override
	public File DownloadVolume(String URL, String hashSum, int timeout)
			throws IonException {
		return new File(volumePath);
	}

	@Override
	public int UploadVolume(String token, File volume, int total,
			int timeout) throws IonException {
		return 200;
	}

	@Override
	public int DownloadComplete(String token, int timeout) throws IonException {
		return 200;
	}

}
