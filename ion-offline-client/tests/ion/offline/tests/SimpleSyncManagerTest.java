//package ion.offline.tests;
//
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.io.InputStream;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.Map;
//
//import ion.core.IDataRepository;
//import ion.core.IPropertyMeta;
//import ion.core.IStructMeta;
//import ion.core.IonException;
//import ion.core.MetaPropertyType;
//import ion.core.meta.ClassMeta;
//import ion.core.meta.CollectionPropertyMeta;
//import ion.core.meta.PropertyMeta;
//import ion.core.meta.StructMeta;
//import ion.core.meta.StructPropertyMeta;
//import ion.core.mocks.ChangeLoggerMock;
//import ion.core.mocks.FileStorageMock;
//import ion.core.mocks.LoggerMock;
//import ion.core.mocks.MetaRepositoryMock;
//import ion.core.storage.IFileStorage;
//import ion.offline.mocks.JsonDataRepositoryMock;
//import ion.offline.mocks.SyncEnvironmentMock;
//import ion.offline.mocks.VolumeProcessorMock;
//import ion.offline.sync.ISyncEnvironment;
//import ion.offline.sync.SimpleSyncManager;
//
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//public class SimpleSyncManagerTest {
//	
//	private class testStructure {
//		private String field_1;
//		private String field_2;
//		public void setField_1(String v){field_1 = v;}
//		public void setField_2(String v){field_2 = v;}
//		public String getField_1(){return field_1;}
//		public String getField_2(){return field_2;}
//	}
//	
//	SimpleSyncManager tested;
//	MetaRepositoryMock metaRepository;
//	IDataRepository dataRepository;
//
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//	}
//
//	@Before
//	public void setUp() throws Exception {
//		
//		metaRepository = new MetaRepositoryMock();
//		
//		Map<String, IPropertyMeta> props = new HashMap<String, IPropertyMeta>();
//		props.put("StructField_1", new PropertyMeta("StructField_1", "Поле 1", MetaPropertyType.STRING));
//		props.put("StructField_2", new PropertyMeta("StructField_2", "Поле 2", MetaPropertyType.FILE));
//		metaRepository.addStruct(new StructMeta("testStructure", metaRepository), props);
//		Map<String, IPropertyMeta> props2 = new HashMap<String, IPropertyMeta>();
//		props2.put("ClassField_1", new PropertyMeta("ClassField_1", "Поле 1", MetaPropertyType.STRING));
//		props2.put("ClassField_2", new StructPropertyMeta("ClassField_2", "Поле 2", "testStructure", metaRepository));
//		metaRepository.addClass(new ClassMeta("ClassField_1", "testClass", metaRepository), props2);
//		
//		Map<String, Class<?>> domain = new HashMap<String, Class<?>>();
//		domain.put("testStructure", testStructure.class);
//		
//		dataRepository = new JsonDataRepositoryMock(domain, metaRepository);
//		
//		ISyncEnvironment environment = new SyncEnvironmentMock(	dataRepository, metaRepository, new ChangeLoggerMock(), null, null, null, null);
//		IFileStorage fileStorage = new FileStorageMock(new HashMap<String, InputStream>(), "");
//		
//		tested = new SimpleSyncManager();
//		tested.setEnvironment(environment);
//		tested.setLogger(new LoggerMock());
//		tested.setVolumeProcessor(new VolumeProcessorMock(new File[]{}));
//		tested.setFileStorage(fileStorage);
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//
//	@Test
//	public void parseFileAttribute() throws IonException {		
//		
//		InputStream is = new java.io.StringBufferInputStream("Lorem ipsum dolor sit amet");
//		tested.getFileStorage().Accept(is, "struct_test", 0);
//		InputStream is2 = new java.io.StringBufferInputStream("Fucking in the bushes");
//		tested.getFileStorage().Accept(is2, "class_test", 0);
//
//		IStructMeta meta;
//		Object value;
//		Object expected;
//		
//		meta = metaRepository.GetStructure("testStructure");
//		value = "struct_test";
//		expected = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQ=";
//		assertEquals(expected, tested.parseFileAttribute(meta, "StructField_2", value));
//		
//		meta = metaRepository.GetClass("testClass");
//		value = "class_test";
//		expected = "RnVja2luZyBpbiB0aGUgYnVzaGVz";
//		assertEquals(expected, tested.parseFileAttribute(meta, "ClassField_2.StructField_2", value));
//	}	
//}
