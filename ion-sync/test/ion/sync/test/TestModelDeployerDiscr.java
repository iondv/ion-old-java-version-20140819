package ion.sync.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.util.sync.ModelDeployer;
import ion.util.sync.SyncUtils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestModelDeployerDiscr {
	
	private static Connection connection;
	private static Statement statement;
	private static File tempDir = new File("./ModelDeployerDiscrTemporary");
	private static final String tablePrefix = "t"; // TODO: SyncUtils.tablePrefix
	private static final String columnPrefix = "f";
	private static final String USERNAME = "Test";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (tempDir.exists())
			clearFolder(tempDir);
		if(!tempDir.exists() || !tempDir.isDirectory())
			tempDir.mkdir();
	}
	
	@Before
	public void setUp() throws Exception {
		connection = DriverManager.getConnection("jdbc:h2:mem:test;DATABASE_TO_UPPER=false");
//		connection = DriverManager.getConnection("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1");
//		connection = DriverManager.getConnection("jdbc:mysql://localhost/test",
//				"root", "admin");
		statement = connection.createStatement();
	}

	@After
	public void tearDown() throws Exception {
		statement.close();
		connection.close();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {		
//		clearFolder(tempDir);
//		tempDir.delete();
	}
	
	private static void clearFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) {
	        for(File f: files) {
	            if(f.isDirectory())
	                clearFolder(f);
	            f.delete();
	        }
	    }
	}

	private ModelDeployer deployEnvironment(StoredClassMeta[] metas, boolean refIntegrity, boolean useDiscriminator) throws Exception {		
		clearFolder(tempDir);
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();			
		for(StoredClassMeta meta:metas){
			File mf = new File(tempDir, meta.name+".class.json");
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(mf),"utf-8");
			osw.write(gson.toJson(meta, StoredClassMeta.class));
			osw.close();
		}
		ModelDeployer deployer = new ModelDeployer(USERNAME, tempDir.getAbsolutePath(), tempDir.getAbsolutePath(), refIntegrity, null, useDiscriminator);
		deployer.Deploy(connection, null);
		return deployer;
	}

	private StoredClassMeta createMeta(final String className, final String ancestorName) {

		StoredClassMeta meta = new StoredClassMeta() {{
			this.name = className;
			this.key = "id";
			this.is_struct = false;
			this.ancestor = ancestorName;
		}};
		
		StoredPropertyMeta prop = new StoredPropertyMeta();
		prop.name = "id";
		prop.nullable = false;
		prop.type = MetaPropertyType.INT.getValue();
		prop.unique = true;

		meta.properties.add(prop);
		
		return meta;
	}
	
	@Test
	public void Deploy_AddingDiscriminator() throws Exception {
		final String root1 = "root1";
		final String child11 = "child1";
		final String child111 = "child111";
		final String child112 = "child112";
		final String child12 = "child12";
		final String discr = "_type";
		final String[] childs = new String[] {child11, child111, child112, child12};
		final String[] all = new String[] {root1, child11, child111, child112, child12};
		StoredClassMeta[] metas = new StoredClassMeta[] {
			createMeta(root1, null),
			createMeta(child11, root1),
			createMeta(child111, child11),
			createMeta(child112, child12),
			createMeta(child12, root1),
		};
		ModelDeployer deployer = deployEnvironment(metas, true, false);
		checkTableExists(all);
		checkColumn(all, discr, false, false);
		connection.commit();
//		tearDown();
//		setUp();
		//connection.commit();
//		unlockDb(); // костыль с неосвобождающейся блокировкой в in-memory бд
		deployEnvironment(metas, true, true);
//		deployer.setUseDiscriminator(true);
//		deployer.Deploy(connection, null);
		checkColumn(root1, discr, true, false);
		checkColumn(childs, discr, false, false);
	}
	
	private void unlockDb() throws SQLException {
		String sql = "UPDATE DATABASECHANGELOGLOCK SET LOCKED=FALSE, LOCKGRANTED=null, LOCKEDBY=null;";
//		String sql = "UPDATE DATABASECHANGELOGLOCK SET LOCKED=0, LOCKGRANTED=null, LOCKEDBY=null;";		
//		String sql = "DELETE FROM DATABASECHANGELOGLOCK;";
//		String sql = "DROP TABLE DATABASECHANGELOGLOCK;";
		statement.executeUpdate(sql);
		connection.commit();
	}
	
	private void checkColumn(String[] classNames, String propertyName, boolean exists, boolean addColumnPrefix) throws Exception {
		for (String classname: classNames)
			checkColumn(classname, propertyName, exists, addColumnPrefix);
	}

	private void checkColumn(String className, String propertyName, boolean exists, boolean addColumnPrefix) throws Exception {
		String tableName = SyncUtils.dbSanitiseName(className, tablePrefix);
		String columnName = addColumnPrefix?SyncUtils.dbSanitiseName(propertyName, columnPrefix):propertyName;
		String query= String.format("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='%s' AND COLUMN_NAME='%s'",
				tableName, columnName);
		ResultSet result = statement.executeQuery(query);
		if (exists) {
			assertTrue(String.format("не найден объект бд %s.%s", tableName, columnName), result.next());
			assertFalse("фантастика", result.next());
		} else {
			assertFalse(String.format("не ожидался объект бд %s.%s", tableName, columnName), result.next());
		}
	}
	
	private void checkTableExists(String[] classnames) throws Exception {
		for(String classname: classnames) {
			checkTableExists(classname);
		}
	}
	
	private void checkTableExists(String className) throws Exception {
		String e1TableName = SyncUtils.dbSanitiseName(className, "t"); // TODO: SyncUtils.tablePrefix 
		String query;
		ResultSet result;
		query= "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE in ('TABLE', 'BASE TABLE') AND TABLE_NAME='"+e1TableName+"'";
		result = statement.executeQuery(query);
		assertTrue(result.next());
		assertFalse(result.next());
	}
	
}
