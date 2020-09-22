package ion.offline.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.offline.filesystem.FileUtils;
import ion.offline.net.DataChange;
import ion.offline.net.DataUnit;
import ion.offline.net.SyncRequestResult;
import ion.offline.net.UserProfile;
import ion.offline.net.VolumeDownloadInfo;
import ion.offline.util.IVolumeProcessor;
import ion.offline.util.SyncState;
import ion.offline.util.client.SyncManager;

public class SimpleSyncManager extends SyncManager {

	private IVolumeProcessor volumeProcessor;
	
	private ISyncEnvironment environment;
	
	private ILogger logger;
	
	private class JsonFileFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".json");
		}
	}
	
	@Override
	public void CreateOutgoingPackage(String[] users) throws IonException {
		File volumesDir = getFile("out/volumes");
		if (volumesDir.exists())
			return;
		else
			volumesDir.mkdirs();
		
		Date since = null;
		Properties props = loadProps("out/.status");	
		if (props.containsKey("sync.prevDate")){
			since = new Date(Long.parseLong(props.getProperty("sync.prevDate")));
		}
		if (since == null) {
			Calendar c = Calendar.getInstance();
			c.set(2014, 0, 1);
			since = c.getTime();
		}
		
		Iterator<DataChange> i = null;
		try {
			i = environment.getChanges(since);
		} catch (Exception e) {
			logger.Error("Ошибка при выборке изменений!", e);
		}
		
		props.setProperty("sync.prevDate", String.valueOf(new Date().getTime()));
		
		Gson gs = new GsonBuilder().serializeNulls().create();

		int k = 0;
		if (i != null){
			while (i.hasNext()){
				DataChange r = i.next();
				saveOutgoingData(r, k, gs);
				k++;
			}
		}

		Iterator<DataUnit> ai = environment.getAuxData(since);
		while (ai.hasNext()) {
			DataUnit r = ai.next();
			saveOutgoingData(r, k, gs);
			k++;
		}
		
		if (k > 0){
			try {
				volumeProcessor.Split(getFile("out/changesets"), getFile("out/volumes"));
			} catch (JsonIOException | IOException e) {
				logger.Error("Ошибка генерации исходящего пакета!", e);
			}
		}
		
		saveProps("out/.status", props);
	}
	
	private void saveOutgoingData(DataUnit obj, int num, Gson gs) {
		String fileName = "out/changesets/";
		
		fileName = fileName + String.format("%06d", num);
		
		if (obj instanceof DataChange)
			fileName = fileName + ".change.json";
		else
			fileName = fileName + ".unit.json";
			
		File dst = getFile(fileName);
		try {
			OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(dst), "utf-8");
			gs.toJson(obj, w);
			w.close();
		} catch (Exception e) {
			logger.Error("Ошибка при формировании исходящего пакета!", e);
		}
	}
	
	private File getFile(String relativePath){
		File f = new File(environment.getSyncWorkingDirectory(), relativePath);
		if (!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		return f;
	}

	@Override
	public File[] OutgoingVolumes() throws IonException {
		File[] volumes = null;
		File volumesDir = getFile("out/volumes");
		if (volumesDir.exists()){				
			File statFile = getFile("out/.rest");
			BufferedReader r = null;
			if (statFile.exists()){
				try {
					r = new BufferedReader(new InputStreamReader(new FileInputStream(statFile),"utf-8"));
					String path = null;
					List<File> rest = new LinkedList<File>();
					while ((path = r.readLine()) != null){
						File file = new File(path);
						if (file.exists())
							rest.add(file);
					}
					if (!rest.isEmpty())
						volumes = rest.toArray(new File[rest.size()]);
					
				} catch (IOException e) {
					logger.Error("Ошибка чтения файла состояния отправки пакета!", e);
				} 
				try {
					r.close();
				} catch (IOException e) {
					logger.Error("Ошибка чтения файла состояния отправки пакета!", e);
				}
			}
			
			if (volumes == null)
				volumes = volumesDir.listFiles();
		}
		return volumes;
	}

	@Override
	public void AcceptVolume(File volume) throws IonException {
		File dest = getFile("in/volumes/"+volume.getName());
		try {
			Files.move(volume.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.Error("Не удалось переместить том пакета!",e);
		}
	}
	
	private String[] processDeletions(File deletions){
		String[] result = new String[0];
		if (deletions.exists()){
			try {
				LinkedList<String> l = new LinkedList<String>();
				BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(deletions),"utf-8"));
				String code;
				while ((code = r.readLine()) != null){
					l.add(code);
				}
				r.close();
				result = l.toArray(new String[l.size()]);
			} catch (IOException e) {
				logger.Error("Не удалось прочитать список на удаление!", e);
			}
		}
		return result;
	}
	
	private Properties loadProps(String fn){
		File statFile = getFile(fn);
		Properties props = new Properties();
		if (statFile.exists()){
			FileInputStream is = null;
			try {
				is = new FileInputStream(statFile);
				props.load(is);
			} catch (IOException e) {
				logger.Error("Не удалось загрузить файл настроек!", e);
			}
			try {
				is.close();
			} catch (IOException e) {
				logger.Error("Не удалось загрузить файл настроек!", e);
			}
		}
		return props;
	}
	
	private void saveProps(String fn, Properties props){
		try {
			props.store(new FileOutputStream(getFile(fn)), "");
		} catch (IOException e) {
			logger.Error("Не удалось сохранить настройки!", e);
		}
	}
	
	private void applyAcceptedData() throws JsonSyntaxException, JsonIOException, FileNotFoundException, UnsupportedEncodingException{
		File data = getFile("in/package/data");
		if (data.exists()){
			//IDataRepository dr = environment.getDataRepository();
			Gson gs = new GsonBuilder().serializeNulls().create();
			for (File unitFile: data.listFiles(new JsonFileFilter())){
				FileInputStream fis = new FileInputStream(unitFile);
				DataUnit unit = gs.fromJson(new InputStreamReader(fis,"utf-8"), DataUnit.class);
				try {
					fis.close();
				} catch (IOException e) {
				}
				if (unit != null)
					environment.acceptDataUnit(unit);
			}
		}
		
	}
	
	private void persistCredentials() throws IOException {
		File dest = new File(environment.getSyncWorkingDirectory(),".credentials");
		OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(dest),"utf-8");
		for (Map.Entry<String, String> pair: credentials.entrySet()){
			w.append(pair.getKey() + ":" + pair.getValue() + "\n");
		}
		w.flush();
		w.close();
	}
	
	private void loadCredentials() throws IOException{
		File src = new File(environment.getSyncWorkingDirectory(),".credentials");
		if (src.exists()){
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(src),"utf-8"));
			String pair = null;
			while ((pair = r.readLine()) != null){
				String[] v = pair.split(":");
				if (!credentials.containsKey(v[0]))
					credentials.put(v[0], v[1]);
			}
			r.close();
			src.delete();
		}
	}

	@Override
	public void ApplyIncomingPackage() throws IonException {
		File dvolumes = getFile("in/volumes");
		File dpack = getFile("in/package");
		dpack.mkdirs();
		try {
			volumeProcessor.Join(dvolumes.listFiles(), dpack);		
			File profiles = getFile("in/package/profiles");
			if (profiles.exists()){
				Gson gs = new GsonBuilder().serializeNulls().create();
				for (File unitFile: profiles.listFiles(new JsonFileFilter())){
					FileInputStream fis = new FileInputStream(unitFile);
					UserProfile profile = gs.fromJson(new InputStreamReader(fis,"utf-8"), UserProfile.class);
					fis.close();
					if (profile != null)
						environment.updateProfile(profile);
				}
			}

			File meta = getFile("in/package/model");
			
			if (meta.exists()){
				if (meta.exists())
					environment.adjustStorageMeta(meta, processDeletions(new File(meta,"deleted.list")));
				
				File nav = getFile("in/package/navigation");
				if (nav.exists())
					environment.adjustNavigationMeta(nav, processDeletions(new File(nav,"deleted.list")));
				
				File view = getFile("in/package/view");
				if (view.exists())
					environment.adjustViewMeta(view, processDeletions(new File(view,"deleted.list")));
			}
			
			File aux = getFile("in/package/misc");
			if (aux.exists()) 
				environment.adjustAuxData(aux);
			
			if (environment.needSyncStop()){
				Properties props = loadProps("in/.status");
				props.setProperty("state.notapplied", "true");
				saveProps("in/.status", props);
				persistCredentials();
				return;
			}
			
			applyAcceptedData();
		} catch (IOException e) {
			logger.Error("Не удалось применить входящий пакет!", e);
		}
	}

	@Override
	public void NoteAuthenticationReject(String login, String message)
			throws IonException {
		logger.Error("Пользователь "+login+" не был аутентифицирован во внешней системе: "+message);
	}

	@Override
	public void OutgoingPackageSent() {
		File f = getFile("out/changesets");
		if (f.exists())
			FileUtils.delete(f);
		
		f = getFile("out/.rest");
		if (f.exists())
			f.delete();
		f = getFile("out/volumes");
		if (f.exists())
			FileUtils.delete(f);
	}

	@Override
	public SyncState State() {
		File init = getFile(".init");
		if (init.exists())
			return SyncState.INCOMPLETE;		
		return SyncState.READY;
	}
	
	@Override
	public void StartSync() {
		try {
			loadCredentials();
		} catch (IOException e1) {
			logger.Warning("Не удалось загрузить сохраненные данные аутентификации.");
		}
		Properties props = loadProps("in/.status");
		if (props.getProperty("state.notapplied","false").equals("true")){
			logger.Info("Применяем изменения после перезагрузки приложения...");
			try {
				applyAcceptedData();
				logger.Info("Готово.");
				props.setProperty("state.notapplied", "false");
				saveProps("in/.status", props);
				cleanDirs();				
			} catch (JsonSyntaxException | JsonIOException
					| FileNotFoundException | UnsupportedEncodingException e) {
				logger.Error("Не удалось применить входящие изменения!", e);
			}
		}
		
		File volumesDir = getFile("in/volumes");
		volumesDir.mkdirs();
		environment.onSyncSessionStart();
	}

	@Override
	public void PersistInitInfo(SyncRequestResult info) {
		if (info != null){
			File init = getFile(".init");
			Gson gs = new GsonBuilder().serializeNulls().create();
			try {
				OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(init),"utf-8");
				gs.toJson(info, SyncRequestResult.class, w);
				w.flush();
				w.close();
			} catch (JsonIOException | IOException e) {
				logger.Error("Не удалось сохранить состояние инициализации!", e);
			}
		}
	}
	
	private void cleanDirs(){
		File f = getFile("out/changesets");
		if (f.exists())
			FileUtils.delete(f);
		
		f = getFile("out/.rest");
		if (f.exists())
			f.delete();
				
		f = getFile("out/volumes");
		if (f.exists())
			FileUtils.delete(f);
		
		f = getFile("in/package");
		if (f.exists())
			FileUtils.delete(f);
		
		f = getFile("in/volumes");
		if (f.exists())
			FileUtils.delete(f);		
	}

	@Override
	public SyncRequestResult restoreInitInfo() {
		SyncRequestResult result = null;
		File init = getFile(".init");
		if (init.exists()){
			Gson gs = new GsonBuilder().serializeNulls().create();
			InputStreamReader r = null;
			try {
				r = new InputStreamReader(new FileInputStream(init),"utf-8");
				result = gs.fromJson(r,SyncRequestResult.class);
				if (result != null){
					List<VolumeDownloadInfo> newDownloadList = new LinkedList<VolumeDownloadInfo>();
					if (result.volumes != null){
						for (VolumeDownloadInfo di: result.volumes){
							URL url = new URL(di.URL);
							File df = new File(url.getFile());
							df = getFile("in/volumes/"+df.getName());
							if (!df.exists())
								newDownloadList.add(di);
						}
						result.volumes = newDownloadList.toArray(new VolumeDownloadInfo[newDownloadList.size()]);
					}
				}
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				logger.Error("Не удалось восстановить состояние синхронизации!", e);
			} finally {
				try {
					r.close();
				} catch (IOException e) {
					logger.Error("Не удалось закрыть файл!", e);
				}
			}
		}
		if (result == null)
			cleanDirs();

		return result;
	}	

	@Override
	public void PersistIncompleteUpload(File[] rest) {
		File statFile = getFile("out/.rest");
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(statFile);
			for (File f: rest){
				stream.write((f.getAbsolutePath() + System.getProperty("line.separator")).getBytes());
			}
			stream.close();
		} catch (IOException e) {
			logger.Error("Не удалось сохранить состояние отправки пакета!", e);
		}
	}

	public IVolumeProcessor getVolumeProcessor() {
		return volumeProcessor;
	}

	public void setVolumeProcessor(IVolumeProcessor volumeProcessor) {
		this.volumeProcessor = volumeProcessor;
	}


	public ILogger getLogger() {
		return logger;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	public ISyncEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(ISyncEnvironment environment) {
		this.environment = environment;
		
	}
	
	@Override
	public boolean SyncComplete() {
		environment.onSyncSessionFinish();
		File init = getFile(".init");
		if (init.exists())
			init.delete();
		if (!environment.needSyncStop()){
			cleanDirs();
			environment.setSyncPrevDate(new Date());
		}
		return !environment.needSyncStop();
	}

	@Override
	public void SyncStopped() {	
		environment.onStop();
	}

	@Override
	public void onVolumeSent(int createdVolumes, int sentVolumes) {
		environment.setOutgoingPackagesQuantity(createdVolumes-sentVolumes);
	}
	
}
