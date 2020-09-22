package ion.web.app.util;

import ion.integration.core.ISysBridge;
import ion.integration.core.UserCredentials;
import ion.web.util.IExternalAuthDispatcher;
import ion.web.util.IExternalAuthProvider;

public class SysBridgeAuthenticator implements IExternalAuthProvider {

	private ISysBridge bridge;
	
	public SysBridgeAuthenticator(IExternalAuthDispatcher dispatcher){
		dispatcher.RegisterProvider(this);
	}
	
	@Override
	public void Authenticate(String login, String password) {
		bridge.Enter(login, new UserCredentials(login, password));
	}

	public ISysBridge getBridge() {
		return bridge;
	}

	public void setBridge(ISysBridge bridge) {
		this.bridge = bridge;
	}

}
