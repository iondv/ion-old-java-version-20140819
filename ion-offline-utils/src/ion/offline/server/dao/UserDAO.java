package ion.offline.server.dao;

import ion.core.IonException;
import ion.offline.server.entity.Point;
import ion.offline.server.entity.User;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class UserDAO implements IUserDAO {
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private Session curSession() {
		return sessionFactory.getCurrentSession();
	}

	public void addUser(String name, Point point, String token)
																														 throws IonException {
		this.addUser(new User(name, point, token));
	};

	public void addUser(User user) throws IonException {
		Session session = curSession();
		try {
			session.saveOrUpdate(user);
			session.flush();
		} catch (Exception e) {
			throw new IonException(e);
		}
	};

	public void attachUser(String name, Point point, String token)
																																throws IonException {
		Session session = curSession();
		try {
			User u = getUser(name);
			if (u == null)
				addUser(name, point, token);
			else {
				u.setPoint(point);
				u.setToken(token);
				session.update(u);
				session.flush();
			}
		} catch (Exception e) {
			throw new IonException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public User[] UsersByPoint(Point point) throws IonException {
		Session session = curSession();
		try {
			List<User> users = session.createQuery("from User where point=:pid")
																.setInteger("pid", point.getId()).list();
			return users.toArray(new User[users.size()]);
		} catch (Exception e) {
			throw new IonException(e);
		}
	};

	public User getUser(String login) throws IonException {
		Session session = curSession();
		try {
			return (User) session.get(User.class, login);
		} catch (Exception e) {
			throw new IonException(e);
		}
	};

	public void ResetUsersTokens(Point point) throws IonException {
		Session session = curSession();
		try {
			session.createQuery("update User set token='' where point=:pid")
						 .setInteger("pid", point.getId()).executeUpdate();
			session.flush();
		} catch (Exception e) {
			throw new IonException(e);
		}
	};

	public void DeleteUser(User user) throws IonException {
		Session session = curSession();
		try {
			session.delete(user);
			session.flush();
		} catch (Exception e) {
			throw new IonException(e);
		}
	};
}
