package ion.framework.offline.client;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.xml.soap.SOAPException;

import ion.core.IonException;
import ion.offline.data.ZipVolumeProcessor;
import ion.offline.security.HashProvider;
import ion.offline.security.SignatureProvider;
import ion.offline.sync.SimpleSyncManager;
import ion.offline.transport.ClientSyncSession;
import ion.offline.transport.UrlDownloadManager;
import ion.offline.util.client.IDaemonStateAware;
import ion.offline.util.client.Transport;

public class Worker {
	
	private Transport transport;
	
	private OfflineSyncEnvironment env;
	
	private int interval = 3600;
	
	public Worker(IDaemonStateAware dsw) throws IonException, SOAPException, MalformedURLException, ClassNotFoundException, SQLException, PropertyVetoException{
		Properties props = new Properties();
		try {
			File cwd = new File(".");
			for (File f: cwd.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".daemon.properties");
				}
			})){
				FileInputStream fis = new FileInputStream(f);
				props.load(fis);
				fis.close();
			}
		} catch (IOException e) {
			throw new IonException(e);
		}
		
		env = new OfflineSyncEnvironment(props);
		
		if (props.containsKey("iteration.interval"))
			interval = Integer.parseInt(props.getProperty("iteration.interval"));
    	
    	HashProvider hp = new HashProvider();  	
		
		transport = new Transport();
		transport.setLogger(env.getLogger());
		transport.setSyncInterval(interval);
		if (props.containsKey("sync.checkConsistency"))
			transport.setCheckConsistency(Boolean.parseBoolean(props.getProperty("sync.checkConsistency")));
		if (props.containsKey("sync.horizon"))
			transport.setSyncHorizon(Integer.parseInt(props.getProperty("sync.horizon")));
		if (props.containsKey("sync.timeouts.downloadComplete"))
			transport.setDownloadCompletionTimeout(Integer.parseInt(props.getProperty("sync.timeouts.downloadComplete")));
		if (props.containsKey("sync.timeouts.download"))
			transport.setDownloadTimeout(Integer.parseInt(props.getProperty("sync.timeouts.download")));
		if (props.containsKey("sync.timeouts.upload"))
			transport.setUploadTimeout(Integer.parseInt(props.getProperty("sync.timeouts.upload")));
		if (props.containsKey("sync.timeouts.volumeDownload"))
			transport.setVolumeDownloadTimeout(Integer.parseInt(props.getProperty("sync.timeouts.volumeDownload")));
		if (props.containsKey("sync.timeouts.volumeUpload"))
			transport.setVolumeUploadTimeout(Integer.parseInt(props.getProperty("sync.timeouts.volumeUpload")));
		
		ClientSyncSession sess = new ClientSyncSession();
		sess.setAdapterUrl(props.getProperty("sync.adapterUrl"));
		sess.setPrivateKey(props.getProperty("sync.privateKey"));
		sess.setTransportModuleId(props.getProperty("sync.clientId"));
		
		sess.setHashProvider(hp);
		sess.setSignatureProvider(new SignatureProvider());
		sess.setLogger(env.getLogger());
		sess.setDownloadManager(new UrlDownloadManager());
		sess.setEnvironment(env);
		
		transport.setSession(sess);
		transport.setDaemonStateWatcher(dsw);
				
		SimpleSyncManager syncManager = new SimpleSyncManager();
		syncManager.setEnvironment(env);
		syncManager.setLogger(env.getLogger());
		syncManager.setVolumeProcessor(new ZipVolumeProcessor(10240));
		transport.setSyncManager(syncManager);
	}
	
	public long getInterval(){
		return interval;
	}

	public void run(){
		try {
			for (Map.Entry<String, String> pair: env.getCredentials().entrySet())
				transport.getSyncManager().AddCredentials(pair.getKey(), pair.getValue());
			transport.iteration();
		} catch (Exception e) {
			System.err.println("Ошибка при формировании пакетов!");
			e.printStackTrace(System.err);
		}
	}
		
	public void stop(){
		transport.stop();
	}
}
