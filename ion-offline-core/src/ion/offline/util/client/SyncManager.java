package ion.offline.util.client;

import java.util.HashMap;
import java.util.Map;

import ion.integration.core.UserCredentials;
import ion.offline.util.ISyncManager;

public abstract class SyncManager implements ISyncManager {
	
	protected Map<String,String> credentials = new HashMap<String, String>();	

	public void AddCredentials(String login, String password){
		credentials.put(login, password);
	}
	
	public void RemoveCredentials(String login){
		credentials.remove(login);
	}
	
	public UserCredentials[] GetCredentials(){
		UserCredentials[] result = new UserCredentials[credentials.size()];
		int i = 0;
		for (Map.Entry<String,String> entry: credentials.entrySet()){
			result[i] = new UserCredentials(entry.getKey(), entry.getValue());
			i++;
		}
		return result;
	}

}
