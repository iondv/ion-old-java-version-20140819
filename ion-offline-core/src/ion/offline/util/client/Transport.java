package ion.offline.util.client;

import java.io.File;
import java.lang.Thread.State;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.integration.core.UserCredentials;
import ion.offline.net.ClientAuthResult;
import ion.offline.net.SyncRequestResult;
import ion.offline.net.VolumeDownloadInfo;
import ion.offline.util.IClientSyncSession;
import ion.offline.util.ISyncManager;
import ion.offline.util.SyncState;

public class Transport implements Runnable {
	
	private int downloadTimeout = 24;
	
	private int uploadTimeout = 24;
	
	private int volumeDownloadTimeout = 5;
	
	private int volumeUploadTimeout = 5;
	
	private int downloadCompletionTimeout = 30;
	
	private int syncInterval = 300;

	private boolean checkConsistency = true;
	
	private Integer syncHorizon = null;

	private volatile Thread init = null;
	
	private ISyncManager syncManager;
	
	private IClientSyncSession session;
	
	private ILogger logger;
	
	private boolean parallel = false;
	
	private IDaemonStateAware daemonStateWatcher;

	public IClientSyncSession getSession() {
		return session;
	}

	public void setSession(IClientSyncSession session) {
		this.session = session;
	}

	public void start(boolean parallel){
		this.parallel = parallel;
		if (init == null){
			if (this.parallel){
				init = new Thread(this);
				init.setDaemon(false);
				init.start();
			} else
				init = Thread.currentThread();
		}
	}
	
	public void start(){
		start(true);
	}
	
	public void stop(){
		syncManager.SyncStopped();
		
		Thread thread = init;
		init = null;
		
		if (parallel){
			Object waiter = this;
			synchronized (waiter) {
				while (thread.getState() == State.TIMED_WAITING || thread.getState() == State.RUNNABLE);
				try {
					this.wait(5000);
				} catch (InterruptedException e) {
					logger.Error("Ошибка потока.", e);
				}			
			}
		}
	}
	
