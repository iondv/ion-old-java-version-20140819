package ion.framework.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ion.core.Condition;
import ion.core.ConditionType;
import ion.core.FilterOption;
import ion.core.IItem;
import ion.core.IStructMeta;
import ion.core.IonException;
import ion.core.ListOptions;
import ion.core.Operation;
import ion.core.OperationType;
import ion.core.Sorting;
import ion.core.SortingMode;
import ion.core.mocks.MetaRepositoryMock;
import ion.framework.dao.jdbc.JdbcDataRepository;
import ion.framework.dao.jdbc.JdbcItem;
import ion.framework.test.domain.Dogs;

public class JdbcDataRepositoryTest {
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
	public void testGetItemClassnameId() throws IonException {
			IItem result = dataRepository.GetItem("Mammals", "1");
			Assert.assertNotNull(result);
			System.out.println("---");
			System.out.println(result.getClassName()+"@"+result.getItemId());
			Map<String, Object> insertions = insertResults.get("Dogs_1");
			for(Entry<String, Object> entry : insertions.entrySet()){
				System.out.println(entry.getKey()+": "+entry.getValue()+" = "+result.Get(entry.getKey()));
				Assert.assertEquals(entry.getValue(), result.Get(entry.getKey()));
			}
			
			IItem result2 = dataRepository.GetItem("Cows", "3");
			Assert.assertNotNull(result2);
			System.out.println("---");
			System.out.println(result2.getClassName()+"@"+result2.getItemId());
			Map<String, Object> insertions2 = insertResults.get("Cows_3");
			for(Entry<String, Object> entry : insertions2.entrySet()){
				System.out.println(entry.getKey()+": "+entry.getValue()+" = "+result2.Get(entry.getKey()));
				if(entry.getValue() instanceof Date){
					Assert.assertEquals(entry.getValue().toString(), result2.Get(entry.getKey()).toString());
				}else{
					Assert.assertEquals(entry.getValue(), result2.Get(entry.getKey()));
				}
			}
	}
	
	@Test
	public void testGetItemDummy() throws IonException{
		Dogs dog = new Dogs();
		dog.setAnimalId(1);
    IItem result = dataRepository.GetItem(dog);
    Assert.assertNotNull(result);
    System.out.println("---");
    System.out.println(result.getClassName()+"@"+result.getItemId());
		Map<String, Object> insertions = insertResults.get("Dogs_1");
		for(Entry<String, Object> entry : insertions.entrySet()){
			System.out.println(entry.getKey()+": "+entry.getValue()+" = "+result.Get(entry.getKey()));
			Assert.assertEquals(entry.getValue(), result.Get(entry.getKey()));
		}
	}
	
	@Test
	public void testGetItemClassname() throws IonException {
		IItem result = dataRepository.GetItem("Cows");
   	Assert.assertNotNull(result);
		Map<String, Object> insertions = insertResults.get("Cows_3");
		for(Entry<String, Object> entry : insertions.entrySet()){
				Assert.assertEquals(null, result.Get(entry.getKey()));
		}
	}
	
	@Test
	public void testCreateItem() throws IonException{
		Map<String,Object> props = appConfig.generateCowsInserts(4);
    IItem result = dataRepository.CreateItem("Cows", props);
    Assert.assertNotNull(result);
    Assert.assertEquals(props.get("name"), result.Get("name"));
	}
	
	@Test
	public void testUpdateItem() throws IonException{
		Map<String,Object> props = appConfig.generateCowsInserts(3);
		props.remove("animalId");
    IItem result = dataRepository.EditItem("Cows", "3", props);
    Assert.assertNotNull(result);
		for(Entry<String, Object> entry : props.entrySet()){
			if(entry.getValue() instanceof Date){
				Assert.assertEquals(entry.getValue().toString(), result.Get(entry.getKey()).toString());
			}else{
				Assert.assertEquals(entry.getValue(), result.Get(entry.getKey()));
			}
		}
	}
	
	@Test
	public void testDeleteItem() throws IonException{
    Boolean testDelete = dataRepository.DeleteItem("Mammals", "3");
    Assert.assertEquals(true, testDelete);
    IItem result = dataRepository.GetItem("Cows","3");
    Assert.assertNull(result);
	}
	
	@Test
	public void testSaveItem() throws IonException{
		IStructMeta meta = metaRepository.Get("Dogs");
		Map<String,Object> props = appConfig.generateDogsInserts(4);
		IItem sobaka = new JdbcItem("4", props, meta, dataRepository);
    IItem result = dataRepository.SaveItem(sobaka);
    Assert.assertNotNull(result);
    Assert.assertEquals(props.get("name"), result.Get("name"));
	}
	
	@Test
	public void testGetCountDummyLo() throws Exception{
		Connection connection = dataSource.getConnection();
		for(int i=4; i<10; i++){
			insertResults.put("Dogs_"+i, appConfig.dogsCreationQuery(i, connection));
		}
		for(int i=10; i<=20; i++){
			insertResults.put("Cows_"+i, appConfig.cowsCreationQuery(i, connection));
		}
    connection.close();
    IStructMeta dogMeta = metaRepository.Get("Dogs");
		Map<String,Object> dogProps = new LinkedHashMap<String, Object>();
		dogProps.put("name", insertResults.get("Dogs_8").get("name"));
		IItem dogItem = new JdbcItem(null, dogProps, dogMeta, dataRepository);
		long result = dataRepository.GetCount(dogItem,null);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result);
		
