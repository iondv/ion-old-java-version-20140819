package ion.offline.server.dao;

import java.io.Serializable;
import java.util.List;

import ion.core.IonException;
import ion.offline.server.entity.Point;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class PointDAO implements IPointDAO {

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

	public Point addPoint() throws IonException {
		Session session = curSession();
		try {
			Serializable id = session.save(new Point());
			session.flush();
			return (Point) session.load(Point.class, id);
		} catch (Exception e) {
			throw new IonException(e);
		}
	};

	public void updatePoint(Point point) throws IonException {
		Session session = curSession();
		try {
			session.update(point);
			session.flush();
		} catch (Exception e) {
			throw new IonException(e);
		}
	};

	public void DeletePoint(Point point) throws IonException {
		Session session = curSession();
		try {
			session.delete(point);
			session.flush();
		} catch (Exception e) {
			throw new IonException(e);
		}
	};

	public Point GetPointById(Serializable point_id) {
		Session session = curSession();
		return (Point) session.get(Point.class, point_id);
	};

	@SuppressWarnings("unchecked")
	public Point[] GetPoints() {
		Session session = curSession();
		List<Point> points = (List<Point>) session.createCriteria(Point.class)
																							.list();
		return points.toArray(new Point[points.size()]);
	}
}
