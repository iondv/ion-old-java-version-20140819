package ion.web.util;

public interface IExternalAuthProvider {
	void Authenticate(String login, String password);
}
