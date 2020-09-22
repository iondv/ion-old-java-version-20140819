//package ion.offline.tests;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.net.MalformedURLException;
//import java.util.Date;
//import java.util.Properties;
//
//import ion.core.IonException;
//import ion.framework.changelog.domain.StoredChangelogRecord;
//import ion.framework.offline.client.OfflineSyncEnvironment;
//
//import org.hibernate.Session;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//public class TestSyncManager {
//
//	OfflineSyncEnvironment environment;
//	Session se;
//	Object testItem;
//	String testClassName = "ion.domain.pm.MappingTest";
//	String propertyFileLocation = "src/test/daemon.properties";
//	String storageMetaLocation = "/home/inkz/Spring/ion_modeler/";
//	@Before
//	public void before() throws IonException, ClassNotFoundException, IOException{
//
//		Properties props = new Properties();
//		try {
//			File f = new File(propertyFileLocation);
//			if (!f.exists())
//				throw new IonException("Не найден конфигурационный файл!");
//			props.load(new FileInputStream(f));
//		} catch (IOException e) {
//			throw new IonException(e);
//		}
//		environment = new OfflineSyncEnvironment(props);
//		String[] dels = new String[1];
//		dels[0] = "yo";
//		environment.adjustStorageMeta(new File(storageMetaLocation), dels);
//		environment.reloadOrm();
//		//se = (Session) environment.begin();
//	}
//	
//	@Test
//	public void testSavingItem() throws IonException, MalformedURLException, ClassNotFoundException{
//		
//		try {
//			testItem = generateTestObject();
//		} catch (NoSuchMethodException | SecurityException
//				| InstantiationException | IllegalAccessException
//				| IllegalArgumentException | InvocationTargetException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		se.save(testItem);
//		Assert.assertNotNull(se.load(testClassName, 99));
//	}
//
//	
//	@After
//	public void after(){
//		se.delete(testItem);
//		//environment.commit(se);
//	}
//	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	private Object generateTestObject() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
////		ClassLoader cl = ClassLoader.getSystemClassLoader();
//		Class classForTest = StoredChangelogRecord.class.getClassLoader().loadClass(testClassName);
//		Method setIdMethod = classForTest.getMethod("setId", new Class[] { Integer.class });
//		Method setTestStroka = classForTest.getMethod("setTestStroka", new Class[] { String.class });
//		Method setTestCeloe = classForTest.getMethod("setTestCeloe", new Class[] { Integer.class });
//		Method setTestDeistvit = classForTest.getMethod("setTestDeistvit", new Class[] { Double.class });
//		Method setTestLog = classForTest.getMethod("setTestLog", new Class[] { Boolean.class });
//		Method setTestDatetime = classForTest.getMethod("setTestDatetime", new Class[] { Date.class });
//		
//		Object v = classForTest.newInstance();
//		
//		setIdMethod.invoke(v, new Object[] { 1 });
//		setTestStroka.invoke(v, new Object[] { "stroka1" });
//		setTestDeistvit.invoke(v, new Object[] { new Double(3.14) });
//		setTestLog.invoke(v, new Object[] { new Boolean(true) });
//		setTestDatetime.invoke(v, new Object[] { new Date() });
//		setTestCeloe.invoke(v, new Object[] { 5 });
//		return v;
//	}
//	
//}
