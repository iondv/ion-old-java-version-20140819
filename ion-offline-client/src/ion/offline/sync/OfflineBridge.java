package ion.offline.sync;

import java.util.HashMap;
import java.util.Map;

import ion.integration.core.UserCredentials;
import ion.integration.core.com.AbstractBridge;
import ion.integration.core.com.Externals;

public class OfflineBridge extends AbstractBridge {
	
	//private Transport transport;
	
	private Map<String, UserCredentials> credentials = new HashMap<String, UserCredentials>();
	
	public OfflineBridge(Externals externals) {
		super(externals);
		//this.transport = transport;		
	}
	
	@Override
	public boolean Enter(String login, UserCredentials user) {
		credentials.put(login, user);
		//transport.getSyncManager().AddCredentials(user.login, user.password);
		return true;
	}

	@Override
	public void Exit(String login) {
		UserCredentials user = credentials.get(login);
		if (user != null){
			credentials.remove(login);
			//transport.getSyncManager().RemoveCredentials(user.login);
		}
	}

	@Override
	public UserCredentials getCredentials(String login) {
		return credentials.get(login);
	}
	
	@Override
	public UserCredentials[] getCredentials(){
		return credentials.values().toArray(new UserCredentials[credentials.size()]);
	}	

	@Override
	public void Enable() {
		//transport.start();
	}

	@Override
	public void Disable() {
		//transport.stop();
	}
}
