package ion.framework.test.workflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import junit.framework.Assert;
import ion.core.IItem;
import ion.core.IWorkflowState;
import ion.core.IonException;
import ion.framework.dao.jdbc.JdbcDataRepository;
import ion.framework.dao.workflow.WorkFlowProvider;
import ion.framework.test.domain.Dogs;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class WorkflowProviderTest {
	ApplicationContext appCtx;
	WorkFlowProvider provider;
	DriverManagerDataSource	dataSource;
	JdbcDataRepository dataRepository;
	IItem testDog;
	WorkflowAppConfig config = new WorkflowAppConfig();
	
	@Before
	public void before() throws SQLException, IonException{
		appCtx = new AnnotationConfigApplicationContext(WorkflowAppConfig.class);
		provider = appCtx.getBean(WorkFlowProvider.class);
		dataSource = appCtx.getBean(DriverManagerDataSource.class);
		dataRepository = appCtx.getBean(JdbcDataRepository.class);
		
		Connection connection = dataSource.getConnection();
    config.createItemTables(connection);
    config.createDog(connection);
    connection.close();
    
    testDog = dataRepository.GetItem("Dogs", "1");
	}
	
	@Test
	public void getStateTest() throws IonException, SQLException{
		Connection connection = dataSource.getConnection();
		String query = "INSERT INTO `workflowtable` (`item`,`workflow`,`state`) VALUES (?,?,?);";
    PreparedStatement statement = connection.prepareStatement(query);
    statement.setString(1, "Dogs@1");
    statement.setString(2, "MammalsWF");
    statement.setString(3, "Newbie");
    statement.executeUpdate();
    statement.close();
    connection.close();
    
		IWorkflowState wfTest = provider.GetState(testDog);
		Assert.assertNotNull(wfTest);
	}
	
	@Test
	public void processTransitionTest() throws IonException{
		testDog.Set("age", 3);
		IWorkflowState wfTest = provider.ProcessTransition(testDog, "MammalsWF", "BecomeDenizen");
		
		Assert.assertNotNull(wfTest);
	}
	
}
