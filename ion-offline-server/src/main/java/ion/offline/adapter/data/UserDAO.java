package ion.offline.adapter.data;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ion.core.IonException;
import ion.offline.server.dao.IUserDAO;
import ion.offline.server.entity.Point;
import ion.offline.server.entity.User;

public class UserDAO extends ion.offline.server.dao.UserDAO implements IUserDAO {

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void addUser(String name, Point point, String token)
																														 throws IonException {
		super.addUser(name, point, token);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void addUser(User user) throws IonException {
		super.addUser(user);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void attachUser(String name, Point point, String token)
																																throws IonException {
		super.attachUser(name, point, token);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public User[] UsersByPoint(Point point) throws IonException {
		return super.UsersByPoint(point);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public User getUser(String login) throws IonException {
		return super.getUser(login);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void ResetUsersTokens(Point point) throws IonException {
		super.ResetUsersTokens(point);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor={IonException.class})	
	public void DeleteUser(User user) throws IonException {
		super.DeleteUser(user);
	}
}
