package ion.framework.offline.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.xml.soap.SOAPException;

import ion.core.IonException;
import ion.core.logging.IonLogger;
import ion.offline.adapters.SitexServicePortalAdapterFactory;
import ion.offline.data.ZipVolumeProcessor;
import ion.offline.security.HashProvider;
import ion.offline.server.IonQueueTargetSystemRequestor;
import ion.offline.server.PackageQueue;
import ion.offline.server.dao.DataPackageDAO;
import ion.offline.server.dao.PointDAO;
import ion.offline.server.dao.UserDAO;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
//import org.hibernate.service.ServiceRegistryBuilder;

public class Worker {
	
	private IonQueueTargetSystemRequestor requestor;
	
	private SessionFactory sessionFactory;
	
	private IonLogger logger;
	
	private long interval = 3600;
	
	public Worker() throws IonException, SOAPException {
		logger = new IonLogger("Offline Worker");
		
		Properties props = new Properties();
		try {
			File cwd = new File(".");
			for (File f: cwd.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".daemon.properties");
				}
			})){
				FileInputStream fis = new FileInputStream(f);
				props.load(fis);
				fis.close();
			}
		} catch (IOException e) {
			throw new IonException(e);
		}
		
		if (props.containsKey("iteration.interval"))
			interval = Long.parseLong(props.getProperty("iteration.interval"));
		
    	requestor = new IonQueueTargetSystemRequestor();
    	@SuppressWarnings("rawtypes")
    	Class[] classes = {
    			ion.offline.server.entity.Point.class,
    			ion.offline.server.entity.DataPackage.class,
    			ion.offline.server.entity.User.class
    	};
    	
    	SitexServicePortalAdapterFactory adapter = new SitexServicePortalAdapterFactory();
    	
    	adapter.setup(props);

    	requestor.setAdapterFactory(adapter);
    	
    	sessionFactory = Worker.createSessionFactory(props,classes);    	
    	
    	requestor.setSessionFactory(sessionFactory);

    	PointDAO pd = new PointDAO();
    	pd.setSessionFactory(sessionFactory);
    	requestor.setPointDAO(pd);
    	
    	UserDAO ud = new UserDAO();
    	ud.setSessionFactory(sessionFactory);
    	requestor.setUserDAO(ud);
    	
    	DataPackageDAO dpd = new DataPackageDAO();
    	dpd.setSessionFactory(sessionFactory);
    	
    	PackageQueue packages = new PackageQueue();
    	packages.setDataPackageDAO(dpd);
    	packages.setHashProvider(new HashProvider());
    	packages.setLogger(new IonLogger("server daemon"));
    	
    	packages.setVolumeProcessor(new ZipVolumeProcessor(10240));
    	packages.setOutgoingDirectory(new File(props.getProperty("packages.path.outgoing")));
    	packages.setUrlBase(props.getProperty("packages.path.urlBase"));
    	if (props.containsKey("packages.path.work"))
    		packages.setWorkDirectory(new File(props.getProperty("packages.path.work")));
    	else
    		packages.setWorkDirectory(new File("sync"));
    	requestor.setPackages(packages);
    	
    	logger.Info("Worker initialized");
	}
	
	public long getInterval(){
		return interval;
	}
	
	
	@SuppressWarnings("rawtypes")
	private static SessionFactory createSessionFactory(Properties settings, Class[] classes) throws IonException {
		Configuration configuration = new Configuration();
		configuration.setProperties(settings);
		//configuration.configure();
		for(Class c:classes)
			configuration.addAnnotatedClass(c);
		
		/*ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		return configuration.buildSessionFactory(serviceRegistry);
		*/
		
		StandardServiceRegistryBuilder srg = new StandardServiceRegistryBuilder();
		srg.applySettings(configuration.getProperties());
		return configuration.buildSessionFactory(srg.build());
	}

	public void run(){		
	  Date d = new Date(); 
	  System.out.println(d.toString()+": starting data load iteration");
		try {
			requestor.QueuePackages();
		} catch (Exception e) {
			System.err.println("Ошибка при формировании пакетов!");
			e.printStackTrace(System.err);
		} finally {
		}
	}
}
