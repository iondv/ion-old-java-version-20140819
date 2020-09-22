import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import ion.core.IonException;
import ion.offline.server.dao.PointDAO;
import ion.offline.server.entity.Point;
import ion.offline.security.SignatureProvider;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class IonOfflineToolkit {
    private final static String ADD_POINT = "-newPoint";
    private final static String GENERATE_KEYS = "-keyGen";
		
	public static void main(String[] args) {
		if(args.length > 0){
			String op = args[0];
			try {
				Properties params = loadSettings();
				
				Integer point = null;
				
				for (int i = 1; i < args.length; i++){
					if (args[i].equals("-p") || args[i].equals("-kA") || args[i].equals("-rA") || args[i].equals("-kL")){
						if (i == args.length - 1 ||
								args[i + 1].equals("-p") || args[i + 1].equals("-kA") || args[i + 1].equals("-rA") || args[i + 1].equals("-kL")
							)
							throw new IonException("Не указано значение параметра "+args[i]);
						
						if (args[i].equals("-p"))
							point = Integer.parseInt(args[i + 1].toString());
						else if (args[i].equals("-kA"))
							params.put("signature.key_algo", args[i + 1].toString());
						else if (args[i].equals("-rA"))
							params.put("signature.rand_algo", args[i + 1].toString());
						else if (args[i].equals("-kL"))
							params.put("signature.key_size", args[i + 1].toString());
					}
				}
				Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);	
				switch (op) {
					case ADD_POINT:			
						newPoint(params);
						break;
					case GENERATE_KEYS:
						keyGen(params, point);
						break;
					default:throw new RuntimeException("Неверная команда!");
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static SessionFactory configureSessionFactory(Class[] classes, Properties settings) throws IonException {
		Configuration configuration = new Configuration();
		configuration.setProperties(settings);
		//configuration.configure();
		for(Class c:classes)
			configuration.addAnnotatedClass(c);
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		return configuration.buildSessionFactory(serviceRegistry);
	}
	
	private static Properties loadSettings() throws IonException {
		Properties props = new Properties();
		try {
			File f = new File("./toolkit.properties");
			if (f.exists())
				props.load(new FileInputStream(f));
		} catch (IOException e) {
			throw new IonException(e);
		}
		return props;
	}
		
	@SuppressWarnings("rawtypes")
	private static void newPoint(Properties params) throws IonException {
		Class[] classes = {ion.offline.server.entity.Point.class};
		SessionFactory sessionFactory = configureSessionFactory(classes,params);
		PointDAO pointDAO = new PointDAO();
		pointDAO.setSessionFactory(sessionFactory);
		
		Point point = null;
		Session s = sessionFactory.getCurrentSession();
		Transaction t = s.beginTransaction(); 
		try {
			point = pointDAO.addPoint();
			t.commit();
		} catch (Exception e){
			t.rollback();
			System.out.println("Не удалось зарегистрировать нового клиента.");
			throw new IonException(e);
		} 
		System.out.println("offline.client.id=" + point.getId());
		sessionFactory.close();
		keyGen(params, point.getId());
	}	
	
	private static void keyGen(Properties params, int pointId) throws IonException {
		SignatureProvider provider = new SignatureProvider();
		if (params.containsKey("signature.key_algo"))
			provider.setKeyAlgo(params.getProperty("signature.key_algo"));
		
		if (params.containsKey("signature.rand_algo"))
			provider.setRandomizeAlgo(params.getProperty("signature.rand_algo"));
		
		if (params.containsKey("signature.key_size"))
			provider.setKeySize(Integer.parseInt(params.getProperty("signature.key_size")));
		
		String[] keys = provider.createKeyPair();
		
		@SuppressWarnings("rawtypes")
		Class[] classes = {ion.offline.server.entity.Point.class};
		SessionFactory sessionFactory = configureSessionFactory(classes, params);
		PointDAO pointDAO = new PointDAO();
		pointDAO.setSessionFactory(sessionFactory);
		
		Point point = null;
		Session s = sessionFactory.getCurrentSession();
		Transaction t = s.beginTransaction(); 
		try {
			point = pointDAO.GetPointById(pointId);
			point.setOpenKey(keys[1]);
			pointDAO.updatePoint(point);
			t.commit();
		} catch (Exception e){
			t.rollback();
			System.out.println("Не удалось изменить данные клиента.");
			throw new IonException(e);
		} 		System.out.println("offline.key.public="+keys[1]);
		System.out.println("offline.key.private="+keys[0]);	
	}
}
