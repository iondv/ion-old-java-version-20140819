package ion.framework.dao.jdbc;

import ion.core.IonException;

import java.sql.Connection;

public interface IJdbcConnectionProvider {
	public Connection getConnection() throws IonException;
}
