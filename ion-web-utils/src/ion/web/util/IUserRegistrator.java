package ion.web.util;

import ion.core.IonException;

public interface IUserRegistrator {
	public void registerUser(String username, String password) throws IonException;
}
