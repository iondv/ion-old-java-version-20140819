package ion.sync.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSorting;
import ion.util.sync.DBNameType;
import ion.util.sync.IDBNameProvider;
import ion.util.sync.IonDBNameProvider;
import ion.util.sync.ModelDeployer;

import org.h2.jdbc.JdbcSQLException;
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
public class TestModelDeployer {
	
	private Connection connection;
	private Statement statement;
	private static File tempDir = new File("./ModelDeployerTemporary");
	private ModelDeployer tested;
	private IDBNameProvider dbNames = new IonDBNameProvider(false);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if(!tempDir.exists() || !tempDir.isDirectory())
			tempDir.mkdir();
	}
	
	@Before
	public void setUp() throws Exception {
		try {
			connection = DriverManager.getConnection("jdbc:h2:mem:test;DATABASE_TO_UPPER=false");
		} catch(SQLException e) {
			throw new IonException(e);
		}
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			throw new IonException(e);
		}
	}

	@After
	public void tearDown() throws Exception {
		statement.close();
		connection.close();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {		
		clearFolder(tempDir);
		tempDir.delete();
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
	
	private void deployEnvironment(StoredClassMeta[] metas, boolean refIntegrity, Connection con, Integer defStrLen) throws IOException, IonException {		
		clearFolder(tempDir);
		Gson gson = new GsonBuilder().serializeNulls().create();			
		for(StoredClassMeta meta:metas){
			File mf = new File(tempDir, meta.name+".class.json");
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(mf),"utf-8");
			osw.write(gson.toJson(meta, StoredClassMeta.class));
			osw.close();
		}
		tested = new ModelDeployer("Test", tempDir.getAbsolutePath(), tempDir.getAbsolutePath(), refIntegrity);
		if(defStrLen != null)
			tested.setDefaultStringLength(defStrLen);
		try {
			tested.Deploy(con, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Object[][] Schema_Arrange(boolean refIntegr) throws IOException, IonException {
		StoredPropertyMeta essence1property1 = new StoredPropertyMeta("key1", "", MetaPropertyType.INT.getValue(), (short)10, (short)0, false, false, false, false, false, null);
		StoredPropertyMeta essence1property2 = new StoredPropertyMeta("field1", "", MetaPropertyType.REFERENCE.getValue(), (short)0);
		essence1property2.ref_class = "EssenceTwo";
		StoredClassMeta essence1 = new StoredClassMeta(false, "key1", "EssenceOne", "", "", Arrays.asList(new StoredPropertyMeta[]{essence1property1, essence1property2}));
		
		StoredPropertyMeta essence2property1 = new StoredPropertyMeta("key2", "", MetaPropertyType.INT.getValue(), (short)10, (short)0, false, false, false, false, false, null);
		StoredPropertyMeta essence2property2 = new StoredPropertyMeta("field2", "", MetaPropertyType.REFERENCE.getValue(), (short)0);
		essence2property2.ref_class = "EssenceOne";
		StoredClassMeta essence2 = new StoredClassMeta(false, "key2", "EssenceTwo", "", "", Arrays.asList(new StoredPropertyMeta[]{essence2property1, essence2property2}));
		
		deployEnvironment(new StoredClassMeta[]{essence1, essence2}, refIntegr, connection, null);
		return new Object[][]{
				new Object[]{essence1, essence1property1, essence1property2}, 
				new Object[]{essence2, essence2property1, essence2property2}
				};
	}
	
	@Test
	public void Schema_CheckTableExists() throws IOException, IonException, SQLException {
		Object[][] arrange = Schema_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		
		String e1TableName = dbNames.getName(e1.name, DBNameType.TABLE);
		String e2TableName = dbNames.getName(e2.name, DBNameType.TABLE);

		ResultSet result;
	
		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='TABLE' AND TABLE_NAME='"+e1TableName+"'");
		assertTrue(result.next());
		assertFalse(result.next());

		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='TABLE' AND TABLE_NAME='"+e2TableName+"'");
		assertTrue(result.next());
		assertFalse(result.next());
	}
	
	@Test
	public void Schema_CheckFieldsExists() throws IOException, IonException, SQLException {
		Object[][] arrange = Schema_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0];
		StoredPropertyMeta e1p1 = (StoredPropertyMeta)arrange[0][1];
		StoredPropertyMeta e1p2 = (StoredPropertyMeta)arrange[0][2];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		StoredPropertyMeta e2p1 = (StoredPropertyMeta)arrange[1][1];
		StoredPropertyMeta e2p2 = (StoredPropertyMeta)arrange[1][2];
		
		String e1TableName = dbNames.getName(e1.name, DBNameType.TABLE);
		String e1p1FieldName = dbNames.getName(e1p1.name, DBNameType.COLUMN);
		String e1p2FieldName = dbNames.getName(e1p2.name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(e2.name, DBNameType.TABLE);
		String e2p1FieldName = dbNames.getName(e2p1.name, DBNameType.COLUMN);
		String e2p2FieldName = dbNames.getName(e2p2.name, DBNameType.COLUMN);
		ResultSet result;

		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+e1TableName+"' AND COLUMN_NAME='"+e1p1FieldName+"'");
		assertTrue(result.next());
		assertFalse(result.next());

		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+e1TableName+"' AND COLUMN_NAME='"+e1p2FieldName+"'");
		assertTrue(result.next());
		assertFalse(result.next());

		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+e2TableName+"' AND COLUMN_NAME='"+e2p1FieldName+"'");
		assertTrue(result.next());
		assertFalse(result.next());

		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+e2TableName+"' AND COLUMN_NAME='"+e2p2FieldName+"'");
		assertTrue(result.next());
		assertFalse(result.next());
	}
	
	@Test
	public void Schema_CheckPrimaryKey() throws IOException, IonException, SQLException {
		Object[][] arrange = Schema_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0];
		StoredPropertyMeta e1p1 = (StoredPropertyMeta)arrange[0][1];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		StoredPropertyMeta e2p1 = (StoredPropertyMeta)arrange[1][1];
		
		String e1TableName = dbNames.getName(e1.name, DBNameType.TABLE);
		String e1p1FieldName = dbNames.getName(e1p1.name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(e2.name, DBNameType.TABLE);
		String e2p1FieldName = dbNames.getName(e2p1.name, DBNameType.COLUMN);	
		String query;
		ResultSet result;
		
		query= "SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e1TableName+"' AND C.CONSTRAINT_TYPE='PRIMARY KEY'";
		result = statement.executeQuery(query);
		assertTrue(result.next());
		assertEquals(e1p1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
		
		query= "SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e2TableName+"' AND C.CONSTRAINT_TYPE='PRIMARY KEY'";
		result = statement.executeQuery(query);
		assertTrue(result.next());
		assertEquals(e2p1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
	}
		
	@Test
	public void Schema_CheckForeignKeys_NoReferentialIntegrity() throws IOException, IonException, SQLException {
		Object[][] arrange = Schema_Arrange(false);
		
		String e1TableName = dbNames.getName(((StoredClassMeta)arrange[0][0]).name, DBNameType.TABLE);
		String p1FieldName = dbNames.getName(((StoredPropertyMeta)arrange[0][2]).name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(((StoredClassMeta)arrange[1][0]).name, DBNameType.TABLE);
		String p2FieldName = dbNames.getName(((StoredPropertyMeta)arrange[1][2]).name, DBNameType.COLUMN);
		
		ResultSet result;
		
		result = statement.executeQuery("SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e1TableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+p1FieldName+"'");
		assertFalse(result.next());
		
		result = statement.executeQuery("SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e2TableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+p2FieldName+"'");
		assertFalse(result.next());
	}
	
	@Test
	public void Schema_CheckForeignKeys_ReferentialIntegrity() throws IOException, IonException, SQLException {
		Object[][] arrange = Schema_Arrange(true);
		
		String e1TableName = dbNames.getName(((StoredClassMeta)arrange[0][0]).name, DBNameType.TABLE);
		String k1FieldName = dbNames.getName(((StoredPropertyMeta)arrange[0][1]).name, DBNameType.COLUMN);
		String p1FieldName = dbNames.getName(((StoredPropertyMeta)arrange[0][2]).name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(((StoredClassMeta)arrange[1][0]).name, DBNameType.TABLE);
		String k2FieldName = dbNames.getName(((StoredPropertyMeta)arrange[1][1]).name, DBNameType.COLUMN);
		String p2FieldName = dbNames.getName(((StoredPropertyMeta)arrange[1][2]).name, DBNameType.COLUMN);
		
		ResultSet result;
		
		result = statement.executeQuery("SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e1TableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+p1FieldName+"'");
		assertTrue(result.next());
		assertEquals(e2TableName, result.getString("TABLE_NAME"));
		assertEquals(k2FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
		
		result = statement.executeQuery("SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e2TableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+p2FieldName+"'");
		assertTrue(result.next());
		assertEquals(e1TableName, result.getString("TABLE_NAME"));
		assertEquals(k1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
	}
	
	private StoredClassMeta Columns_Arrange(Collection<StoredPropertyMeta> properties) throws IOException, IonException{
		StoredPropertyMeta key = new StoredPropertyMeta("key", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		properties.add(key);
		StoredClassMeta essence = new StoredClassMeta(false, "key", "EssenceOne", "", "", properties);
	
		deployEnvironment(new StoredClassMeta[]{essence}, false, connection, null);
		return essence;
	}
			
	@Test
	public void Columns_CheckOrder() throws SQLException, IOException, IonException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta p2 = new StoredPropertyMeta("p2", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta p3 = new StoredPropertyMeta("p3", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta p4 = new StoredPropertyMeta("p4", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta p5 = new StoredPropertyMeta("p5", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		p2.order_number = 155;
		p3.order_number = 1;
		p4.order_number = 23;
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		ps.add(p2);
		ps.add(p3);
		ps.add(p4);
		ps.add(p5);
		StoredClassMeta e = Columns_Arrange(ps);
						
		String query;
		ResultSet result;
		
		Collections.sort(ps);
		int order = 1;
		for(StoredPropertyMeta pm : ps){
			query = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS"
					+ " WHERE TABLE_NAME='"+dbNames.getName(e.name, DBNameType.TABLE)+"'"
					+ " AND COLUMN_NAME='"+dbNames.getName(pm.name, DBNameType.COLUMN)+"'";
			result = statement.executeQuery(query);
			assertTrue(result.next());
			assertEquals(order, result.getInt("ORDINAL_POSITION"));
			assertFalse(result.next());
			order++;
		}	
	}
	
	@Test
	public void Columns_CheckDefaultValue() throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		p1.default_value = "23";
		StoredPropertyMeta p2 = new StoredPropertyMeta("p2", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		ps.add(p2);
		StoredClassMeta e = Columns_Arrange(ps);
				
		String eTableName = dbNames.getName(e.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(e.key, DBNameType.COLUMN);
		
		String query;
		ResultSet result;
		
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+") VALUES(1)");
		query = "SELECT * FROM "+eTableName+" WHERE "+keyFieldName+"=1";
		result = statement.executeQuery(query);
		assertTrue(result.next());
		assertEquals(p1.default_value, result.getString(dbNames.getName(p1.name, DBNameType.COLUMN)));
		assertEquals(p2.default_value, result.getString(dbNames.getName(p2.name, DBNameType.COLUMN)));
		assertFalse(result.next());
		
		
	}
	
	@Test(expected=JdbcSQLException.class)
	public void Columns_CheckNullable() throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		p1.nullable = false;
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		StoredClassMeta e = Columns_Arrange(ps);
		
		statement.execute("INSERT INTO "+dbNames.getName(e.name, DBNameType.TABLE)+"("+dbNames.getName(e.key, DBNameType.COLUMN)+")"
				+ " VALUES(1)");
	}
	
	@Test(expected=JdbcSQLException.class)
	public void Columns_CheckUnique() throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		p1.unique = true;
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		StoredClassMeta e = Columns_Arrange(ps);
		
		String eTableName = dbNames.getName(e.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(e.key, DBNameType.COLUMN);
		String uniqueFieldName = dbNames.getName(p1.name, DBNameType.COLUMN);
		
		int v = 23;
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+uniqueFieldName+") VALUES(1,"+v+")");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+uniqueFieldName+") VALUES(2,"+v+")");
	}

	@Test
	public void Columns_CheckAutoassigned() throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		p1.autoassigned = true;
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		StoredClassMeta e = Columns_Arrange(ps);
		
		String eTableName = dbNames.getName(e.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(e.key, DBNameType.COLUMN);
		String p1FieldName = dbNames.getName(p1.name, DBNameType.COLUMN);

		ResultSet result;
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+") VALUES(10)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+") VALUES(20)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+") VALUES(30)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+") VALUES(40)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+") VALUES(50)");
		for(int i=1; i<6; i++){
			result = statement.executeQuery("SELECT * FROM "+eTableName+" WHERE "+keyFieldName+"="+i*10);			
			assertTrue(result.next());
			assertEquals(i, result.getInt(p1FieldName));
			assertFalse(result.next());
		}
	}
	
	private static String test_string = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam nisi nisi, semper sit amet tellus vitae, tincidunt ornare arcu."
			+ " Morbi fermentum lorem sed ipsum aliquam pellentesque. Duis feugiat lobortis nunc quis gravida. Proin molestie nunc sit amet cursus convallis."
			+ " Sed molestie orci quis lacinia faucibus. In hac habitasse platea dictumst. Aliquam gravida felis vel quam pretium, id vehicula neque malesuada."
			+ " Etiam interdum leo vitae vestibulum convallis. Sed viverra feugiat efficitur. Class aptent taciti sociosqu ad litora torquent per conubia nostra,"
			+ " per inceptos himenaeos. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Duis sed lacinia erat. "
			+ "Pellentesque nec arcu ullamcorper, vestibulum erat a, laoreet tellus. Pellentesque ullamcorper ante vel tincidunt gravida. "
			+ "Nunc vitae nibh vestibulum felis faucibus venenatis in in nisi. Duis efficitur, enim nec fringilla bibendum, mauris elit ullamcorper ipsum, "
			+ "non ultrices purus ipsum id metus. Donec condimentum ligula nec viverra iaculis. Aliquam quis orci turpis. Vivamus eget dolor nibh. Ut auctor,"
			+ " purus elementum volutpat pretium, magna nisl tristique neque, sit amet pellentesque leo libero lacinia erat. Praesent tellus risus, "
			+ "viverra venenatis efficitur id, commodo vitae enim. Proin vel lacinia arcu, iaculis facilisis urna. Vestibulum convallis urna a tortor tincidunt commodo."
			+ " Curabitur gravida dui lacus, non iaculis libero dignissim in. Maecenas maximus bibendum justo, sed gravida sem facilisis ut. Morbi ac urna finibus, "
			+ "mollis nibh eu, dapibus nisi. Duis dignissim, neque id rhoncus ornare, elit enim sollicitudin nunc, at elementum magna mauris in turpis.";
	
	private void check_strings(MetaPropertyType t) throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", t.getValue(), (short)10, (short)0);
		StoredPropertyMeta p2 = new StoredPropertyMeta("p2", "", t.getValue(), (short)100, (short)0);
		StoredPropertyMeta p3 = new StoredPropertyMeta("p3", "", t.getValue(), (short)0, (short)0);
		StoredPropertyMeta key = new StoredPropertyMeta("key", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredClassMeta essence = new StoredClassMeta(false, "key", "EssenceOne", "", "", Arrays.asList(new StoredPropertyMeta[]{key, p1, p2, p3}));
	
		deployEnvironment(new StoredClassMeta[]{essence}, false, connection, 250);
		
		String eTableName = dbNames.getName(essence.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(essence.key, DBNameType.COLUMN);
		String p1FieldName = dbNames.getName(p1.name, DBNameType.COLUMN);
		String p2FieldName = dbNames.getName(p2.name, DBNameType.COLUMN);
		String p3FieldName = dbNames.getName(p3.name, DBNameType.COLUMN);
				
		ResultSet result;			
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+","+p2FieldName+","+p3FieldName+") VALUES(1,'"+test_string.substring(0, 10)+"', '"+test_string.substring(0, 100)+"','"+test_string.substring(0, 250)+"')");
		result = statement.executeQuery("SELECT * FROM "+eTableName+" WHERE "+keyFieldName+"=1");
		assertTrue(result.next());
		assertEquals(test_string.substring(0, 10), result.getString(p1FieldName));
		assertEquals(test_string.substring(0, 100), result.getString(p2FieldName));
		assertEquals(test_string.substring(0, 250), result.getString(p3FieldName));
		assertFalse(result.next());
	}
	
	@Test
	public void Columns_CheckType_STRING() throws IOException, IonException, SQLException {
		check_strings(MetaPropertyType.STRING);
	}
	@Test
	public void Columns_CheckType_FILE() throws IOException, IonException, SQLException {
		check_strings(MetaPropertyType.FILE);
	}
	@Test
	public void Columns_CheckType_IMAGE() throws IOException, IonException, SQLException {
		check_strings(MetaPropertyType.IMAGE);
	}
	@Test
	public void Columns_CheckType_PASSWORD() throws IOException, IonException, SQLException {
		check_strings(MetaPropertyType.PASSWORD);
	}
	@Test
	public void Columns_CheckType_URL() throws IOException, IonException, SQLException {
		check_strings(MetaPropertyType.URL);
	}
	
	private void check_texts(MetaPropertyType t) throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", t.getValue(), (short)0, (short)0);
		StoredPropertyMeta p2 = new StoredPropertyMeta("p2", "", t.getValue(), (short)10, (short)0);
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		ps.add(p2);
		StoredClassMeta e = Columns_Arrange(ps);
		
		String eTableName = dbNames.getName(e.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(e.key, DBNameType.COLUMN);
		String p1FieldName = dbNames.getName(p1.name, DBNameType.COLUMN);
		String p2FieldName = dbNames.getName(p2.name, DBNameType.COLUMN);	
		
		ResultSet result;			
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+","+p2FieldName+") VALUES(1,'"+test_string+"', '"+test_string+"')");
		result = statement.executeQuery("SELECT * FROM "+eTableName+" WHERE "+keyFieldName+"=1");
		assertTrue(result.next());
		assertEquals(test_string, result.getString(p1FieldName));
		assertEquals(test_string, result.getString(p2FieldName));
		assertFalse(result.next());
	}
	
	@Test
	public void Columns_CheckType_HTML() throws IOException, IonException, SQLException {
		check_texts(MetaPropertyType.HTML);
	}
	
	@Test
	public void Columns_CheckType_TEXT() throws IOException, IonException, SQLException {
		check_texts(MetaPropertyType.TEXT);
	}
	
	@Test
	public void Columns_CheckType_STRUCT_structCOLLECTION() throws IOException, IonException, SQLException {
		StoredPropertyMeta struct_key = new StoredPropertyMeta("skey", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta struct_p1 = new StoredPropertyMeta("sp1", "", MetaPropertyType.STRING.getValue(), (short)10, (short)0);
		StoredPropertyMeta struct_p2 = new StoredPropertyMeta("sp2", "", MetaPropertyType.DECIMAL.getValue(), (short)10, (short)2);
		StoredClassMeta struct = new StoredClassMeta(true, "key", "Structure", "", "", Arrays.asList(new StoredPropertyMeta[]{struct_key, struct_p1, struct_p2}));
		
		StoredPropertyMeta key = new StoredPropertyMeta("key", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.STRUCT.getValue(), (short)0, (short)0);
		p1.ref_class = "Structure";
		StoredPropertyMeta p2 = new StoredPropertyMeta("p2", "", MetaPropertyType.STRUCT.getValue(), (short)10, (short)0);
		p2.ref_class = "Structure";
		StoredPropertyMeta p3 = new StoredPropertyMeta("p3", "", MetaPropertyType.COLLECTION.getValue(), (short)0, (short)0);
		p3.items_class = "Structure";
		StoredPropertyMeta p4 = new StoredPropertyMeta("p4", "", MetaPropertyType.COLLECTION.getValue(), (short)10, (short)0);
		p4.items_class = "Structure";
		StoredClassMeta essence = new StoredClassMeta(false, "key", "EssenceOne", "", "", Arrays.asList(new StoredPropertyMeta[]{key, p1, p2, p3, p4}));
	
		deployEnvironment(new StoredClassMeta[]{essence, struct}, false, connection, null);
		
		String eTableName = dbNames.getName(essence.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(essence.key, DBNameType.COLUMN);
		String p1FieldName = dbNames.getName(p1.name, DBNameType.COLUMN);
		String p2FieldName = dbNames.getName(p2.name, DBNameType.COLUMN);
		String p3FieldName = dbNames.getName(p3.name, DBNameType.COLUMN);
		String p4FieldName = dbNames.getName(p4.name, DBNameType.COLUMN);
		
		ResultSet result;			
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+","+p2FieldName+","+p3FieldName+","+p4FieldName+")"
				+ " VALUES(1,'"+test_string+"', '"+test_string+"', '"+test_string+"', '"+test_string+"')");
		result = statement.executeQuery("SELECT * FROM "+eTableName+" WHERE "+keyFieldName+"=1");
		assertTrue(result.next());
		assertEquals(test_string, result.getString(p1FieldName));
		assertEquals(test_string, result.getString(p2FieldName));
		assertEquals(test_string, result.getString(p3FieldName));
		assertEquals(test_string, result.getString(p4FieldName));
		assertFalse(result.next());
	}
	
	@Test
	public void Columns_CheckType_INT() {
		assertTrue(false);
	}
	
	@Test
	public void Columns_CheckType_REAL() {
		assertTrue(false);
	}
	
	@Test
	public void Columns_CheckType_DECIMAL() {
		assertTrue(false);
	}
	
	@Test
	public void Columns_CheckType_DATETIME() {
		assertTrue(false);
	}
	
	@Test
	public void Columns_CheckType_BOOLEAN() throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.BOOLEAN.getValue(), (short)0, (short)0);
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		StoredClassMeta e = Columns_Arrange(ps);
		
		String eTableName = dbNames.getName(e.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(e.key, DBNameType.COLUMN);
		String p1FieldName = dbNames.getName(p1.name, DBNameType.COLUMN);
		
		ResultSet result;		
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+") VALUES(1, TRUE)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+") VALUES(2, FALSE)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+") VALUES(3, NULL)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+") VALUES(4, 0)");
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+") VALUES(5, 1)");
		result = statement.executeQuery("SELECT * FROM "+eTableName);
		assertTrue(result.next());
		assertTrue(result.getBoolean(p1FieldName));
		result.next();
		assertFalse(result.getBoolean(p1FieldName));
		result.next();
		assertNull(result.getObject(p1FieldName));
		result.next();
		assertFalse(result.getBoolean(p1FieldName));
		result.next();
		assertTrue(result.getBoolean(p1FieldName));
		assertFalse(result.next());
		
	}
	
	@Test
	public void Columns_CheckType_GUID() throws IOException, IonException, SQLException {
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.GUID.getValue(), (short)0, (short)0);
		List<StoredPropertyMeta> ps = new ArrayList<StoredPropertyMeta>();
		ps.add(p1);
		StoredClassMeta e = Columns_Arrange(ps);
		
		String eTableName = dbNames.getName(e.name, DBNameType.TABLE);
		String keyFieldName = dbNames.getName(e.key, DBNameType.COLUMN);
		String p1FieldName = dbNames.getName(p1.name, DBNameType.COLUMN);
				
		ResultSet result;
		UUID value = UUID.randomUUID();
		statement.execute("INSERT INTO "+eTableName+"("+keyFieldName+","+p1FieldName+") VALUES(1,'"+value.toString()+"')");
		result = statement.executeQuery("SELECT * FROM "+eTableName+" WHERE "+keyFieldName+"=1");
		assertTrue(result.next());
		assertEquals(value, UUID.fromString(result.getString(p1FieldName)));
		assertFalse(result.next());
	}
	
	@Test
	public void Columns_CheckType_REFERENCE() throws SQLException, IOException, IonException {
		StoredPropertyMeta ref_key = new StoredPropertyMeta("rkey", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta ref_p1 = new StoredPropertyMeta("rp1", "", MetaPropertyType.STRING.getValue(), (short)0, (short)0);
		StoredClassMeta ref_class = new StoredClassMeta(false, "rkey", "ReferenceClass", "", "", Arrays.asList(new StoredPropertyMeta[]{ref_key, ref_p1}));
		
		StoredPropertyMeta key = new StoredPropertyMeta("key", "", MetaPropertyType.INT.getValue(), (short)10, (short)0);
		StoredPropertyMeta p1 = new StoredPropertyMeta("p1", "", MetaPropertyType.REFERENCE.getValue(), (short)0, (short)0);
		p1.ref_class = "ReferenceClass";
		StoredClassMeta essence = new StoredClassMeta(false, "key", "EssenceOne", "", "", Arrays.asList(new StoredPropertyMeta[]{key, p1}));
	
		deployEnvironment(new StoredClassMeta[]{essence, ref_class}, false, connection, null);
		
		String refTableName = dbNames.getName(ref_class.name, DBNameType.TABLE);
		String refkeyFieldName = dbNames.getName(ref_key.name, DBNameType.COLUMN);
		String eTableName = dbNames.getName(essence.name, DBNameType.TABLE);
		String p1FieldName = dbNames.getName(p1.name, DBNameType.COLUMN);
				
		ResultSet result1 = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS"
				+ " WHERE TABLE_NAME='"+refTableName+"' AND COLUMN_NAME='"+refkeyFieldName+"'");
		assertTrue(result1.next());
		int type = result1.getInt("DATA_TYPE");
		int size = result1.getInt("CHARACTER_MAXIMUM_LENGTH");
		assertFalse(result1.next());
		ResultSet result2 = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS"
				+ " WHERE TABLE_NAME='"+eTableName+"' AND COLUMN_NAME='"+p1FieldName+"'");		
		assertTrue(result2.next());
		assertEquals(type, result2.getInt("DATA_TYPE"));
		assertEquals(size, result2.getInt("CHARACTER_MAXIMUM_LENGTH"));
		assertFalse(result2.next());
	}
	
	@Test
	public void Columns_CheckType_SET() {
		assertTrue(false);
	}
	
	@Test
	public void Columns_CheckType_USER() {
		assertTrue(false);
	}
	
	private Object[][] ManyToMany_Arrange(boolean refIntegr) throws IOException, IonException {
		//EssenceOne
		/*
		StoredPropertyMeta essence1property1 = new StoredPropertyMeta("key", "", MetaPropertyType.INT.getValue(), (short)10, (short)0, false, false, false, false, false, null);
		StoredPropertyMeta essence1property2 = new StoredPropertyMeta(10, "collection", "", MetaPropertyType.COLLECTION.getValue(), (short)0, (short)0, 
				false, false, false, false, false, "","","EssenceTwo","","","", 
				new ArrayList<StoredCondition>(), new ArrayList<StoredSorting>(), false, false);
		StoredClassMeta essence1 = new StoredClassMeta(false, "key", "EssenceOne", "", "", Arrays.asList(new StoredPropertyMeta[]{essence1property1, essence1property2}));
		*/
		//EssenceTwo
		/*
		StoredPropertyMeta essence2property1 = new StoredPropertyMeta("key", "", MetaPropertyType.INT.getValue(), (short)10, (short)0, false, false, false, false, false, null);
		StoredPropertyMeta essence2property2 = new StoredPropertyMeta(10, "collection", "", MetaPropertyType.COLLECTION.getValue(), (short)0, (short)0, 
				false, false, false, false, false, "","","EssenceOne","","","", 
				new ArrayList<StoredCondition>(), new ArrayList<StoredSorting>(), false, false);
		StoredClassMeta essence2 = new StoredClassMeta(false, "key", "EssenceTwo", "", "", Arrays.asList(new StoredPropertyMeta[]{essence2property1,essence2property2}));

		deployEnvironment(new StoredClassMeta[]{essence1, essence2}, refIntegr, connection, null);
		return new Object[][]{
				new Object[]{essence1, essence1property1, essence1property2},
				new Object[]{essence2, essence2property1, essence2property2}
				};
				*/
		return null;
	}
	
	@Test
	public void ManyToMany_CheckRelTableExists() throws IOException, IonException, SQLException {
		Object[][] arrange = ManyToMany_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];		
		
		String relTableName = dbNames.getName(dbNames.sortWords(new String[]{e1.name, e2.name}), DBNameType.TABLE);		
		String query = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='TABLE' AND TABLE_NAME='"+relTableName+"'";
		ResultSet result = statement.executeQuery(query);
		assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
		assertFalse("Скрипт <<"+query+">> вернул слишком много результатов.", result.next());
	}
	
	@Test
	public void ManyToMany_CheckRelFieldsExists() throws IOException, IonException, SQLException {
		Object[][] arrange = ManyToMany_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		
		String relTableName = dbNames.getName(dbNames.sortWords(new String[]{e1.name, e2.name}), DBNameType.TABLE);
		String relFieldName_e1 = dbNames.getName(e1.name, DBNameType.COLUMN);
		String relFieldName_e2 = dbNames.getName(e2.name, DBNameType.COLUMN);		
		String query;
		ResultSet result;
		
		query= "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+relTableName+"' AND COLUMN_NAME='"+relFieldName_e1+"'";
		result = statement.executeQuery(query);
		assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
		assertFalse("Скрипт <<"+query+">> вернул слишком много результатов.", result.next());
		
		query= "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+relTableName+"' AND COLUMN_NAME='"+relFieldName_e2+"'";
		result = statement.executeQuery(query);
		assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
		assertFalse("Скрипт <<"+query+">> вернул слишком много результатов.", result.next());
	}

	@Test
	public void ManyToMany_CheckForeignKeys_NoReferentialIntegrity() throws IOException, IonException, SQLException {
		Object[][] arrange = ManyToMany_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		
		String relTableName = dbNames.getName(dbNames.sortWords(new String[]{e1.name, e2.name}), DBNameType.TABLE);	
		
		String query;
		ResultSet result;		
		
		query= "SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+relTableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL'";
		result = statement.executeQuery(query);
		assertFalse("Скрипт <<"+query+">> вернул слишком много результатов.", result.next());
	}
	
	@Test
	public void ManyToMany_CheckForeignKeys_ReferentialIntegrity() throws IOException, IonException, SQLException {
		Object[][] arrange = ManyToMany_Arrange(true);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0]; 
		StoredPropertyMeta e1p1 = (StoredPropertyMeta)arrange[0][1];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		StoredPropertyMeta e2p1 = (StoredPropertyMeta)arrange[1][1];
		
		String e1TableName = dbNames.getName(e1.name, DBNameType.TABLE);
		String e1p1FieldName = dbNames.getName(e1p1.name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(e2.name, DBNameType.TABLE);
		String e2p1FieldName = dbNames.getName(e2p1.name, DBNameType.COLUMN);
		String relTableName = dbNames.getName(dbNames.sortWords(new String[]{e1.name, e2.name}), DBNameType.TABLE);
		String relFieldName_e1 = dbNames.getName(e1.name, DBNameType.COLUMN);
		String relFieldName_e2 = dbNames.getName(e2.name, DBNameType.COLUMN);		
		
		String query;
		ResultSet result;		
		
		query= "SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+relTableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+relFieldName_e1+"'";
		result = statement.executeQuery(query);
		assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
		assertEquals("Результат скрипта <<"+query+">> не соответсвтвует ожиданиям.", e1TableName, result.getString("TABLE_NAME"));
		assertEquals("Результат скрипта <<"+query+">> не соответсвтвует ожиданиям.", e1p1FieldName, result.getString("COLUMN_NAME"));
		assertFalse("Скрипт <<"+query+">> вернул больше одного результата.", result.next());
		
		query= "SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+relTableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+relFieldName_e2+"'";
		result = statement.executeQuery(query);
		assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
		assertEquals("Результат скрипта <<"+query+">> не соответсвтвует ожиданиям.", e2TableName, result.getString("TABLE_NAME"));
		assertEquals("Результат скрипта <<"+query+">> не соответсвтвует ожиданиям.", e2p1FieldName, result.getString("COLUMN_NAME"));
		assertFalse("Скрипт <<"+query+">> вернул больше одного результата.", result.next());
	}
	
	@Test
	public void ManyToMany_CheckPrimaryKey() throws IOException, IonException, SQLException {
		Object[][] arrange = ManyToMany_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		
		String relTableName = dbNames.getName(dbNames.sortWords(new String[]{e1.name, e2.name}), DBNameType.TABLE);
		String relFieldName_e1 = dbNames.getName(e1.name, DBNameType.COLUMN);
		String relFieldName_e2 = dbNames.getName(e2.name, DBNameType.COLUMN);		
		
		String query;
		ResultSet result;
		
		query= "SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+relTableName+"' AND C.CONSTRAINT_TYPE='PRIMARY KEY' AND COLUMN_NAME='"+relFieldName_e1+"'";
		result = statement.executeQuery(query);
		assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
		assertFalse("Скрипт <<"+query+">> вернул больше одного результата.", result.next());
		
		query= "SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+relTableName+"' AND C.CONSTRAINT_TYPE='PRIMARY KEY' AND COLUMN_NAME='"+relFieldName_e2+"'";
		result = statement.executeQuery(query);
		assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
		assertFalse("Скрипт <<"+query+">> вернул больше одного результата.", result.next());
	}
	
	@Test
	public void ManyToMany_CheckInsert() throws IOException, IonException, SQLException {
		Object[][] arrange = ManyToMany_Arrange(false);
		StoredClassMeta e1 = (StoredClassMeta)arrange[0][0]; 
		StoredPropertyMeta e1p1 = (StoredPropertyMeta)arrange[0][1];
		StoredClassMeta e2 = (StoredClassMeta)arrange[1][0];
		StoredPropertyMeta e2p1 = (StoredPropertyMeta)arrange[1][1];
		
		String e1TableName = dbNames.getName(e1.name, DBNameType.TABLE);
		String e1p1FieldName = dbNames.getName(e1p1.name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(e2.name, DBNameType.TABLE);
		String e2p1FieldName = dbNames.getName(e2p1.name, DBNameType.COLUMN);
		String relTableName = dbNames.getName(dbNames.sortWords(new String[]{e1.name, e2.name}), DBNameType.TABLE);
		String relFieldName_e1 = dbNames.getName(e1.name, DBNameType.COLUMN);
		String relFieldName_e2 = dbNames.getName(e2.name, DBNameType.COLUMN);
		
		int[] e1Keys = {10,11,12,13,14};
		int[] e2Keys = {20,21,22,23,24};		
		for(int i = 0; i < 5; i++){
			statement.execute("INSERT INTO "+e1TableName+"("+e1p1FieldName+") VALUES("+e1Keys[i]+")");
			statement.execute("INSERT INTO "+e2TableName+"("+e2p1FieldName+") VALUES("+e2Keys[i]+")");
		}		
		int[][] relationships = {{e1Keys[0], e2Keys[0]}, 
								{e1Keys[0], e2Keys[1]}, 
								{e1Keys[1],e2Keys[1]}, 
								{e1Keys[1],e2Keys[2]},
								{e1Keys[2],e2Keys[1]},
								{e1Keys[2],e2Keys[2]},
								{e1Keys[2],e2Keys[3]},
								{e1Keys[4],e2Keys[4]}};
		for(int[] rel:relationships)
			statement.execute("INSERT INTO "+relTableName+"("+relFieldName_e1+","+relFieldName_e2+") VALUES("+rel[0]+","+rel[1]+")");
		
		String query;
		ResultSet result;
		
		for(int i = 0; i < 5; i++){
			int e1_relCount = 0;
			int e2_relCount = 0;
			for(int[] rel:relationships){
				if(rel[0] == e1Keys[i])
					e1_relCount++;
				if(rel[1] == e2Keys[i])
					e2_relCount++;
			}
			
			query = "SELECT COUNT(*) AS C FROM "+e1TableName+" AS E1 INNER JOIN "+relTableName+" AS RT ON E1."+e1p1FieldName+" = RT."+relFieldName_e1
					+" INNER JOIN "+e2TableName+" AS E2 ON E2."+e2p1FieldName+" = RT."+relFieldName_e2
					+" WHERE E1."+e1p1FieldName+" = "+e1Keys[i];
			result = statement.executeQuery(query);
			assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
			assertEquals("Результат скрипта <<"+query+">> не соответсвтвует ожиданиям.", e1_relCount, result.getInt("C"));
			assertFalse("Скрипт <<"+query+">> вернул больше одного результата.", result.next());
			
			query = "SELECT COUNT(*) AS C FROM "+e1TableName+" AS E1 INNER JOIN "+relTableName+" AS RT ON E1."+e1p1FieldName+" = RT."+relFieldName_e1
					+" INNER JOIN "+e2TableName+" AS E2 ON E2."+e2p1FieldName+" = RT."+relFieldName_e2
					+" WHERE E2."+e2p1FieldName+" = "+e2Keys[i];
			result = statement.executeQuery(query);
			assertTrue("Скрипт <<"+query+">> не вернул результатов.", result.next());
			assertEquals("Результат скрипта <<"+query+">> не соответсвтвует ожиданиям.", e2_relCount, result.getInt("C"));
			assertFalse("Скрипт <<"+query+">> вернул больше одного результата.", result.next());
		}		
	}
	
	private Object[][] Inheritance_Arrange(boolean refIntegr) throws IOException, IonException {
		StoredPropertyMeta essence1property1 = new StoredPropertyMeta("key1", "", MetaPropertyType.INT.getValue(), (short)10);
		StoredPropertyMeta essence1property2 = new StoredPropertyMeta("field1", "", MetaPropertyType.STRING.getValue(), (short)0);
		StoredClassMeta essence1 = new StoredClassMeta(false, "key1", "EssenceOne", "", "", Arrays.asList(new StoredPropertyMeta[]{essence1property1, essence1property2}));
		
		StoredPropertyMeta essence2property1 = new StoredPropertyMeta("key2", "", MetaPropertyType.INT.getValue(), (short)10);
		StoredPropertyMeta essence2property2 = new StoredPropertyMeta("field2", "", MetaPropertyType.STRING.getValue(), (short)0);
		StoredClassMeta essence2 = new StoredClassMeta(false, "key2", "EssenceTwo", "", "", "EssenceOne", Arrays.asList(new StoredPropertyMeta[]{essence2property1, essence2property2}));
				
		StoredPropertyMeta essence3property1 = new StoredPropertyMeta("key3", "", MetaPropertyType.INT.getValue(), (short)10);
		StoredPropertyMeta essence3property2 = new StoredPropertyMeta("field3", "", MetaPropertyType.STRING.getValue(), (short)0);
		StoredClassMeta essence3 = new StoredClassMeta(false, "key3", "EssenceThree", "", "", "EssenceTwo", Arrays.asList(new StoredPropertyMeta[]{essence3property1, essence3property2}));
		
		deployEnvironment(new StoredClassMeta[]{essence1, essence2, essence3}, refIntegr, connection, null);
		return new Object[][]{
				new Object[]{essence1, essence1property1, essence1property2},
				new Object[]{essence2, essence2property1, essence2property2},
				new Object[]{essence3, essence3property1, essence3property2}
				};		
	}
	
	@Test
	public void Inheritance_CheckKeyFieldsExists() throws IOException, IonException, SQLException {
		Object[][] arrange = Inheritance_Arrange(false);
		
		String e1TableName = dbNames.getName(((StoredClassMeta)arrange[0][0]).name, DBNameType.TABLE);
		String k1FieldName = dbNames.getName(((StoredPropertyMeta)arrange[0][1]).name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(((StoredClassMeta)arrange[1][0]).name, DBNameType.TABLE);
		String e3TableName = dbNames.getName(((StoredClassMeta)arrange[2][0]).name, DBNameType.TABLE);
		
		ResultSet result;
		
		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+e1TableName+"' AND COLUMN_NAME='"+k1FieldName+"'");
		assertTrue(result.next());
		assertFalse(result.next());
		
		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+e2TableName+"' AND COLUMN_NAME='"+k1FieldName+"'");
		assertTrue(result.next());
		assertFalse(result.next());
		
		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='"+e3TableName+"' AND COLUMN_NAME='"+k1FieldName+"'");
		assertTrue(result.next());
		assertFalse(result.next());
	}
	
	@Test
	public void Inheritance_CheckPrimaryKey() throws IOException, IonException, SQLException {
		Object[][] arrange = Inheritance_Arrange(false);

		String e1TableName = dbNames.getName(((StoredClassMeta)arrange[0][0]).name, DBNameType.TABLE);
		String k1FieldName = dbNames.getName(((StoredPropertyMeta)arrange[0][1]).name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(((StoredClassMeta)arrange[1][0]).name, DBNameType.TABLE);
		String e3TableName = dbNames.getName(((StoredClassMeta)arrange[2][0]).name, DBNameType.TABLE);
		
		ResultSet result;
		
		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME WHERE C.TABLE_NAME='"+e1TableName+"' AND C.CONSTRAINT_TYPE='PRIMARY KEY'");
		assertTrue(result.next());
		assertEquals(k1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());

		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME WHERE C.TABLE_NAME='"+e2TableName+"' AND C.CONSTRAINT_TYPE='PRIMARY KEY'");
		assertTrue(result.next());
		assertEquals(k1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
		
		result = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME WHERE C.TABLE_NAME='"+e3TableName+"' AND C.CONSTRAINT_TYPE='PRIMARY KEY'");
		assertTrue(result.next());
		assertEquals(k1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
	}	
	
	private void inheritance_check_fk(boolean refIntegrity) throws IOException, IonException, SQLException {
		Object[][] arrange = Inheritance_Arrange(refIntegrity);
		
		String e1TableName = dbNames.getName(((StoredClassMeta)arrange[0][0]).name, DBNameType.TABLE);
		String k1FieldName = dbNames.getName(((StoredPropertyMeta)arrange[0][1]).name, DBNameType.COLUMN);
		String e2TableName = dbNames.getName(((StoredClassMeta)arrange[1][0]).name, DBNameType.TABLE);
		String e3TableName = dbNames.getName(((StoredClassMeta)arrange[2][0]).name, DBNameType.TABLE);
		
		ResultSet result;
		
		result = statement.executeQuery("SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e2TableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+k1FieldName+"'");
		assertTrue(result.next());
		assertEquals(e1TableName, result.getString("TABLE_NAME"));
		assertEquals(k1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
		
		result = statement.executeQuery("SELECT I.* FROM INFORMATION_SCHEMA.CONSTRAINTS AS C INNER JOIN INFORMATION_SCHEMA.INDEXES AS I ON C.UNIQUE_INDEX_NAME=I.INDEX_NAME"
				+ " WHERE C.TABLE_NAME='"+e3TableName+"' AND C.CONSTRAINT_TYPE='REFERENTIAL' AND C.COLUMN_LIST='"+k1FieldName+"'");
		assertTrue(result.next());
		assertEquals(e2TableName, result.getString("TABLE_NAME"));
		assertEquals(k1FieldName, result.getString("COLUMN_NAME"));
		assertFalse(result.next());
	}
	
	@Test
	public void Inheritance_CheckForeignKeys_NoReferentialIntegrity() throws IOException, IonException, SQLException {
		inheritance_check_fk(false);
	}
	
	@Test
	public void Inheritance_CheckForeignKeys_ReferentialIntegrity() throws IOException, IonException, SQLException {
		inheritance_check_fk(true);
	}
	
	@Test
	public void Migration() {
		assertTrue(false);
	}
}