	public void iteration(){
		SyncRequestResult init_result = null;
		if (syncManager.State() == SyncState.INCOMPLETE){
			init_result = syncManager.restoreInitInfo();
		}
		
		if (init_result == null){
			syncManager.StartSync();
			try {
				UserCredentials[] credentials = syncManager.GetCredentials();
				if (credentials != null && credentials.length > 0)
					init_result = session.Init(credentials,syncHorizon);
				if (init_result != null){
					logger.Info("На запрос сессии получен ответ.");
					syncManager.PersistInitInfo(init_result);
				} else 
					logger.Info("На запрос сессии не получен ответ, либо запрос не был отправлен.");
			} catch (IonException e){
				logger.Error("Ошибка инициализации сессии синхронизации!", e);
			}
		}
		
		if (init_result != null){
			try {
				final String uploadToken = init_result.uploadToken;
				
				if (init_result.auth != null && init_result.auth.length > 0){
					
					List<String> users = new ArrayList<String>();
					for (ClientAuthResult auth: init_result.auth){
						if (auth.result){
							users.add(auth.login);
						} else {
							try {
								syncManager.NoteAuthenticationReject(auth.login, auth.resultMessage);
							} catch (IonException e) {
								logger.Error("Ошибка фиксации отказа в аутентификации!", e);
							}
						}
					}
					
					
					if (users.size() > 0){
						logger.Info("Отправка...");
						File[] volumes = null;
						try {
							syncManager.CreateOutgoingPackage(users.toArray(new String[users.size()]));
							volumes = syncManager.OutgoingVolumes();
						} catch (IonException e){
							logger.Error("Ошибка формирования исходящего пакета!", e);
						}
						
						if (volumes != null && volumes.length > 0){
							logger.Info("Сформировано " + volumes.length + " пакетов для отправки.");
							try {
								Date now = new Date();
								Date till = new Date(now.getTime() + uploadTimeout*3600000);
								boolean ok = false;
								int sentVolumes = 0;
								for (File volume: volumes){
									logger.Info("Отправка пакета: "+volume.getAbsolutePath());
									ok = false;
									boolean rejected = false;
									int status = 0;
									while (!rejected && !ok && till.after(new Date())){
										try {
											if(this.daemonStateWatcher.isStopped()){
												logger.Info("Итерация прервана.");
												return;
											}
											status = session.UploadVolume(uploadToken, volume, volumes.length, volumeUploadTimeout);
											ok = status == 200;
											rejected = status == 406;
										} catch (SocketTimeoutException e) {
											//retry.add(volume);
											ok = false;
										} catch (IonException e) {
											ok = false;
											logger.Error("Ошибка отправки тома пакета!", e);												
										}
										if(ok){
											sentVolumes++;
											syncManager.onVolumeSent(volumes.length, sentVolumes);
										} 
									}
								}
											
								if (ok)
									syncManager.OutgoingPackageSent();
								else
									logger.Error("Не удалось отправить пакеты по причине недоступности сервера!");
							} catch (Exception e) {
								syncManager.PersistIncompleteUpload(volumes);
								logger.Error("Ошибка отправки пакета!", e);
							}	
						} else
							logger.Info("Нет данных для отправки.");
					} else
						logger.Warning("Ни один из пользователей не прошел аутентификацию!");
				} else 
					logger.Error("Не получены данные аутентификации!");
				
				if (init_result.volumes != null && init_result.volumes.length > 0){
					logger.Info("Загрузка...");
					VolumeDownloadInfo[] links = init_result.volumes;
					Date now = new Date();
					Date till = new Date(now.getTime() + downloadTimeout*3600000);						
					List<VolumeDownloadInfo> retry = new ArrayList<VolumeDownloadInfo>();
					File volume;
					while (links.length > 0 && till.after(new Date())){
						volume = null;
						retry.clear();
						for (VolumeDownloadInfo link: links){
							volume = null;
							try {
								if(this.daemonStateWatcher.isStopped()){
									logger.Info("Итерация прервана.");
									return;
								}
								volume = session.DownloadVolume(link.URL, checkConsistency?link.hashSum:null, volumeDownloadTimeout);
							} catch (SocketTimeoutException | SocketException e){
								retry.add(link);
								logger.Info("Том [" + link.URL + "] будет повторно загружен.");
							} catch (IonException e){
								logger.Error("Не удалось загрузить том пакета [" + link.URL + "]!", e);
							}
							
							if (volume != null){
								try {
									syncManager.AcceptVolume(volume);
								} catch (IonException e) {
									logger.Error("Ошибка приема тома пакета!", e);
								}
							}
						}
						links = retry.toArray(new VolumeDownloadInfo[retry.size()]);
					}
															
					boolean ok = false;
					if (links.length == 0){
						while (!ok && till.after(new Date())){
							try {
								ok = session.DownloadComplete(uploadToken, downloadCompletionTimeout) == 200;
							} catch (SocketTimeoutException | SocketException e) {
								ok = false;
							} catch (IonException e) {
								logger.Error("Ошибка завершения загрузки!", e);
								break;
							}
						}
					}
					
					if (ok)
						syncManager.ApplyIncomingPackage();
					else
						throw new IonException("Не удалось загрузить тома пакета! Превышен таймаут.");
					
				} else logger.Info("Нет данных для загрузки.");
				
				if (!syncManager.SyncComplete()){
					//stop(); // TODO Выпилить это нахуй
				}
				logger.Info("Сессия синхронизации завершена успешно.");
			} catch (Exception e){
				logger.Error("Сессия синхронизации была прервана!", e);
			}
		}	//else stop();	
	}
	
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (init == thisThread){
			iteration();
			if (init == thisThread)
				try {
					Thread.sleep(syncInterval*1000);
				} catch (InterruptedException e) {
					logger.Error("Ошибка потока!", e);
				}
		}
  }

	public Integer getSyncHorizon() {
		return syncHorizon;
	}

	public void setSyncHorizon(Integer syncHorizon) {
		this.syncHorizon = syncHorizon;
	}

	public int getDownloadTimeout() {
		return downloadTimeout;
	}

	public void setDownloadTimeout(int downloadTimeout) {
		this.downloadTimeout = downloadTimeout;
	}

	public int getUploadTimeout() {
		return uploadTimeout;
	}

	public void setUploadTimeout(int uploadTimeout) {
		this.uploadTimeout = uploadTimeout;
	}

	public int getVolumeDownloadTimeout() {
		return volumeDownloadTimeout;
	}

	public void setVolumeDownloadTimeout(int volumeDownloadTimeout) {
		this.volumeDownloadTimeout = volumeDownloadTimeout;
	}

	public int getVolumeUploadTimeout() {
		return volumeUploadTimeout;
	}

	public void setVolumeUploadTimeout(int volumeUploadTimeout) {
		this.volumeUploadTimeout = volumeUploadTimeout;
	}

	public int getSyncInterval() {
		return syncInterval;
	}

	public void setSyncInterval(int syncInterval) {
		this.syncInterval = syncInterval;
	}

	public ISyncManager getSyncManager() {
		return syncManager;
	}

	public void setSyncManager(ISyncManager syncManager) {
		this.syncManager = syncManager;
	}

	public int getDownloadCompletionTimeout() {
		return downloadCompletionTimeout;
	}

	public void setDownloadCompletionTimeout(int downloadCompletionTimeout) {
		this.downloadCompletionTimeout = downloadCompletionTimeout;
	}

	public void setCheckConsistency(boolean checkConsistency) {
		this.checkConsistency = checkConsistency;
	}

	public ILogger getLogger() {
		return logger;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}
	
	public void setDaemonStateWatcher(IDaemonStateAware daemonStateWatcher) {
		this.daemonStateWatcher = daemonStateWatcher;
	}
}
