package ion.db.search;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


public class MySQLFulltextSearch implements IFulltextSearchAdapter {
	
	DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		createTable();
	}
	
	private void createTable(){
		String query = "CREATE TABLE IF NOT EXISTS SEARCHTABLE (ID VARCHAR(100) PRIMARY KEY , SEARCHTEXT TEXT, FULLTEXT(SEARCHTEXT)) ENGINE=MYISAM";
		JdbcTemplate table = new JdbcTemplate(dataSource);
		table.execute(query);
	}
	
	private Boolean checkIfItemExists(String id){
		JdbcTemplate template = new JdbcTemplate(dataSource);
		Integer rowCount = template.queryForObject("SELECT count(*) FROM `SEARCHTABLE` WHERE `ID` = ?", Integer.class, id);
		if(rowCount == 0) return false;
		return true;
	}

	public void put(String id, String text){
		if (text != null && !text.isEmpty()){
			JdbcTemplate insert = new JdbcTemplate(dataSource);
			if(checkIfItemExists(id)){
				insert.update("UPDATE `SEARCHTABLE` SET `SEARCHTEXT`=? WHERE `ID`=?", new Object[]{text,id});
			}	else	{
				insert.update("INSERT INTO SEARCHTABLE (ID, SEARCHTEXT) VALUES(?,?)", new Object[]{id,text});
			}
		}
	}

	public List<String> search(String searchQuery, int offset, int rows) {
		JdbcTemplate searchResult = new JdbcTemplate(dataSource);
		if (searchQuery != null){
			List<String> results = searchResult.query("SELECT ID FROM SEARCHTABLE WHERE MATCH (SEARCHTEXT) AGAINST (?"+(searchQuery.endsWith("*")?" IN BOOLEAN MODE":"")+") LIMIT ?,?", new Object[]{searchQuery,offset,rows}, new RowMapper<String>(){
				public String mapRow(ResultSet rs, int arg1) throws SQLException {
					return rs.getString("ID");
				}
			});
			return results;
		}
		return new ArrayList<String>();
	}

	public void remove(String id) {
		JdbcTemplate del = new JdbcTemplate(dataSource);
		del.update("DELETE `SEARCHTABLE` WHERE `ID`=?", new Object[]{id});
	}
}
