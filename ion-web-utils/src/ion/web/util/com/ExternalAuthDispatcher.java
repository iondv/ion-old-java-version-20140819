package ion.web.util.com;

import java.util.LinkedList;
import java.util.List;

import ion.web.util.IExternalAuthDispatcher;
import ion.web.util.IExternalAuthProvider;

public class ExternalAuthDispatcher implements IExternalAuthDispatcher {
	
	private List<IExternalAuthProvider> providers = new LinkedList<IExternalAuthProvider>();

	@Override
	public void RegisterProvider(IExternalAuthProvider provider) {
		providers.add(provider);
	}

	@Override
	public void Authenticate(String login, String password) {
		for (IExternalAuthProvider p: providers)
			p.Authenticate(login, password);
	}
}
