package ion.offline.sync;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import ion.core.IonException;
import ion.offline.net.DataChange;
import ion.offline.net.DataUnit;
import ion.offline.net.UserProfile;

public interface ISyncEnvironment {
	
	File getSyncWorkingDirectory();
	
	File getDomainModelDirectory();
	
	File getNavigationModelDirectory();
	
	File getDownloadDirectory();
	
	Iterator<DataChange> getChanges(Date since) throws IonException;
	
	Iterator<DataUnit> getAuxData(Date since) throws IonException;
			
	void adjustStorageMeta(File src, String[] deletions) throws IonException;
	
	void adjustNavigationMeta(File src, String[] deletions) throws IonException;
	
	void adjustViewMeta(File src, String[] deletions) throws IonException;	
	
	void adjustAuxData(File src) throws IonException;

	void onSyncSessionStart();
		
	void onSyncSessionFinish();
	
	void onStop();
	
	void updateProfile(UserProfile u);
	
	void acceptDataUnit(DataUnit unit);
	
	boolean needSyncStop();
	
	void setSyncPrevDate(Date prevDate);
	
	void setOutgoingPackagesQuantity(Integer quantity);
}
