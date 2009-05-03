package appman.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBHelper {

	public static void registerAppStart(String appId, String jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = AppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET EXEHDA_APP_ID = ?, DTSTART = ? WHERE JOB_ID = ?");
			int i = 1;
			stmt.setString(i++, appId);
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setInt(i++, Integer.parseInt(jobId));
			stmt.execute();
		} finally {
			AppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static void registerAppEnd(String jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = AppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET DTEND = ? WHERE JOB_ID = ?");
			int i = 1;
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setInt(i++, Integer.parseInt(jobId));
			stmt.execute();
		} finally {
			AppDataSource.closeHandlers(conn, stmt, null);
		}
	}
}
