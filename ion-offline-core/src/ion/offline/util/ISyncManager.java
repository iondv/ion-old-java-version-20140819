package ion.offline.util;

import java.io.File;

import ion.core.IonException;
import ion.integration.core.UserCredentials;
import ion.offline.net.SyncRequestResult;

public interface ISyncManager {
	void StartSync();
	
	boolean SyncComplete();
	
	void SyncStopped();
	
	void PersistInitInfo(SyncRequestResult info);
	
	SyncRequestResult restoreInitInfo();
	
	void CreateOutgoingPackage(String[] users) throws IonException;
	
	File[] OutgoingVolumes() throws IonException;
	
	void AcceptVolume(File volume) throws IonException;
	
	void ApplyIncomingPackage() throws IonException;
	
	public void AddCredentials(String login, String password);
	
	public void RemoveCredentials(String login);
	
	public UserCredentials[] GetCredentials();
	
	void NoteAuthenticationReject(String login, String message) throws IonException;
	
	void OutgoingPackageSent();
	
	SyncState State();
		
	void PersistIncompleteUpload(File[] rest);
	
	void onVolumeSent(int createdVolumes, int sentVolumes);
}
