package ion.framework.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.activation.DataSource;

import ion.core.IDataRepository;
import ion.core.IMetaRepository;
import ion.core.IPropertyMeta;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.logging.ILogger;
import ion.core.meta.ClassMeta;
import ion.core.meta.PropertyMeta;
import ion.core.meta.ReferencePropertyMeta;
import ion.core.mocks.MetaRepositoryMock;
import ion.framework.dao.jdbc.IJdbcConnectionProvider;
import ion.framework.dao.jdbc.JdbcDataRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class TestAppConfig {
	
	private ILogger logger;
	
	@Bean
	public IDataRepository dataRepository(){
		final DriverManagerDataSource ds = this.dataSource();
		JdbcDataRepository dr = new JdbcDataRepository(new IJdbcConnectionProvider() {
			@Override
			public Connection getConnection() {
				try {
					return ds.getConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		dr.setMetaRepository(metaRepository());
		dr.setLogger(logger);
		dr.setShowSql(true);
		return dr;
	}
	
	@Bean
	public DriverManagerDataSource dataSource(){
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		ds.setUsername("sa");
		ds.setPassword("");
		return ds;
	}
	
	@Bean
	public IMetaRepository metaRepository(){
		MetaRepositoryMock metaRepository = new MetaRepositoryMock();
		
		Map<String, IPropertyMeta> animalsProps = new HashMap<String, IPropertyMeta>();
		animalsProps.put("animalId", new PropertyMeta("animalId", "animalId", MetaPropertyType.INT, new Short("9")));
		animalsProps.put("name", new PropertyMeta("name", "name", MetaPropertyType.STRING));
		
		Map<String, IPropertyMeta> birdsProps = new HashMap<String, IPropertyMeta>();
		birdsProps.put("birdType", new PropertyMeta("birdType", "birdType", MetaPropertyType.STRING));
		
		Map<String, IPropertyMeta> mammalsProps = new HashMap<String, IPropertyMeta>();
		mammalsProps.put("age", new PropertyMeta("age", "age", MetaPropertyType.INT, new Short("9")));
		
		Map<String, IPropertyMeta> catsProps = new HashMap<String, IPropertyMeta>();
		catsProps.put("catscol", new PropertyMeta("catscol","catscol", MetaPropertyType.STRING));
		
		Map<String, IPropertyMeta> dogsProps = new HashMap<String, IPropertyMeta>();
		dogsProps.put("heigth", new PropertyMeta("heigth","heigth", MetaPropertyType.INT, new Short("9")));	
		
		Map<String, IPropertyMeta> cowsProps = new HashMap<String, IPropertyMeta>();
		cowsProps.put("info", new PropertyMeta("info", "info", MetaPropertyType.TEXT));
		cowsProps.put("tbool", new PropertyMeta("tbool", "tbool", MetaPropertyType.BOOLEAN));
		cowsProps.put("tdate", new PropertyMeta("tdate", "tdate", MetaPropertyType.DATETIME));
		cowsProps.put("tdecim", new PropertyMeta("tdecim", "tdecim", MetaPropertyType.DECIMAL));
		cowsProps.put("tfile", new PropertyMeta("tfile", "tfile", MetaPropertyType.FILE));
		cowsProps.put("tguid", new PropertyMeta("tguid", "tguid", MetaPropertyType.GUID));
		cowsProps.put("thtml", new PropertyMeta("thtml", "thtml", MetaPropertyType.HTML));
		cowsProps.put("timage", new PropertyMeta("timage", "timage", MetaPropertyType.IMAGE));
		cowsProps.put("tint", new PropertyMeta("tint", "tint", MetaPropertyType.INT,new Short((short) 11)));
		cowsProps.put("tpass", new PropertyMeta("tpass", "tpass", MetaPropertyType.PASSWORD));
		cowsProps.put("treal", new PropertyMeta("treal", "treal", MetaPropertyType.REAL));
		cowsProps.put("tref", new ReferencePropertyMeta("tref","tref","Dogs", false,metaRepository));
		cowsProps.put("tset", new PropertyMeta("tset", "tset", MetaPropertyType.SET));
		cowsProps.put("tstring", new PropertyMeta("tstring", "tstring", MetaPropertyType.STRING));
		cowsProps.put("ttext", new PropertyMeta("ttext", "ttext", MetaPropertyType.TEXT));
		cowsProps.put("turl", new PropertyMeta("turl", "turl", MetaPropertyType.URL));
		try {
			metaRepository.addStruct(new ClassMeta("animalId", "Animals", metaRepository), animalsProps);
			metaRepository.addStruct(new ClassMeta("animalId", "Birds", metaRepository),birdsProps);
			metaRepository.addStruct(new ClassMeta("animalId", "Mammals", metaRepository),mammalsProps);
			metaRepository.addStruct(new ClassMeta("animalId", "Cats", metaRepository), catsProps);
			metaRepository.addStruct(new ClassMeta("animalId", "Dogs", metaRepository),dogsProps);
			metaRepository.addStruct(new ClassMeta("animalId", "Cows", metaRepository),cowsProps);
			metaRepository.setAncestor("Birds", "Animals");
			metaRepository.setAncestor("Mammals", "Animals");
			metaRepository.setAncestor("Cats", "Mammals");
			metaRepository.setAncestor("Dogs", "Mammals");
			metaRepository.setAncestor("Cows", "Mammals");
		} catch (IonException e) {
			e.printStackTrace();
		}
		return metaRepository;
	}
	
	public String tablesCreationQuery(){
		String query = 
			"DROP TABLE IF EXISTS `t_animals`,`t_birds`,`t_mammals`,`t_cats`,`t_dogs`,`t_cows`;"+
			"CREATE TABLE `t_animals` ("+
			 "`f_animal_id` int(11) NOT NULL,"+
			 "`f_name` varchar(200) DEFAULT NULL,"+
			  "`_type` varchar(200) DEFAULT NULL,"+
			  "PRIMARY KEY (`f_animal_id`)"+
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"+
			
			"CREATE TABLE `t_birds` ("+
			  "`f_animal_id` int(11) NOT NULL,"+
			  "`f_bird_type` varchar(200) DEFAULT NULL,"+
			  "PRIMARY KEY (`f_animal_id`)"+
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"+
			
			"CREATE TABLE `t_mammals` ("+
			  "`f_animal_id` int(11) NOT NULL,"+
			  "`f_age` int(11) DEFAULT NULL,"+
			  "PRIMARY KEY (`f_animal_id`)"+
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"+
			
			"CREATE TABLE `t_cats` ("+
			  "`f_animal_id` int(11) NOT NULL,"+
			  "`f_catscol` varchar(200) DEFAULT NULL,"+
			  "PRIMARY KEY (`f_animal_id`)"+
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"+
			
			"CREATE TABLE `t_dogs` ("+
			  "`f_animal_id` int(11) NOT NULL,"+
			  "`f_heigth` int(11) DEFAULT NULL,"+
			  "PRIMARY KEY (`f_animal_id`)"+
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"+
			  
			"CREATE TABLE `t_cows` ("+
				"`f_animal_id` int(11) NOT NULL,"+
			  "`f_info` text NOT NULL,"+
			  "`f_tbool` tinyint(1) DEFAULT NULL,"+
			  "`f_tdate` date DEFAULT NULL,"+
			  "`f_tdecim` decimal(10,1) DEFAULT NULL,"+
			  "`f_tfile` varchar(200) DEFAULT NULL,"+
			  "`f_tguid` char(36) DEFAULT NULL,"+
			  "`f_thtml` text,"+
			  "`f_timage` varchar(200) DEFAULT NULL,"+
			  "`f_tint` bigint(20) DEFAULT NULL,"+
			  "`f_tpass` varchar(200) DEFAULT NULL,"+
			  "`f_treal` double DEFAULT NULL,"+
			  "`f_tref` char(1) DEFAULT NULL,"+
			  "`f_tset` smallint(6) DEFAULT NULL,"+
			  "`f_tstring` varchar(200) DEFAULT NULL,"+
			  "`f_ttext` text,"+
			  "`f_turl` varchar(200) DEFAULT NULL,"+
		  "PRIMARY KEY (`f_animal_id`)"+
		  ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		return query;
	}
	
	public Map<String,Object> generateDogsInserts(Integer id){
		Map<String,Object> inserts = new LinkedHashMap<String, Object>();
		inserts.put("animalId", id);
		String rs = generateRandomString();
		inserts.put("name", rs);
		Integer ri = generateRandomInt();
		inserts.put("age", ri);
		Integer ri2 = generateRandomInt();
		inserts.put("heigth", ri2);
		return inserts;
	}
	
	public Map<String,Object> dogsCreationQuery(Integer id, Connection con) throws SQLException{
		Map<String,Object> inserts = generateDogsInserts(id);
		String sqlAnimals = "INSERT INTO `t_animals` (`f_animal_id`,`f_name`,`_type`) VALUES (?,?,?);";
		String sqlMammals = "INSERT INTO `t_mammals` (`f_animal_id`, `f_age`) VALUES (?,?);";
		String sqlDogs =	"INSERT INTO `t_dogs` (`f_animal_id`,`f_heigth`) VALUES (?,?);";
		PreparedStatement st = con.prepareStatement(sqlAnimals);
		st.setInt(1, (int) inserts.get("animalId"));
		st.setString(2, (String) inserts.get("name"));
		st.setString(3, "Dogs");
		st.executeUpdate();
		
		st = con.prepareStatement(sqlMammals);
		st.setInt(1, (int) inserts.get("animalId"));
		st.setInt(2,(int) inserts.get("age"));
		st.executeUpdate();
		
		st = con.prepareStatement(sqlDogs);
		st.setInt(1, (int) inserts.get("animalId"));
		st.setInt(2,(int) inserts.get("heigth"));
		st.executeUpdate();
		
		st.close();
		return inserts;
	}
	
	public String catsCreationQuery(){
		String query = ""+
				"INSERT INTO `t_animals` (`f_animal_id`,`f_name`,`_type`) VALUES (?,?,?);"+
				"INSERT INTO `t_mammals` (`f_animal_id`, `f_age`) VALUES (?,?);"+
				"INSERT INTO `t_cats` (`f_animal_id`, `t_catscol`) VALUES (?,?);";
		return query;
	}
	
	public String birdsCreationQuery(){
		String query = ""+
				"INSERT INTO `t_animals` (`f_animal_id`,`f_name`,`_type`) VALUES (?,?,?);"+
				"INSERT INTO `t_birds` (`f_animal_id`, `f_bird_type`) VALUES (?,?);";
		return query;
	}
	
	public Map<String,Object> generateCowsInserts(Integer id){
		Map<String,Object> inserts = new LinkedHashMap<String, Object>();
		inserts.put("animalId", id);
		String rs = generateRandomString();
		inserts.put("name", rs);
		
		Integer ri = generateRandomInt();
		inserts.put("age", ri);
		
		String rs1 = generateRandomString();
    inserts.put("info", rs1);
    
    Boolean b = new Random().nextBoolean();
    inserts.put("tbool",b);
    
    Date date = new Date(new java.util.Date().getTime());
		inserts.put("tdate", date);
		
		Integer ri1 = new Random().nextInt(200);
		BigDecimal bd = new BigDecimal(ri1.toString()+".5");
    inserts.put("tdecim", bd);
    
		String rs2 = generateRandomString();
    inserts.put("tfile", rs2);
    
		String rguid = UUID.randomUUID().toString();
    inserts.put("tguid", rguid);
    
    String rs3 = "<h1>"+generateRandomString()+"</h1>";
    inserts.put("thtml", rs3);
    
    String rs4 = "/"+generateRandomString()+".jpg";
    inserts.put("timage", rs4);
    
    Integer ri2 = generateRandomInt();
    inserts.put("tint", ri2);
    
    String rs5 = generateRandomString();
    inserts.put("tpass", rs5);
    
    Float f = new Random().nextFloat();
    inserts.put("treal", f);
    
    inserts.put("tref", 1);
    
    Short sh = new Short(String.valueOf(new Random().nextInt(50)));
    inserts.put("tset", sh);
    
    String rs6 = generateRandomString();
    inserts.put("tstring", rs6);
    
    String rs7 = generateRandomString();
    inserts.put("ttext", rs7);
    
    String rs8 = "www."+generateRandomString()+".com";
    inserts.put("turl", rs8);
		
		return inserts;
	}
	
	public Map<String,Object> cowsCreationQuery(Integer id, Connection con) throws SQLException{
		Map<String,Object> inserts = generateCowsInserts(id);
		String sqlAnimals = "INSERT INTO `t_animals` (`f_animal_id`,`f_name`,`_type`) VALUES (?,?,?);";
		String sqlMammals = "INSERT INTO `t_mammals` (`f_animal_id`, `f_age`) VALUES (?,?);";
		String cowsQuery = "INSERT INTO `t_cows` (`f_animal_id`,`f_info`,`f_tbool`,`f_tdate`,`f_tdecim`,`f_tfile`,`f_tguid`,`f_thtml`"+
				",`f_timage`,`f_tint`,`f_tpass`,`f_treal`,`f_tref`,`f_tset`,`f_tstring`,`f_ttext`,`f_turl`)"+
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement st = con.prepareStatement(sqlAnimals);
		st.setInt(1, (int) inserts.get("animalId"));
		st.setString(2, (String) inserts.get("name"));
		st.setString(3, "Cows");
		st.executeUpdate();
		
		st = con.prepareStatement(sqlMammals);
		st.setInt(1, (int) inserts.get("animalId"));
		st.setInt(2, (int) inserts.get("age"));
		st.executeUpdate();
		
		st = con.prepareStatement(cowsQuery);
		st.setInt(1, id);
    st.setString(2, (String) inserts.get("info"));
    st.setBoolean(3, (boolean) inserts.get("tbool"));
		st.setDate(4, (Date) inserts.get("tdate"));
    st.setBigDecimal(5, (BigDecimal) inserts.get("tdecim"));
    st.setString(6, (String) inserts.get("tfile"));
    st.setString(7, (String) inserts.get("tguid"));
    st.setString(8, (String) inserts.get("thtml"));
    st.setString(9, (String) inserts.get("timage"));
    st.setInt(10, (int) inserts.get("tint"));
    st.setString(11, (String) inserts.get("tpass"));
    st.setFloat(12, (float) inserts.get("treal"));
    st.setInt(13, (int) inserts.get("tref"));
    st.setShort(14, (short) inserts.get("tset"));
    st.setString(15, (String) inserts.get("tstring"));
    st.setString(16, (String) inserts.get("ttext"));
    st.setString(17, (String) inserts.get("turl"));
		st.executeUpdate();
		
		st.close();
		return inserts;
	}
	
	private String generateRandomString(){
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 9; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	
	private Integer generateRandomInt(){
		Random random = new Random();
		return random.nextInt();
	}

}
