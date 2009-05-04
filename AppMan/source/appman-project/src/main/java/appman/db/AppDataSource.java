package appman.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.BasicDataSource;

import appman.log.Debug;

public class AppDataSource {

	private static final long serialVersionUID = -85835755789357234L;

	private static BasicDataSource ds;

	static {
		ds = new BasicDataSource();
		ds.setDriverClassName("org.hsqldb.jdbcDriver");
		ds.setUrl("jdbc:hsqldb:hsql://localhost:8887");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMinIdle(1);
		ds.setMaxIdle(2);
		ds.setMaxActive(16);
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	public static void closeHandlers(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
			if (conn != null) conn.close();
		} catch (SQLException ex) {
			Debug.debug("erro fechando handlers", ex);
		}
	}

	public static Integer getSequenceNextVal(String seqName) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT top 1 NEXT VALUE FOR " + seqName
				+ " FROM information_schema.SYSTEM_sequences");
			rs.next();
			Integer seq = rs.getInt(1);
			return seq;
		} finally {
			closeHandlers(conn, stmt, rs);
		}
	}
}
