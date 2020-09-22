package ion.offline.util.server;

import java.io.File;
import java.io.IOException;

import ion.core.IonException;
import ion.integration.core.UserCredentials;
import ion.offline.net.ActionResult;
import ion.offline.net.AuthResult;
import ion.offline.net.DataChange;
import ion.offline.net.DataUnit;
import ion.offline.net.SyncRequestResult;
import ion.offline.util.IPackageQueue;
import ion.offline.util.ISyncSession;
import ion.offline.util.ITargetSystemAdapter;
import ion.offline.util.ITargetSystemAdapterFactory;

public abstract class SyncSession implements ISyncSession {
	
	protected IPackageQueue packages;
	
	protected ITargetSystemAdapterFactory adapterFactory;
	
	public IPackageQueue getPackages() {
		return packages;
	}

	public void setPackages(IPackageQueue packages) {
		this.packages = packages;
	}

	public void setAdapterFactory(ITargetSystemAdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
	}

	@Override
	public SyncRequestResult Init(String client, UserCredentials[] users, Integer syncHorizon) throws IonException {
		AuthResult[] auth = adapterFactory.getAdapter().authenticate(users);
		processAuthentication(client, auth);
		setPointSyncHorizon(client, syncHorizon);		
		prepare(client);
		SyncRequestResult res = new SyncRequestResult(generateUploadToken(client), auth, packages.PendingPackage(client));
		packages.BeginDequeue(client);
		return res;
	}
	
	@Override
	public boolean DownloadComplete(String client, String token) throws IonException, IOException {
		if (checkUploadToken(client, token)){
			packages.CommitDequeue(client);
			return true;
		}
		return false;
	}
	
	protected abstract void processAuthentication(String client, AuthResult[] auth) throws IonException;
	
	protected abstract void setPointSyncHorizon(String client, Integer syncHorizon) throws IonException;
	
	protected abstract String generateUploadToken(String client) throws IonException;
	
	protected abstract boolean checkUploadToken(String client, String token) throws IonException;
	
	protected abstract void prepare(String client) throws IonException;

	protected abstract boolean accept(String client, File file, String hashSum) throws IOException, IonException;

	protected abstract boolean packageSent(String client) throws IOException, IonException;
		
	protected abstract boolean packageReady(String client,int total) throws IonException;
	
	protected abstract DataUnit[] unPack(String client) throws IOException, IonException;
	
	protected abstract String getUserAuthToken(String login) throws IonException;

	@Override
	public boolean AcceptVolume(String client, String token, File file, String hashSum, int total) throws IonException, IOException {
		if (checkUploadToken(client, token)){
			if (!accept(client, file, hashSum)){
				// TODO Если том уже загружен, надо бы возвращать http 200 но ничего не делать
				
				System.out.println("Том не был принят: "+file.getAbsolutePath());
				return false;
			}
			
			if (packageReady(client,total)){
				try{
					ITargetSystemAdapter adapter = adapterFactory.getAdapter();
					System.out.println("Пакет принят полностью, передача данных...");
					DataUnit[] data = unPack(client);
					System.out.println("К передаче "+data.length+" изменений.");
					for (DataUnit entry: data){
						if (entry instanceof DataChange) {
							DataChange change = (DataChange)entry;
	  					System.out.println("Передаем изменение от лица " + change.author);					
	  					String user_token = getUserAuthToken(change.author);
	  					if (user_token != null){
	  						System.out.println("Отправка объекта...");
	  						ActionResult result = adapter.push(user_token, change);
	  						if (!result.success){
	  							System.out.println("Объект не отправлен.");
	  							// TODO: Фиксация ошибок отправки сущностей в ЦС
	  						} else
	  							System.out.println("Объект отправлен.");
	  					}
						} else {
	  					System.out.println("Передаем объект класса " + entry.className);					
							ActionResult result = adapter.push("", entry);
							if (!result.success){
								System.out.println("Объект не отправлен.");
								// TODO: Фиксация ошибок отправки сущностей в ЦС
							} else
								System.out.println("Объект отправлен.");
							
							// TODO: DEV-75: отправка электронных подписей
						}
							
					}
				} finally {
					packageSent(client);
				}
			}
			return true;
		}
		return false;
	}
}
