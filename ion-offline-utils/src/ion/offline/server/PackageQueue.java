package ion.offline.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.framework.meta.plain.StoredClassMeta;
import ion.offline.filesystem.FileUtils;
import ion.offline.net.DataDelta;
import ion.offline.net.DataUnit;
import ion.offline.net.MetaDelta;
import ion.offline.net.NavigationDelta;
import ion.offline.net.SyncError;
import ion.offline.net.UserProfile;
import ion.offline.net.ViewDelta;
import ion.offline.net.VolumeDownloadInfo;
import ion.offline.server.dao.IDataPackageDAO;
import ion.offline.server.entity.DataPackage;
import ion.offline.server.entity.Point;
import ion.offline.util.IHashProvider;
import ion.offline.util.IPackageQueue;
import ion.offline.util.IVolumeProcessor;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredViewModel;

public class PackageQueue implements IPackageQueue {
	
	private IVolumeProcessor volumeProcessor;
	
	private IDataPackageDAO dataPackageDAO;
	
	private File workDirectory;
	
	private File outgoingDirectory;
	
	private String urlBase;
	
	private IHashProvider hashProvider;
	
	private ILogger logger;
	
	private Gson gs = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		
	
	private File dataPackageDir(DataPackage p){
		if(p != null){
			File data_package_dir = new File(p.getDirectory());
			if(data_package_dir.exists() && data_package_dir.isDirectory()){
				return data_package_dir;
			} else {
				logger.Error("Для пакета № "+ p.getId() +" предназначенного клиенту [" + p.getPoint().getId() + "] неправильно указана директория!");
			}
		}
		return null;
	}
	
	private DataPackage getFirstPackage(String client){
		return dataPackageDAO.GetFirstPackage(Integer.parseInt(client));
	}
	
