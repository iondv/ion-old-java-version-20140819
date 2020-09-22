package ion.db.search;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class TestSearchAdapter {
	
	DriverManagerDataSource ds;
	MySQLFulltextSearch searchAdapter;
	String itemId = "rap@123";
	String itemText = "The debugging part of your answer will not work. It needs to be corrected. Put the name of the database that you want to search the table in as 1st parameter";
	
	@Before
	public void before(){
		ds = new DriverManagerDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost/ft_search_test");
		ds.setUsername("root");
		ds.setPassword("");
		searchAdapter = new MySQLFulltextSearch();
		searchAdapter.setDataSource(ds);
		JdbcTemplate delete = new JdbcTemplate(ds);
		delete.execute("DROP TABLE IF EXISTS SEARCHTABLE");
	}
	
	@Test
	public void testSavingItemToDB(){
		searchAdapter.put(itemId, itemText);
		JdbcTemplate list = new JdbcTemplate(ds);
		List<TestItem> results = list.query("SELECT ID, SEARCHTEXT FROM SEARCHTABLE WHERE ID=?", new Object[]{itemId}, new RowMapper<TestItem>(){
			public TestItem mapRow(ResultSet rs, int arg1)
					throws SQLException {
				TestItem item = new TestItem();
				item.setId(rs.getString("ID"));
				item.setSearchtext(rs.getString("SEARCHTEXT"));
				return item;
			}});
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
	}
	
	@Test
	public void testSearchingQuery(){
		searchAdapter.put(itemId, itemText);
		List<String> results = searchAdapter.search("debugging",0,10);
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
	}
	
	@Test
	public void testUpdatingItem(){
		searchAdapter.put(itemId, itemText);
		searchAdapter.put(itemId, "shlyapa");
		List<String> results = searchAdapter.search("shlyapa",0,10);
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
	}
	
	public class TestItem{
		String id;
		String searchtext;
		public String getSearchtext() {
			return searchtext;
		}
		public void setSearchtext(String searchtext) {
			this.searchtext = searchtext;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}

}
