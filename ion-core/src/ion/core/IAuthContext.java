package ion.core;

public interface IAuthContext {
	public IUserContext CurrentUser();
	public void enableContextReload(String u);
}
