//package ion.offline.mocks;
//
//import java.io.File;
//
//import ion.core.IDataRepository;
//import ion.core.IMetaRepository;
//import ion.core.IonException;
//import ion.core.logging.IChangeLogger;
//import ion.offline.net.DataUnit;
//import ion.offline.net.UserProfile;
//import ion.offline.sync.ISyncEnvironment;
//
//public class SyncEnvironmentMock implements ISyncEnvironment {
//
//	private IDataRepository dataRepository;
//	private IMetaRepository metaRepository;
//	private IChangeLogger changeLogger;
//	private File syncWorkingDirectory;
//	private File domainModelDirectory;
//	private File navigationModelDirectory;
//	private File downloadDirectory;
//	
//	public SyncEnvironmentMock(IDataRepository dataRepository,
//			IMetaRepository metaRepository, IChangeLogger changeLogger,
//			File syncWorkingDirectory, File domainModelDirectory,
//			File navigationModelDirectory, File downloadDirectory) {
//		super();
//		this.dataRepository = dataRepository;
//		this.metaRepository = metaRepository;
//		this.changeLogger = changeLogger;
//		this.syncWorkingDirectory = syncWorkingDirectory;
//		this.domainModelDirectory = domainModelDirectory;
//		this.navigationModelDirectory = navigationModelDirectory;
//		this.downloadDirectory = downloadDirectory;
//	}
//	
//	@Override
//	public Object begin() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void commit(Object info) {}
//
//	@Override
//	public void rollback(Object info) {}
//
//	@Override
//	public IDataRepository getDataRepository() {return dataRepository;}	
//
//	@Override
//	public IMetaRepository getMetaRepository() {return metaRepository;}
//
//	@Override
//	public IChangeLogger getChangeLogger() {return changeLogger;}
//
//	@Override
//	public File getSyncWorkingDirectory() {return syncWorkingDirectory;}
//
//	@Override
//	public File getDomainModelDirectory() {return domainModelDirectory;}
//
//	@Override
//	public File getNavigationModelDirectory() {return navigationModelDirectory;}
//
//	@Override
//	public File getDownloadDirectory() {return downloadDirectory;}
//
//	@Override
//	public void adjustStorageMeta(File src, String[] deletions)	throws IonException {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void adjustNavigationMeta(File src, String[] deletions) throws IonException {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void adjustViewMeta(File src, String[] deletions) throws IonException {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void onSyncSessionStart() {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void onSyncSessionFinish() {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void updateProfile(UserProfile u) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void acceptDataUnit(DataUnit unit) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public boolean needSyncStop() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//}