	public VolumeDownloadInfo[] PendingPackage(String client) throws IonException {
		File data_package_dir;
		DataPackage pckg = getFirstPackage(client);
		while (pckg != null) {
			data_package_dir = dataPackageDir(pckg);
			if (data_package_dir != null){
  			BufferedReader br = null;
  			try {
  				br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(data_package_dir, ".MANIFEST")),"utf-8"));
  				String line;
  				List<VolumeDownloadInfo> ret = new ArrayList<VolumeDownloadInfo>();
  				while((line = br.readLine()) != null){
  					String[] splitline = line.split(":");
  					ret.add(new VolumeDownloadInfo(urlBase +"/"+ splitline[0], splitline[1]));
  				}
  				br.close();
  				return ret.toArray(new VolumeDownloadInfo[ret.size()]);
  			} catch (IOException e) {
  				logger.Error("Ошибка при получении списка томов пакета!", e);
  				return null;
  			} finally {
  				try {
  					br.close();
  				} catch (IOException e) {
  					logger.Error("Ошибка при получении списка томов пакета!", e);
  					return null;
  				}
  			}
			}
			
			dataPackageDAO.DeletePackage(pckg);
			pckg = getFirstPackage(client);	
		}
		return null;
	}

	public File[] PendingPackageVolumes(String client) throws IonException {
		File data_package_dir;
		if ((data_package_dir = dataPackageDir(getFirstPackage(client))) != null){
			return data_package_dir.listFiles();
		}
		return null;
	}
	
	private DataPackage getLastPackage(String client){
		return dataPackageDAO.GetLastPackage(Integer.parseInt(client));
	}
	
	public void Pack(String client) throws IonException, IOException {
		DataPackage data_package = getLastPackage(client);
		if (data_package != null && !data_package.getIsPacked()){
			File src = new File(data_package.getDirectory());
			File dest = new File(outgoingDirectory, data_package.getId().toString());
			dest.mkdirs();
			File[] volumes = volumeProcessor.Split(src, dest);
			
			OutputStreamWriter w = null;
			try {
				w = new OutputStreamWriter(new FileOutputStream(new File(dest,".MANIFEST")),"utf-8");
				for(File volume: volumes){				
					String hash = hashProvider.hash(volume);
					w.write(data_package.getId().toString()+"/"+volume.getName() + ":" + hash + System.getProperty("line.separator"));
				}
			} finally {
				w.close();
			}
			data_package.setDirectory(dest.getAbsolutePath());
			data_package.setIsPacked(true);
			dataPackageDAO.updateDataPackage(data_package);
			FileUtils.delete(src);
		}
	}
	
	private void stringsToFile(String[] strings, File f) throws IOException {
		OutputStreamWriter w = null;
		try {
			w = new OutputStreamWriter(new FileOutputStream(f),"utf-8");
			for (String s: strings)
				w.write(s + System.getProperty("line.separator"));
		} finally {
			w.close();
		}
	}
	
	private void saveObject(Object o, File dest) throws JsonIOException, IOException{
		OutputStreamWriter w = null;
		try {
			w = new OutputStreamWriter(new FileOutputStream(dest),"utf-8");
			gs.toJson(o, o.getClass(),w);
			w.flush();
		} finally {
			w.close();
		}
	}
		
	@Override
	public void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, NavigationDelta nav, ViewDelta view, Map<String, Iterator<DataUnit>> aux, SyncError[] errors) throws IonException, IOException {
		int client_id = Integer.parseInt(client);
		DataPackage data_package = getLastPackage(client);
		if(data_package == null || data_package.getIsPacked()){
			Point point = new Point();
			point.setId(client_id);
			data_package = dataPackageDAO.CreateNewPackage(point, workDirectory);
		}
		
		File dataPackageDirectory = new File(data_package.getDirectory());
		dataPackageDirectory.mkdirs();
		
		if(profiles != null && profiles.length > 0){
			File profilesDirectory = new File(dataPackageDirectory, "profiles");
			if(!profilesDirectory.exists())
				profilesDirectory.mkdirs();
			for(UserProfile profile:profiles)
				saveObject(profile, new File(profilesDirectory, profile.login+".json"));
		}
		
		if(model != null){
			File modelDirectory = new File(dataPackageDirectory, "model");
			if(!modelDirectory.exists())
				modelDirectory.mkdirs();
			stringsToFile(model.removed, new File(modelDirectory,"deleted.list"));
			for(StoredClassMeta change:model.changes)
				saveObject(change, new File(modelDirectory, change.name+".class.json"));
		}
		
		if(data != null){
			File dataDirectory = new File(dataPackageDirectory, "data");
			if(!dataDirectory.exists())
				dataDirectory.mkdirs();
			stringsToFile(data.deleted, new File(dataDirectory,"deleted.list"));
			while (data.changes.hasNext()) {
				DataUnit change = data.changes.next();
				if(change.id != null) {
					saveObject(change, new File(dataDirectory, change.className+"@"+URLEncoder.encode(change.id,"UTF-8")+".json"));
				}				
			}
		}
		
		if(nav != null){
			File navDirectory = new File(dataPackageDirectory, "navigation");
			if(!navDirectory.exists())
				navDirectory.mkdirs();
			stringsToFile(nav.unavailable, new File(navDirectory,"deleted.list"));
			
			for(StoredNavNode change:nav.changes){
				File dest = new File(navDirectory, change.code+".json");
				dest.getParentFile().mkdirs();
				saveObject(change, dest);
			}
		}
		
		if (view != null){
			File viewDirectory = new File(dataPackageDirectory, "view");
			if(!viewDirectory.exists())
				viewDirectory.mkdirs();
			stringsToFile(view.deprecated, new File(viewDirectory,"deleted.list"));
			
			for(Map.Entry<String, StoredViewModel> change:view.models.entrySet()){
				File dest = new File(viewDirectory, change.getKey().replace("/", File.separator)+".json");
				dest.getParentFile().mkdirs();
				saveObject(change.getValue(), dest);
			}
		}
		
		if (aux != null){
			File auxDirectory = new File(dataPackageDirectory, "misc");
			if (!auxDirectory.exists())
				auxDirectory.mkdirs();
			for (Map.Entry<String, Iterator<DataUnit>> auxChapter: aux.entrySet()){
				File chapterDir = new File(auxDirectory, auxChapter.getKey());
				if(!chapterDir.exists())
					chapterDir.mkdirs();
				
				while (auxChapter.getValue().hasNext()){
						DataUnit auxData = auxChapter.getValue().next();
						if (auxData != null)
							saveObject(auxData, new File(chapterDir, auxData.className+"."+auxData.id));				
				}
			}
		}
	}

	@Override
	public void Queue(String client, UserProfile[] profiles, MetaDelta model,
			DataDelta data, NavigationDelta nav, ViewDelta view, Map<String, Iterator<DataUnit>> aux)
			throws IonException, IOException {
		this.Queue(client, profiles, model, data, nav, view, aux, null);
	}
	
	@Override
	public void Queue(String client, UserProfile[] profiles, MetaDelta model,
										DataDelta data, NavigationDelta nav, ViewDelta view,
										SyncError[] errors) throws IonException, IOException {
		this.Queue(client, profiles, model, data, nav, view, null, errors);		
	}	
	
	@Override
	public void Queue(String client, UserProfile[] profiles, MetaDelta model,
			DataDelta data, NavigationDelta nav, ViewDelta view)
			throws IonException, IOException {
		this.Queue(client, profiles, model, data, nav, view, null, null);
	}	
	
	@Override
	public void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, NavigationDelta nav) throws IonException, IOException {
		this.Queue(client, profiles, model, data, nav);
	}
	
	public void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data, SyncError[] errors) throws IonException, IOException {
		this.Queue(client, profiles, model, data, null, null, errors);
	}
	
	public void Queue(String client, UserProfile[] profiles, MetaDelta model, DataDelta data) throws IonException, IOException {
		this.Queue(client, profiles, model, data, null, null);
	}
	
	public void Queue(String client, UserProfile[] profiles, MetaDelta model) throws IonException, IOException {
		this.Queue(client, profiles, model, null, null, null);
	}
	
	public void Queue(String client, UserProfile[] profiles) throws IonException, IOException {
		this.Queue(client, profiles, null, null, null, null);
	}
	
	public void Queue(String client, StoredNavNode[] navnode) throws IonException, IOException {
		this.Queue(client, null, null, null, new NavigationDelta(navnode), null);
	}
	
	public void Queue(String client, StoredClassMeta[] model) throws IonException, IOException {
		this.Queue(client, null, new MetaDelta(model), null, null, null);
	}
	
	public void Queue(String client, Iterator<DataUnit> data) throws IonException, IOException {
		this.Queue(client, null, null, new DataDelta(data), null, null);
	}
	public void Queue(String client, UserProfile profile) throws IonException, IOException {
		this.Queue(client, new UserProfile[]{profile}, null, null, null, null);
	}
	
	public void Queue(String client, StoredClassMeta meta) throws IonException, IOException {
		this.Queue(client, null, new MetaDelta(new StoredClassMeta[]{meta}), null, null, null);
	}
	
	public void Queue(String client, DataUnit data) throws IonException, IOException {
		List<DataUnit> l = new LinkedList<DataUnit>();
		l.add(data);
		this.Queue(client, null, null, new DataDelta(l.iterator()), null, null);
	}
	
	public void Queue(String client, SyncError error) throws IonException, IOException {
		this.Queue(client, null, null, null, null, null, new SyncError[]{error});
	}

	public IHashProvider getHashProvider() {
		return hashProvider;
	}

	public void setHashProvider(IHashProvider hashProvider) {
		this.hashProvider = hashProvider;
	}

	public ILogger getLogger() {
		return logger;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public IVolumeProcessor getVolumeProcessor() {
		return volumeProcessor;
	}

	public void setVolumeProcessor(IVolumeProcessor volumeProcessor) {
		this.volumeProcessor = volumeProcessor;
	}

	public IDataPackageDAO getDataPackageDAO() {
		return dataPackageDAO;
	}

	public void setDataPackageDAO(IDataPackageDAO dataPackageDAO) {
		this.dataPackageDAO = dataPackageDAO;
	}

	public File getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(File workDirectory) {
		this.workDirectory = workDirectory;
		this.workDirectory.mkdirs();
	}

	public File getOutgoingDirectory() {
		return outgoingDirectory;
	}

	public void setOutgoingDirectory(File outgoingDirectory) {
		this.outgoingDirectory = outgoingDirectory;
		this.outgoingDirectory.mkdirs();
	}

	@Override
	public void BeginDequeue(String client) throws IonException {
		/*
		DataPackage data_package = dataPackageDAO.GetFirstPackage(Integer.parseInt(client));
		if (data_package != null){
			data_package.setIsBusy(true);
			dataPackageDAO.updateDataPackage(data_package);
		}
		*/
	}

	@Override
	public void CommitDequeue(String client) throws IonException {
		DataPackage data_package = dataPackageDAO.GetFirstPackage(Integer.parseInt(client));		
		if(data_package != null){
			File dir = new File(data_package.getDirectory());
			dataPackageDAO.DeletePackage(data_package);
			if (dir.exists())
				FileUtils.delete(dir);
		}
	}
}
