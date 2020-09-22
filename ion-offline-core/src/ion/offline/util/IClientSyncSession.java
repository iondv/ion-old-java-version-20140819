package ion.offline.util;

import java.io.File;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import ion.core.IonException;
import ion.integration.core.UserCredentials;
import ion.offline.net.SyncRequestResult;

public interface IClientSyncSession {
	SyncRequestResult Init(UserCredentials[] credentials, Integer syncHorizon) throws IonException;
	File DownloadVolume(String url, String hashSum, int timeout) throws IonException, SocketTimeoutException, SocketException;
	int UploadVolume(String token, File volume, int total, int timeout) throws IonException, SocketTimeoutException, SocketException;
	int DownloadComplete(String token, int timeout) throws IonException, SocketTimeoutException, SocketException;
}