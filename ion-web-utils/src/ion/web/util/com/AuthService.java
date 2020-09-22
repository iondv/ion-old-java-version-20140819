package ion.web.util.com;

import java.util.Collection;
import java.util.LinkedList;

import ion.auth.dao.AuthorityDaoImpl;
import ion.auth.dao.UserDaoImpl;
import ion.auth.persistence.Authority;
import ion.auth.persistence.User;
import ion.core.IonException;
import ion.web.util.IUserRegistrator;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class AuthService implements UserDetailsService, IUserRegistrator {
	
	private UserDaoImpl userDao;
	
	private AuthorityDaoImpl authDao;

	private String[] roles;
	
	public void setDefaultUserRoles(String roles){
		this.roles = roles.split("\\s*,\\s*");
	}
	
	public void setUserDao(UserDaoImpl userDao) {
		this.userDao = userDao;
	}
		
	public void setAuthDao(AuthorityDaoImpl authDao) {
		this.authDao = authDao;
	}	

	@Override
	@Transactional(rollbackFor=IonException.class)
	public void registerUser(String username, String password) throws IonException{
		try {
			User u = userDao.createUser(username, password);
			Authority a = null;
			for (String role: roles){
				a = authDao.getAuthority(role);
				if (a != null){
					u.getListOfAuthorities().add(a);
				}
			}
			userDao.save(u);
		} catch (Exception e){
			if (e instanceof ConstraintViolationException)
				throw new IonException("Пользователь с таким именем уже зарегистрирован в системе!");
			else
				throw new IonException(e);
		}
	}	
	
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username)
																												throws UsernameNotFoundException {
		User u = userDao.getUser(username);
		if (u == null)
			throw new UsernameNotFoundException("Пользователь "+username+" не найден!");

    Collection<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
    for (Authority role : u.getListOfAuthorities()) {
      authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
    }		
		
		return new org.springframework.security.core.userdetails.User(u.getUsername(),u.getPassword(),u.getEnabled(),u.getEnabled(),u.getEnabled(),u.getEnabled(),authorities);
	}
}
