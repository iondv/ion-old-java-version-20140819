package ion.framework.test.workflow;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ion.core.IDataRepository;
import ion.core.IMetaRepository;
import ion.core.IPropertyMeta;
import ion.core.IWorkflowProvider;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.core.meta.ClassMeta;
import ion.core.meta.PropertyMeta;
import ion.core.mocks.MetaRepositoryMock;
import ion.framework.dao.jdbc.JdbcDataRepository;
import ion.framework.dao.workflow.WorkFlowProvider;

public class WorkflowAppConfig {
	
	@Bean
	public IWorkflowProvider workflowProvider(){
		WorkFlowProvider wfProvider = new WorkFlowProvider();
		wfProvider.setWorkflowDirectory(new File("/home/inkz/Spring/workspace/ion_core_dao/src/test/resources/workflows/"));
		wfProvider.setDataSource(dataSource());
		return wfProvider;
	}
	
	@Bean
	public IDataRepository dataRepository(){
		JdbcDataRepository dr = new JdbcDataRepository();
		dr.setDataSource(dataSource());
		dr.setMetaRepository(metaRepository());
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
		
		Map<String, IPropertyMeta> mammalsProps = new HashMap<String, IPropertyMeta>();
		mammalsProps.put("age", new PropertyMeta("age", "age", MetaPropertyType.INT, new Short("9")));
		
		Map<String, IPropertyMeta> dogsProps = new HashMap<String, IPropertyMeta>();
		dogsProps.put("heigth", new PropertyMeta("heigth","heigth", MetaPropertyType.INT, new Short("9")));	
		
		try {
			metaRepository.addStruct(new ClassMeta("animalId", "Animals", metaRepository), animalsProps);
			metaRepository.addStruct(new ClassMeta("animalId", "Mammals", metaRepository),mammalsProps);
			metaRepository.addStruct(new ClassMeta("animalId", "Dogs", metaRepository),dogsProps);
			metaRepository.setAncestor("Mammals", "Animals");
			metaRepository.setAncestor("Dogs", "Mammals");
		} catch (IonException e) {
			e.printStackTrace();
		}
		return metaRepository;
	}
	
	public Map<String,Object> createDog(Connection con) throws SQLException{
		Map<String,Object> inserts = new LinkedHashMap<String, Object>();
		inserts.put("animalId", 1);
		inserts.put("name", "DoggyDog");
		inserts.put("age", 1);
		inserts.put("heigth", 13);
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
	
	public void createItemTables(Connection con) throws SQLException{
    String itemsTable = "DROP TABLE IF EXISTS `t_animals`,`t_mammals`,`t_dogs`;"+
  					"CREATE TABLE `t_animals` ("+
  					 "`f_animal_id` int(11) NOT NULL,"+
  					 "`f_name` varchar(200) DEFAULT NULL,"+
  					  "`_type` varchar(200) DEFAULT NULL,"+
  					  "PRIMARY KEY (`f_animal_id`)"+
  					") ENGINE=InnoDB DEFAULT CHARSET=utf8;"+
  					
  					"CREATE TABLE `t_mammals` ("+
  					  "`f_animal_id` int(11) NOT NULL,"+
  					  "`f_age` int(11) DEFAULT NULL,"+
  					  "PRIMARY KEY (`f_animal_id`)"+
  					") ENGINE=InnoDB DEFAULT CHARSET=utf8;"+
  					
  					"CREATE TABLE `t_dogs` ("+
  					  "`f_animal_id` int(11) NOT NULL,"+
  					  "`f_heigth` int(11) DEFAULT NULL,"+
  					  "PRIMARY KEY (`f_animal_id`)"+
  					") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    Statement statement = con.createStatement();
    statement.execute(itemsTable);
    statement.close();
	}
	
}
