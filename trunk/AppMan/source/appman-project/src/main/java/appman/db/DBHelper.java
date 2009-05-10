package appman.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBHelper {

	public static void registerAppId(String appId, Integer jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = AppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET EXEHDA_APP_ID = ? WHERE JOB_ID = ?");
			int i = 1;
			stmt.setString(i++, appId);
			stmt.setInt(i++, jobId);
			stmt.execute();
		} finally {
			AppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static void registerAppEnd(Integer jobId, boolean success) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = AppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET DTEND = ?, SUCCESS = ? WHERE JOB_ID = ?");
			int i = 1;
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setString(i++, success ? "Y" : "N");
			stmt.setInt(i++, jobId);
			stmt.execute();
		} finally {
			AppDataSource.closeHandlers(conn, stmt, null);
		}
	}
}
