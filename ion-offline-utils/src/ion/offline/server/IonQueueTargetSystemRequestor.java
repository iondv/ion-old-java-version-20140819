package ion.offline.server;

import java.io.IOException;
import java.util.Date;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;

import ion.core.IonException;
import ion.offline.server.dao.IPointDAO;
import ion.offline.server.dao.IUserDAO;
import ion.offline.server.entity.Point;
import ion.offline.server.entity.User;
import ion.offline.util.SyncDelta;
import ion.offline.util.server.TargetSystemRequestor;

public class IonQueueTargetSystemRequestor extends TargetSystemRequestor {
	
	private SessionFactory sessionFactory;
	
	private IPointDAO pointDAO;
	
	private IUserDAO userDAO;
	
	public IPointDAO getPointDAO() {
		return pointDAO;
	}

	public void setPointDAO(IPointDAO pointDAO) {
		this.pointDAO = pointDAO;
	}

	public IUserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(IUserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	private void begin(){
		Session sess = sessionFactory.openSession();
		sess.setFlushMode(FlushMode.MANUAL);
		ManagedSessionContext.bind(sess);		
	  sessionFactory.getCurrentSession().beginTransaction();
	}
	
	private void commit(){
		sessionFactory.getCurrentSession().flush();			
		sessionFactory.getCurrentSession().getTransaction().commit();
		sessionFactory.getCurrentSession().close();
		//sessionFactory.close();
		ManagedSessionContext.unbind(sessionFactory);		
	}
	
	private void rollback(){
		sessionFactory.getCurrentSession().getTransaction().rollback();		
		sessionFactory.getCurrentSession().close();
		//sessionFactory.close();
		ManagedSessionContext.unbind(sessionFactory);		
	}
	
	@Override
	protected void queue(ClientInfo client, SyncDelta delta) throws IonException, IOException {
		begin();
		try {
			super.queue(client, delta);
			commit();
		} catch (IonException e) {
			rollback();
			throw e;
		}
	}
		
	@Override
	protected void setSyncTime(String client, Date date) {
		begin();
		try {
			Point point = pointDAO.GetPointById(Integer.parseInt(client));
			point.setLastDataPackageGenerating(date);
			pointDAO.updatePoint(point);
			commit();
		} catch (IonException e) {
			rollback();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected ClientInfo[] clients() {
		ClientInfo[] clients = null;
		begin();
		try {
			Point[] points = pointDAO.GetPoints();
			clients = new ClientInfo[points.length];
			for(int i=0; i<points.length; i++){
				clients[i] = new ClientInfo(points[i].getId().toString(), points[i].getLastDataPackageGenerating(), points[i].getSyncHorizon());
			}
			commit();
		} catch (Exception e) {
			rollback();
			clients = new ClientInfo[0];
		}
		return clients;
	}

	@Override
	protected String[] users(String client) {
		String[] strings;
		begin();
		try {
			Point point = pointDAO.GetPointById(Integer.parseInt(client));	
			User[] users = userDAO.UsersByPoint(point);
			commit();
			strings = new String[users.length];
			for(int i=0;i<users.length; i++)
					strings[i] = users[i].getNickname();
		} catch(IonException e) {
			rollback();
			e.printStackTrace();
			strings = null;
		}
		return strings;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}	
}
