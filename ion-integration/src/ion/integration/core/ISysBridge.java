package ion.integration.core;

public interface ISysBridge {
	String getName();
	boolean Enter(String login, UserCredentials user);
	void Exit(String login);
	UserCredentials getCredentials(String login);
	UserCredentials[] getCredentials();
	void Enable();
	void Disable();
}