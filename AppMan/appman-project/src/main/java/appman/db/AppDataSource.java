package appman.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AppDataSource {

	private static final long serialVersionUID = -85835755789357234L;
	private static final Log log = LogFactory.getLog(AppDataSource.class);

	private static BasicDataSource ds;

	private static void init() {
		URL dbconfig = AppDataSource.class.getResource("/db.properties");
		try {
			InputStream is = dbconfig.openStream();
			Properties props = new Properties();
			props.load(is);
			is.close();

			ds = new BasicDataSource();
			ds.setDriverClassName(props.getProperty("appman.db.driver"));
			ds.setUrl(props.getProperty("appman.db.url"));
			ds.setUsername(props.getProperty("appman.db.username"));
			ds.setPassword(props.getProperty("appman.db.password"));
			ds.setMinIdle(Integer.parseInt(props.getProperty("appman.db.ds.minIdle")));
			ds.setMaxIdle(Integer.parseInt(props.getProperty("appman.db.ds.maxIdle")));
			ds.setMaxActive(Integer.parseInt(props.getProperty("appman.db.ds.maxActive")));
		} catch (IOException ex) {
			throw new Error("impossível ler " + dbconfig, ex);
		}
	}

	public static Connection getConnection() throws SQLException {
		if (ds == null) init();
		return ds.getConnection();
	}

	public static void closeHandlers(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
			if (conn != null) conn.close();
		} catch (SQLException ex) {
			log.warn("erro fechando handlers", ex);
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
