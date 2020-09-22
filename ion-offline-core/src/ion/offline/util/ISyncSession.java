package ion.offline.util;

import java.io.File;
import java.io.IOException;

import ion.core.IonException;
import ion.integration.core.UserCredentials;
import ion.offline.net.SyncRequestResult;

public interface ISyncSession {
	SyncRequestResult Init(String client, UserCredentials[] users, Integer syncHorizon) throws IonException;
	boolean AcceptVolume(String client, String token, File file, String hashSum, int total) throws IonException, IOException;
	boolean DownloadComplete(String client, String token) throws IonException, IOException;
}