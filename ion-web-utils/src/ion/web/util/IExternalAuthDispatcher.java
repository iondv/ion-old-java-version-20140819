package ion.web.util;

public interface IExternalAuthDispatcher {
	void RegisterProvider(IExternalAuthProvider provider);
	void Authenticate(String login, String password);
}
