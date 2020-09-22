package ion.offline.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import ion.core.IonException;
import ion.framework.meta.plain.StoredClassMeta;
import ion.offline.net.DataDelta;
import ion.offline.net.DataUnit;
import ion.offline.net.MetaDelta;
import ion.offline.net.NavigationDelta;
import ion.offline.net.SyncError;
import ion.offline.net.UserProfile;
import ion.offline.net.ViewDelta;
import ion.offline.net.VolumeDownloadInfo;
import ion.viewmodel.plain.StoredNavNode;

public interface IPackageQueue { 
	void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, NavigationDelta nav, ViewDelta view, Map<String, Iterator<DataUnit>> aux, SyncError[] errors) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, NavigationDelta nav, ViewDelta view, Map<String, Iterator<DataUnit>> aux) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, NavigationDelta nav, ViewDelta view, SyncError[] errors) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, NavigationDelta nav, ViewDelta view) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, NavigationDelta nav) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, SyncError[] errors) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles, MetaDelta model) throws IonException, IOException;
	void Queue(String client, UserProfile[] profiles) throws IonException, IOException;
	void Queue(String client, StoredNavNode[] navnode) throws IonException, IOException;
	void Queue(String client, StoredClassMeta[] model) throws IonException, IOException;
	void Queue(String client, Iterator<DataUnit> data) throws IonException, IOException;
	void Queue(String client, UserProfile profile) throws IonException, IOException;
	void Queue(String client, StoredClassMeta meta) throws IonException, IOException;
	void Queue(String client, DataUnit data) throws IonException, IOException;
	void Queue(String client, SyncError error) throws IonException, IOException;
	void Pack(String client) throws IonException, IOException;
	VolumeDownloadInfo[] PendingPackage(String client) throws IonException;
	File[] PendingPackageVolumes(String client) throws IonException;
	void BeginDequeue(String client) throws IonException;
	void CommitDequeue(String client) throws IonException;
}
