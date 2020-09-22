package ion.web.admin;

import java.io.UnsupportedEncodingException;
import java.util.List;

import ion.auth.dao.AuthorityDaoImpl;
import ion.auth.dao.UserDaoImpl;
import ion.auth.persistence.Authority;
import ion.auth.persistence.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

	@Autowired
	private AuthorityDaoImpl authorityDao;
	
	@Autowired
	private UserDaoImpl userDao;
	
	@Transactional
	public List<Authority> listAuthorities() {
		return authorityDao.listAuthority();
	}
	
	@Transactional
	public void addAuthority(Authority authority) {
		authorityDao.addAuthority(authority);
	}

	@Transactional
	public void deleteAuthority(Integer[] ids) {
		if (ids != null){
			for(int i: ids){
				authorityDao.removeAuthority(i);
			}
		}
	}
	
	@Transactional
	public void deleteAuthorityById(Integer authId) {
		authorityDao.removeAuthority(authId);
	}
	
	@Transactional
	public Authority getAuthority(Integer authId) {
		return authorityDao.chooseAuthority(authId);
	}
	
	@Transactional
	public void updateAuthority(Authority authority, Integer authId) {
		authorityDao.updateAuthority(authority, authId);
	}	
	
	@Transactional
	public List<User> listUsers() {
		return userDao.listUser();
	}
	
	@Transactional
	public PagedListHolder<User> listUsersPage(Integer pageNumber, Integer pageSize) {
		Integer page = pageNumber - 1;
		PagedListHolder<User> pagedList = new PagedListHolder<User>(userDao.listUser());
		pagedList.setPageSize(pageSize);
		pagedList.setPage(page);
		return pagedList;
	}
	
	@Transactional
	public Integer addUser(User user,Integer[] userAuthorities) {
		if(userAuthorities != null){
			for(Integer authId : userAuthorities){
				Authority userAuthority = new Authority();
				userAuthority.setId(authId);
				user.getListOfAuthorities().add(userAuthority);
			}
		}
		
		return userDao.addUser(user);
	}
	
	@Transactional
	public void deleteUser(Integer[] ids) throws UnsupportedEncodingException {
		for(Integer id : ids){
			userDao.removeUser(id);
		}
	}
	
	@Transactional
	public void deleteUserById(Integer userId) {
		userDao.removeUser(userId);
	}
	
	@Transactional
	public User getUser(Integer userId) {
		return userDao.chooseUser(userId);
	}
	
	@Transactional
	public void updateUser (User user, Integer userId, Integer[] authIds) {
		userDao.updateUserInfo(user, userId);
		if(authIds != null){
			userDao.updateUserAuthority(userId, authIds);			
		}
	}
	
	@Transactional
	public void updateUserPassword (Integer userId, String password) {
		userDao.updateUserPassword(userId, password);
	}
	
	
}
