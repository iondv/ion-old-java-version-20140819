package ion.web.app.service;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import ion.core.IonException;
import ion.framework.dao.jdbc.IJdbcConnectionProvider;

public class JdbcConnectionProvider implements IJdbcConnectionProvider {

	DataSource dataSource;
	
	public JdbcConnectionProvider(DataSource dataSource){
		this.dataSource = dataSource;
	}
	
	@Override
	public Connection getConnection() throws IonException {
		return DataSourceUtils.getConnection(dataSource);
	}
}
