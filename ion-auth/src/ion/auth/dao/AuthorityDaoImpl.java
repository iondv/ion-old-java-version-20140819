package ion.auth.dao;

import ion.auth.persistence.Authority;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class AuthorityDaoImpl {

	private SessionFactory sessionFactory;
	
	public void addAuthority(Authority authority){
		if(authority.getVersion() == null){ authority.setVersion(0); }
		sessionFactory.getCurrentSession().save(authority);
	}
	
	@SuppressWarnings("unchecked")
	public List<Authority> listAuthority(){
		return sessionFactory.getCurrentSession().createQuery("from Authority").list();
	}
	
	public void removeAuthority(Integer id){
		Authority authority = (Authority) sessionFactory.getCurrentSession().load(Authority.class, id);
		if(null != authority){
			sessionFactory.getCurrentSession().delete(authority);
			sessionFactory.getCurrentSession().flush();
		}
	}
	
	public Authority getAuthority(Integer id){
		Authority authority = (Authority) sessionFactory.getCurrentSession().load(Authority.class, id);
		return authority;
	}
	
	public Authority getAuthority(String code){
		Query q = sessionFactory.getCurrentSession().createQuery("from Authority where authority = :nm");
		q.setString("nm", code);
		Authority authority = null;
		try {
			authority = (Authority) q.uniqueResult();
		} catch (Exception e) {
			
		}
		return authority;
	}
	
	
	public Authority chooseAuthority(Integer id){
		Authority authority = (Authority) sessionFactory.getCurrentSession().get(Authority.class, id);
		return authority;
	}

	public void updateAuthority(Authority authority,Integer id){
		Authority authorityToUpdate = getAuthority(id);
		authorityToUpdate.setAuthority(authority.getAuthority());
		authorityToUpdate.setVersion(authority.getVersion());
		sessionFactory.getCurrentSession().update(authorityToUpdate);
		sessionFactory.getCurrentSession().flush();
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
}
