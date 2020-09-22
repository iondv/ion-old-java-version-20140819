package ion.offline.server.dao;

import java.io.File;
import java.util.Date;
import java.util.List;

import ion.core.IonException;
import ion.offline.server.entity.DataPackage;
import ion.offline.server.entity.Point;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DataPackageDAO implements IDataPackageDAO {
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

	public DataPackage CreateNewPackage(Point point, File directory_destination)
																																							throws IonException {
		DataPackage data_package = AddDataPackage(new DataPackage(point, new Date()));
		data_package.setDirectory(new File(directory_destination,
																			 data_package.getId().toString()).getAbsolutePath());
		updateDataPackage(data_package);
		return data_package;
	}

	public void DeletePackage(DataPackage data_package) throws IonException {
		Session session = curSession();
		try {
			session.delete(data_package);
			session.flush();
		} catch (Exception e) {
			throw new IonException(e);
		}
	}

	public DataPackage AddDataPackage(DataPackage data_package)
																														 throws IonException {
		Session session = curSession();
		try {
			session.save(data_package);
			session.flush();
			return data_package;
		} catch (Exception e) {
			throw new IonException(e);
		}
	}

	public void updateDataPackage(DataPackage data_package) throws IonException {
		Session session = curSession();
		try {
			session.update(data_package);
			session.flush();
		} catch (Exception e) {
			throw new IonException(e);
		}
	}

	public DataPackage GetLastPackage(int point) {
		Session session = curSession();
		Query q = session.createQuery("from DataPackage where point=:pid order by generating desc");
		q.setInteger("pid", point);
		q.setMaxResults(1);
		DataPackage data_package = (DataPackage) q.uniqueResult();
		return data_package;
	}

	public DataPackage GetFirstPackage(int point) {
		Session session = curSession();
		Query q = session.createQuery("from DataPackage where point=:pid order by generating asc");
		q.setInteger("pid", point);
		q.setMaxResults(1);
		DataPackage data_package = (DataPackage) q.uniqueResult();
		return data_package;
	}

	@SuppressWarnings("unchecked")
	public List<DataPackage> GetQueue(int count) {
		Session session = curSession();
		Query q = session.createQuery("from DataPackage order by generating asc");
		q.setMaxResults(count);
		return (List<DataPackage>) q.list();
	}
}
