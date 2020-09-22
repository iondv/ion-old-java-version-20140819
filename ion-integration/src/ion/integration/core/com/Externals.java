package ion.integration.core.com;

import ion.integration.core.ISysBridge;
import ion.integration.core.UserCredentials;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Externals {
	private Map<String, ISysBridge> bridges = new LinkedHashMap<String, ISysBridge>();
	
	public void RegisterBridge(ISysBridge bridge){
		bridges.put(bridge.getName(),bridge);
	}
	
	public Map<String, UserCredentials> getBridgeCredentials(String login){
		Map<String, UserCredentials> result = new HashMap<String, UserCredentials>();
		for (ISysBridge b : bridges.values()){
			result.put(b.getName(),b.getCredentials(login));
		}
		return result;
	} 
	
	public void SetCredentials(String bridge, String login, UserCredentials user){
		ISysBridge b = bridges.get(bridge);
		if (b != null){
			b.Exit(login);
			b.Enter(login, user);
		}
	}
	
	public void Connect(){
		for (ISysBridge b : bridges.values())
			b.Enable();
	}
	
	public void Disconnect(){
		for (ISysBridge b : bridges.values())
			b.Disable();		
	}
}
