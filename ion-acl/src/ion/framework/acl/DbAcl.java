package ion.framework.acl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ion.core.acl.IonAcl;
import ion.core.logging.ILogger;
import ion.framework.acl.entity.AccessRecord;

public class DbAcl extends IonAcl {

	private ILogger logger;
	
	private SessionFactory sessionFactory;
	
	@Override
	protected boolean check(String ouid, String[] uids, int permissions) {
		Session sess = sessionFactory.getCurrentSession();
		try {
			Query q = sess.createQuery("from AccessRecord where ouid = :oid and actor in (:actors)");
			q.setString("oid", ouid);
			q.setParameterList("actors", uids);
			@SuppressWarnings("unchecked")
			List<AccessRecord> records = q.list();
			int chperm = 0;
			for (AccessRecord r: records)
				chperm = chperm | r.getPermissions();
			
			if ((permissions & chperm) == permissions)
				return true;
		} catch (Exception e) {
			//e.printStackTrace();
			logger.Error("Ошибка проверки доступа!", e);
		}
		
		return false;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ILogger getLogger() {
		return logger;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	@Override
	protected List<String> getGrantedList(String[] uids, int permissions) {
		List<String> result = new ArrayList<String>();
		Session sess = sessionFactory.getCurrentSession();
		Query q = sess.createQuery("from AccessRecord where actor in (:actors)");
		q.setParameterList("actors", uids);
		@SuppressWarnings("unchecked")
		List<AccessRecord> records = q.list();
		for (AccessRecord r: records){
			int perm = r.getPermissions() & permissions;
			if(perm==permissions) result.add(r.getOuid());
		}
		return result;
	}

	@Override
	protected List<String> getGrantedList(String[] uids, int permissions,
			String prefix) {
		List<String> result = new ArrayList<String>();
		Session sess = sessionFactory.getCurrentSession();
		Query q = sess.createQuery("from AccessRecord where actor in (:actors) and substring(ouid,1," + prefix.length() + ") = :pref");
		q.setParameterList("actors", uids);
		q.setString("pref", prefix);
		@SuppressWarnings("unchecked")
		List<AccessRecord> records = q.list();
		for (AccessRecord r: records){
			int perm = r.getPermissions() & permissions;
			if(perm == permissions) result.add(r.getOuid().substring(prefix.length()));
		}
		return result;
	}

}
