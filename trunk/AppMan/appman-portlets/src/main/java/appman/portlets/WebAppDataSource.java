package appman.portlets;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebAppDataSource extends HttpServlet {

	private static final long serialVersionUID = -85835755789357234L;
	private static final Log log = LogFactory.getLog(WebAppDataSource.class);

	private static BasicDataSource ds;

	public static Connection getConnection() throws SQLException {
		if (ds == null) throw new Error("context listener nao inicializado");
		return ds.getConnection();
	}

	public static void closeHandlers(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
			if (conn != null) conn.close();
		} catch (SQLException ex) {
			log.error("fechando handlers", ex);
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

	private Integer getIntOpt(String str) {
		if (str == null || str.trim().length() == 0) return null;
		return Integer.valueOf(str);
	}

	public void init(ServletConfig config) throws ServletException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(AppManConfig.get().getString("db.driverClassName"));
		ds.setUrl(AppManConfig.get().getString("db.url"));
		ds.setUsername(AppManConfig.get().getString("db.username"));
		ds.setPassword(AppManConfig.get().getString("db.password"));
		Integer val = getIntOpt(AppManConfig.get().getString("db.minIdle"));
		if (val != null) ds.setMinIdle(val.intValue());
		val = getIntOpt(AppManConfig.get().getString("db.maxIdle"));
		if (val != null) ds.setMaxIdle(val.intValue());
		val = getIntOpt(AppManConfig.get().getString("db.maxActive"));
		if (val != null) ds.setMaxActive(val.intValue());

		WebAppDataSource.ds = ds;
	}

	public void destroy() {
		WebAppDataSource.ds = null;
	}

}
