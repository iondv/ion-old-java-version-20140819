package ion.sync.test;

import ion.framework.meta.plain.StoredClassMeta;
import ion.util.sync.JPAEntityGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

public class TestMappingCreation {
	
	SessionFactory sessionFactory;
	Session session;
	String xmlFileLocation = "ion/test/MappingTest.hbm.xml";
	JPAEntityGenerator mappingGenerator = new JPAEntityGenerator();


	@SuppressWarnings({ "deprecation", "unused" })
	@Before
	public void before(){
		try {
			Gson gs = new Gson();
			Reader r = new InputStreamReader(new FileInputStream(new File("./test/ion/test/MappingTest.class.json")),"UTF-8");
			StoredClassMeta cm = gs.fromJson(r, StoredClassMeta.class);
			r.close();
			File destination = new File("./test/ion/domain/test/");
			destination.mkdirs();
			JPAEntityGenerator mappingGenerator = new JPAEntityGenerator();
			File classFile = mappingGenerator.generateSourceFile(cm, new File("./test/ion/test/"), destination, "ion.domain.test");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Configuration config = new Configuration()
			.addResource(xmlFileLocation)
			.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
			.setProperty("hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
			.setProperty("hibernate.current_session_context_class", "thread")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.hbm2ddl.auto", "create");
		  
		sessionFactory = config.buildSessionFactory();
		session = sessionFactory.getCurrentSession();
		session.beginTransaction();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void returnListOfEntries(){
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			Class classForTest = cl.loadClass("ion.domain.test.MappingTest");
			Method setIdMethod = classForTest.getMethod("setId", new Class[] { Integer.class });
			Method setTestStroka = classForTest.getMethod("setTestStroka", new Class[] { String.class });
			Method setTestCeloe = classForTest.getMethod("setTestCeloe", new Class[] { Integer.class });
			Method setTestDeistvit = classForTest.getMethod("setTestDeistvit", new Class[] { Double.class });
			Method setTestLog = classForTest.getMethod("setTestLog", new Class[] { Boolean.class });
			Method setTestDatetime = classForTest.getMethod("setTestDatetime", new Class[] { Date.class });
			
			Object v = classForTest.newInstance();
			Object y = classForTest.newInstance();
			
			setIdMethod.invoke(v, new Object[] { 1 });
			setIdMethod.invoke(y, new Object[] { 2 });
			setTestStroka.invoke(v, new Object[] { "stroka1" });
			setTestStroka.invoke(y, new Object[] { "stroka1" });
			setTestCeloe.invoke(y, new Object[] { 99 });
			setTestCeloe.invoke(y, new Object[] { 99 });
			setTestDeistvit.invoke(v, new Object[] { new Double(3.14) });
			setTestDeistvit.invoke(y, new Object[] { new Double(6.66) });
			setTestLog.invoke(v, new Object[] { new Boolean(true) });
			setTestLog.invoke(y, new Object[] { new Boolean(false) });
			setTestDatetime.invoke(v, new Object[] { new Date() });
			setTestDatetime.invoke(y, new Object[] { new Date() });
			session.save(v);
			session.save(y);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		  List result = session.createQuery("from MappingTest").list();
		  Assert.assertNotNull(result);
		  Assert.assertEquals(2, result.size());
	}
	
	@After
	public void after(){
		session.getTransaction().commit();
	}
	
}
