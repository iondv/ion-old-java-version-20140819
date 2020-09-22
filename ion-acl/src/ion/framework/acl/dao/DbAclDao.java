package ion.framework.acl.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ion.core.DACPermission;
import ion.core.IonException;
import ion.framework.acl.entity.AccessRecord;
import ion.framework.acl.entity.AccessRecordId;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DbAclDao {
	private SessionFactory sessionFactory;
	
	private Session getSession(){
		return sessionFactory.getCurrentSession();
	}
	
	private AccessRecord find(String ouid, String actor){
		Session sess = getSession();
		return (AccessRecord)sess.get(AccessRecord.class, new AccessRecordId(ouid, actor));
	}
	
	private int parsePermissions(DACPermission[] src){
		int res = 0;
		for (DACPermission p: src)
			res = res | p.getValue();
		return res;
	}
	
	public void Grant(String ouid, String actor, DACPermission[] permissions) throws IonException {
		AccessRecord r = find(ouid, actor);
		int p = parsePermissions(permissions);
		if (r == null)
			r = new AccessRecord(ouid, actor, p);
		else 
			r.setPermissions(r.getPermissions() | p);
		
		Session sess = getSession();
		sess.saveOrUpdate(r);
		sess.flush();
	}
	
	public void Grant(Set<String> ouids, String actor, DACPermission[] permissions) throws IonException {
		Set<String> tmp = new HashSet<String>(ouids);
		int p = parsePermissions(permissions);
		Session sess = getSession();
		Query q = sess.createQuery("from AccessRecord where actor = :actor").setString("actor", actor);
		@SuppressWarnings("unchecked")
		List<AccessRecord> records = q.list();
		for (AccessRecord r: records){
			if (!ouids.contains(r.getOuid()))
				sess.delete(r);
			else {
				r.setPermissions(r.getPermissions() | p);
				tmp.remove(r.getOuid());
			}
		}
		
		for (String id: tmp){
			AccessRecord r = find(id, actor);
			if (r == null)
				r = new AccessRecord(id, actor, p);
			else 
				r.setPermissions(r.getPermissions() | p);
			sess.saveOrUpdate(r);
		}
		sess.flush();
	}
	
	public void Deny(String ouid, String actor, DACPermission[] permissions) throws IonException {
		AccessRecord r = find(ouid, actor);
		int p = parsePermissions(permissions);
		if (r != null){
			r.setPermissions(r.getPermissions() & ~p);
			Session sess = getSession();
			if (r.getPermissions() > 0)
				sess.saveOrUpdate(r);
			else
				sess.delete(r);
			sess.flush();	
		}
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	
}