		IStructMeta meta = metaRepository.Get("Cows");
		Map<String,Object> props = new LinkedHashMap<String, Object>();
		BigDecimal middleDecim = new BigDecimal("0");
		Integer middleInteger = 0;
		for(Entry<String, Map<String, Object>> entry : insertResults.entrySet()){
			if(entry.getKey().contains("Cows")){
				middleDecim = middleDecim.add((BigDecimal)entry.getValue().get("tdecim"));
				middleInteger += (Integer)entry.getValue().get("tint");
			}
		}
		middleDecim = middleDecim.divide(new BigDecimal("2"));
		middleInteger = middleInteger/2;
		ListOptions lo = new ListOptions();
		FilterOption fo = new Condition("class", ConditionType.EQUAL, "Cows");
		FilterOption[] fos = new FilterOption[2];
		fos[0] = new Condition("tdecim", ConditionType.MORE, middleDecim);
		fos[1] = new Condition("tint", ConditionType.LESS_OR_EQUAL, middleInteger);
		FilterOption operation = new Operation(OperationType.OR, fos);
		lo.Filter().add(fo);
		lo.Filter().add(operation);
		IItem cowItem = new JdbcItem(null, props, meta, dataRepository);
		long result2 = dataRepository.GetCount(cowItem,lo);
		Assert.assertNotNull(result2);
	}
	
	@Test
	public void testGetCountDummy() throws IonException{
		IStructMeta dogMeta = metaRepository.Get("Dogs");
		Map<String,Object> dogProps = new LinkedHashMap<String, Object>();
		IItem dummy = new JdbcItem(null, dogProps, dogMeta, dataRepository);
		long result = dataRepository.GetCount(dummy);
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result);
	}
	
	@Test
	public void testGetCountClassnameLo() throws Exception{
		FilterOption fo = new Condition("class", ConditionType.EQUAL, "Dogs");
		ListOptions lo = new ListOptions();
		lo.Filter().add(fo);
		long result = dataRepository.GetCount("Mammals",lo);
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result);
	}
	
	@Test
	public void testGetCountClassname() throws Exception{
		long result = dataRepository.GetCount("Dogs");
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result);
	}
	
	@Test
	public void testGetListDummyLo() throws Exception{
		Connection connection = dataSource.getConnection();
		for(int i=4; i<10; i++){
			insertResults.put("Dogs_"+i, appConfig.dogsCreationQuery(i, connection));
		}
		for(int i=10; i<=20; i++){
			insertResults.put("Cows_"+i, appConfig.cowsCreationQuery(i, connection));
		}
    connection.close();
    Map<String,Object> props2 = new LinkedHashMap<String, Object>();
    IItem dummy = new JdbcItem(null, props2, metaRepository.Get("Dogs"), dataRepository);
    Collection<IItem> dogs = dataRepository.GetList(dummy, new ListOptions());
    Assert.assertNotNull(dogs);
    Assert.assertEquals(8, dogs.size());
    
    IItem dummy2 = new JdbcItem(null, props2, metaRepository.Get("Mammals"), dataRepository);
    Collection<IItem> mammals = dataRepository.GetList(dummy2, new ListOptions());
    Assert.assertNotNull(mammals);
    Assert.assertEquals(20, mammals.size());
    
		IStructMeta meta = metaRepository.Get("Cows");
		Map<String,Object> props = new LinkedHashMap<String, Object>();
    props.put("name", "Mary");
    props.put("age", 17);
		BigDecimal middleDecim = new BigDecimal("0");
		Integer middleInteger = 0;
		for(Entry<String, Map<String, Object>> entry : insertResults.entrySet()){
			if(entry.getKey().contains("Cows")){
				middleDecim = middleDecim.add((BigDecimal)entry.getValue().get("tdecim"));
				middleInteger += (Integer)entry.getValue().get("tint");
			}
		}
		middleDecim = middleDecim.divide(new BigDecimal("2"));
		middleInteger = middleInteger/2;
		ListOptions lo = new ListOptions();
		FilterOption fo = new Condition("class", ConditionType.EQUAL, "Cows");
		FilterOption[] fos = new FilterOption[3];
		fos[0] = new Condition("tdecim", ConditionType.MORE, middleDecim);
		fos[1] = new Condition("tint", ConditionType.LESS_OR_EQUAL, middleInteger);
		fos[2] = new Condition("age", ConditionType.EQUAL, middleInteger);
		FilterOption operation = new Operation(OperationType.OR, fos);
		lo.Filter().add(fo);
		lo.Filter().add(operation);
		Sorting so = new Sorting("age");
		Sorting so1 = new Sorting("name", SortingMode.DESC);
		lo.Sorting().add(so);
		lo.Sorting().add(so1);
		lo.SetPageSize(5);
		lo.SetPage(3);
		IItem cowItem = new JdbcItem(null, props, meta, dataRepository);
		Collection<IItem> result2 = dataRepository.GetList(cowItem,lo);
		Assert.assertNotNull(result2);
	}
	
	@Test
	public void testGetIteratorDummyLo() throws SQLException, IonException{
		Connection connection = dataSource.getConnection();
		for(int i=4; i<10; i++){
			insertResults.put("Dogs_"+i, appConfig.dogsCreationQuery(i, connection));
		}
		for(int i=10; i<=20; i++){
			insertResults.put("Cows_"+i, appConfig.cowsCreationQuery(i, connection));
		}
    connection.close();
    Map<String,Object> props2 = new LinkedHashMap<String, Object>();
    IItem dummy = new JdbcItem(null, props2, metaRepository.Get("Mammals"), dataRepository);
    Iterator<IItem> iter = dataRepository.GetIterator(dummy, new ListOptions());
    while(iter.hasNext()){
    	IItem i = iter.next();
    	System.out.println("#"+i.getClassName()+"@"+i.getItemId());
    	Assert.assertNotNull(i);
    }
	}

}
