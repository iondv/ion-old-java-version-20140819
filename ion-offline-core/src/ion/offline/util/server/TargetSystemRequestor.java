package ion.offline.util.server;

import java.io.IOException;
import java.util.Date;

import ion.core.IonException;
import ion.core.logging.ILogger;
import ion.core.logging.IonLogger;
import ion.offline.util.IPackageQueue;
import ion.offline.util.ITargetSystemAdapter;
import ion.offline.util.ITargetSystemAdapterFactory;
import ion.offline.util.ITargetSystemRequestor;
import ion.offline.util.SyncDelta;

public abstract class TargetSystemRequestor implements ITargetSystemRequestor {
	
	protected ILogger logger = new IonLogger("Target system requestor");
	
	protected class ClientInfo {
		public String id;		
		public Date lastSync;
		public Integer syncHorizon;
		public ClientInfo (String id, Date lastSync, Integer syncHorizon) {
			super();
			this.id = id;
			this.lastSync = lastSync;
			this.syncHorizon = syncHorizon;
		}
	}

	protected ITargetSystemAdapterFactory adapterFactory;
	
	protected IPackageQueue packages;	
	
	public void setAdapterFactory(ITargetSystemAdapterFactory adapterFactory) {
		this.adapterFactory = adapterFactory;
	}

	public IPackageQueue getPackages() {
		return packages;
	}

	public void setPackages(IPackageQueue packages) {
		this.packages = packages;
	}
	
	public void QueuePackages() throws IonException, IOException {
		ITargetSystemAdapter adapter = adapterFactory.getAdapter();
		for (ClientInfo client: clients()){
			String[] users = users(client.id);
			if (users != null && users.length > 0)
			try {
				Date syncDate = new Date();
				logger.Info("Package forming for client " + client.id + " started");
				SyncDelta delta = adapter.getDelta(client.id, users, client.lastSync, client.syncHorizon);
				queue(client, delta);
				logger.Info("Package forming for client " + client.id + " finished");
				setSyncTime(client.id, syncDate);
			} catch (IOException e){
				throw e;
			} catch (Exception e) {
				throw new IonException(e);
			}
		}
	}
	
	protected void queue(ClientInfo client, SyncDelta delta) throws IonException, IOException{
		if (delta != null){
			packages.Queue(client.id, delta.profiles, delta.meta, delta.data, delta.navigation, delta.view, delta.aux);
			packages.Pack(client.id);
		}		
	}
	
	protected abstract void setSyncTime(String client, Date date);

	protected abstract ClientInfo[] clients();
	
	protected abstract String[] users(String client);
	
}
