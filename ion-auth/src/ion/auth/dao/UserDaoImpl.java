package ion.auth.dao;

import ion.auth.persistence.Authority;
import ion.auth.persistence.User;
import ion.auth.persistence.UserProperty;
import ion.auth.persistence.UserPropertyId;

import java.util.List;

import org.hibernate.SessionFactory;

public class UserDaoImpl {
	
	private SessionFactory sessionFactory;

	private PasswordHasher passwordEncoder;

	public PasswordHasher getPasswordEncoder() {
		return passwordEncoder;
	}

	public void setPasswordEncoder(PasswordHasher passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public Integer addUser(User user){
		String hashedPassword = passwordEncoder.hash(user.getPassword());
		user.setPassword(hashedPassword);
		if(user.getVersion() == null){user.setVersion(0);}
		sessionFactory.getCurrentSession().save(user);
		sessionFactory.getCurrentSession().flush();
	  return user.getId();
	}
	
	public User createUser(String username, String pwd){
		User result = new User();
		result.setEnabled(true);
		result.setUsername(username);
		result.setPassword(pwd);
		addUser(result);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> listUser(){
		return sessionFactory.getCurrentSession().createQuery("from User").list();
	}
	
	public void removeUser(Integer id){
		User user = (User) sessionFactory.getCurrentSession().load(User.class, id);
		if(null != user){
			sessionFactory.getCurrentSession().delete(user);
			sessionFactory.getCurrentSession().flush();
		}
	}
	
	public User getUser(Integer id){
		User user = (User) sessionFactory.getCurrentSession().load(User.class, id);
		return user;
	}
	
	public User getUser(String username){
		User user = (User) sessionFactory.getCurrentSession().
				createQuery("from User where username = :u").
				setParameter("u", username).uniqueResult();
		return user;
	}	
	
	public User chooseUser(Integer id){
		User user = (User) sessionFactory.getCurrentSession().get(User.class, id);
		return user;
	}
	
	public void updateUserAuthority(Integer id, Integer[] authIds){
		User userToAddAuthority = getUser(id);
		if(authIds != null){
			userToAddAuthority.getListOfAuthorities().clear();
			for(int i : authIds){
				Authority auth = new Authority();
				auth.setId(i);
				userToAddAuthority.getListOfAuthorities().add(auth);
			}
		}
		sessionFactory.getCurrentSession().update(userToAddAuthority);
		sessionFactory.getCurrentSession().flush();
	}

	public void updateUserPassword(Integer id, String password){
		User userToAddPassword = getUser(id);
		String hashedPassword = passwordEncoder.hash(password);
		userToAddPassword.setPassword(hashedPassword);
		sessionFactory.getCurrentSession().update(userToAddPassword);	
		sessionFactory.getCurrentSession().flush();
	}
	
	public void save(User u){
		sessionFactory.getCurrentSession().saveOrUpdate(u);
		sessionFactory.getCurrentSession().flush();
	}
	
	
	public void updateUserInfo(User user,Integer id){
		User userToUpdate = getUser(id);
		userToUpdate.setAccount_expired(user.getAccount_expired());
		userToUpdate.setAccount_locked(user.getAccount_locked());
		userToUpdate.setEnabled(user.getEnabled());
		userToUpdate.setPassword_expired(user.getPassword_expired());
		userToUpdate.setUsername(user.getUsername());
		userToUpdate.setVersion(user.getVersion());
		
		sessionFactory.getCurrentSession().update(userToUpdate);
		sessionFactory.getCurrentSession().flush();
	}
	
	public void setUserProperty(User user, String key, String value){
		UserProperty up = (UserProperty)sessionFactory.getCurrentSession().get(UserProperty.class, new UserPropertyId(user, key));
		if (up != null){
			up.setValue(value);
		} else {
			up = new UserProperty(user, key, value);
			sessionFactory.getCurrentSession().save(up);
			user.getProperties().add(up);
		}
		sessionFactory.getCurrentSession().flush();
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
