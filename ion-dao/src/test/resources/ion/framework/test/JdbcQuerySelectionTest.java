package ion.framework.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;
import ion.core.IItem;
import ion.core.IonException;
import ion.core.mocks.MetaRepositoryMock;
import ion.framework.dao.jdbc.JdbcDataRepository;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class JdbcQuerySelectionTest {
	ApplicationContext	    appCtx;
	JdbcDataRepository	    dataRepository;
	DriverManagerDataSource	dataSource;
	MetaRepositoryMock	    metaRepository;
	TestAppConfig appConfig = new TestAppConfig();
	Map<String,Map<String, Object>> insertResults = new HashMap<String, Map<String,Object>>();
	
	@Before
	public void before() throws SQLException {
		appCtx = new AnnotationConfigApplicationContext(TestAppConfig.class);
		dataRepository = appCtx.getBean(JdbcDataRepository.class);
		dataSource = appCtx.getBean(DriverManagerDataSource.class);
		metaRepository = appCtx.getBean(MetaRepositoryMock.class);
		Connection connection = dataSource.getConnection();
    String createTableQuery = appConfig.tablesCreationQuery();
    Statement statement = connection.createStatement();
    statement.execute(createTableQuery);
    statement.close();
    
    insertResults.put("Dogs_1", appConfig.dogsCreationQuery(1, connection));
    insertResults.put("Dogs_2", appConfig.dogsCreationQuery(2, connection));
    insertResults.put("Cows_3", appConfig.cowsCreationQuery(3, connection));
    connection.close();
	}
	
	@Test
	public void testSelectionList() throws IonException, SQLException {
		Connection connection = dataSource.getConnection();
		for(int i=4; i<10; i++){
			insertResults.put("Dogs_"+i, appConfig.dogsCreationQuery(i, connection));
		}
    connection.close();
    
		String query = "FROM Dogs WHERE animalId in (:val1,:val2)";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("val1", 4);
		parameters.put("val2", 8);
		Collection<IItem> result = dataRepository.SelectionItems(query, parameters);
		Assert.assertEquals(2, result.size());
	}
	
	@Test
	public void testSelectionIterator() throws IonException, SQLException {
		Connection connection = dataSource.getConnection();
		for(int i=4; i<10; i++){
			insertResults.put("Dogs_"+i, appConfig.dogsCreationQuery(i, connection));
		}
    connection.close();
    
		String query = "FROM Dogs";
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		Iterator<IItem> iter = dataRepository.SelectionIterator(query, parameters);
    while(iter.hasNext()){
    	IItem i = iter.next();
    	System.out.println("#"+i.getClassName()+"@"+i.getItemId());
    	Assert.assertNotNull(i);
    }
	}
}
