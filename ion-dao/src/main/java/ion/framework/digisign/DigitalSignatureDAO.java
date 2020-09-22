package ion.framework.digisign;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DigitalSignatureDAO {
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void addSign(String actor, String className, String objId, String action, String attributes, byte[] sign, byte[] data) {
		Session session = sessionFactory.getCurrentSession();
		DigitalSignature signObj = new DigitalSignature();
		signObj.setActor(actor);
		signObj.setTs(new Date());
		signObj.setClassName(className);
		signObj.setObjId(objId);
		signObj.setPart(0);
		signObj.setAction(action);
		signObj.setAttributes(attributes);
		signObj.setSign(sign);
		signObj.setData(data);
		session.save(signObj);
	}
	
	public void addSign(String actor, String className, String objId, Integer index, String action, String attributes, byte[] sign, byte[] data) {
		Session session = sessionFactory.getCurrentSession();
		DigitalSignature signObj = new DigitalSignature();
		signObj.setActor(actor);
		signObj.setTs(new Date());
		signObj.setClassName(className);
		signObj.setObjId(objId);
		signObj.setPart(index);
		signObj.setAction(action);
		signObj.setAttributes(attributes);
		signObj.setSign(sign);
		signObj.setData(data);
		session.save(signObj);
	}
	
	public List<DigitalSignature> getSigns(Date since) {
		return getSigns(since, null);
	}
	
	@SuppressWarnings("unchecked")
	public List<DigitalSignature> getSigns(Date since, Date till) {
		String hql = "from DigitalSignature where ts >= :since"
				+ (till == null ? "" : " and ts < :till");
		Session session = sessionFactory.getCurrentSession();
    Query q = session.createQuery(hql)
 			.setTimestamp("since", since);
		if (till != null)
			q.setTimestamp("till", till);
		return q.list();
	}
}
