package appman.portlets;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import appman.portlets.model.AppManJob;

public class AppManDBHelper {

	private static AppManJob populateJob(ResultSet rs) throws SQLException {
		AppManJob job = new AppManJob();
		job.setId(rs.getInt("job_id"));
		job.setUserName(rs.getString("username"));

		String file = rs.getString("file");
		job.setFile(file);
		job.setFileExists(new File(AppManHelper.getJobDir(job.getId()), file).exists());
		job.setDtStart(rs.getTimestamp("dtstart"));
		job.setDtEnd(rs.getTimestamp("dtend"));
		job.setSuccess(rs.getString("success"));

		String status = null;
		if (job.getDtStart() == null)
			status = "Aguardando";
		else if (job.getDtEnd() != null)
			status = "Finalizado";
		else
			status = "Executando";
		job.setStatus(status);
		return job;
	}

	public static ArrayList<AppManJob> searchJobs() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM APPMAN_JOB WHERE DELETED = ?");
			stmt.setString(1, "N");
			rs = stmt.executeQuery();
			ArrayList<AppManJob> jobs = new ArrayList<AppManJob>();
			while (rs.next()) {
				jobs.add(populateJob(rs));
			}

			return jobs;
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, rs);
		}
	}

	public static AppManJob findJobToRun() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("SELECT * FROM APPMAN_JOB WHERE DELETED = ?"
				+ " AND DTSTART IS NULL AND DTEND IS NULL ORDER BY JOB_ID");
			stmt.setString(1, "N");
			rs = stmt.executeQuery();
			if (rs.next()) {
				return populateJob(rs);
			}
			return null;
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, rs);
		}
	}

	public static boolean isJobFinished(int jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("SELECT JOB_ID FROM APPMAN_JOB WHERE JOB_ID = ?" +
				" AND (DELETED = ? OR DTEND IS NOT NULL)");
			int i = 1;
			stmt.setInt(i++, jobId);
			stmt.setString(i++, "Y");
			rs = stmt.executeQuery();
			return rs.next();
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, rs);
		}
	}

	public static void createJob(String userName, String fileName, int id) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = WebAppDataSource.getConnection();

			stmt = conn.prepareStatement("INSERT INTO APPMAN_JOB"
				+ "(USERNAME, FILE, DTSTART, DTEND, SUCCESS, DELETED, JOB_ID) VALUES (?, ?, ?, ?, ?, ?, ?)");
			int i = 1;
			stmt.setString(i++, userName);
			stmt.setString(i++, fileName);
			stmt.setNull(i++, Types.TIMESTAMP);
			stmt.setNull(i++, Types.TIMESTAMP);
			stmt.setNull(i++, Types.VARCHAR);
			stmt.setString(i++, "N");
			stmt.setInt(i++, id);

			stmt.executeUpdate();
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static void updateJobStart(int jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET DTSTART = ? WHERE JOB_ID = ?");
			int i = 1;
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setInt(i++, jobId);

			stmt.executeUpdate();
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static void updateJobFailed(int jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET DTSTART = ?, DTEND = ?, SUCCESS = ? WHERE JOB_ID = ?");
			int i = 1;
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setString(i++, "N");
			stmt.setInt(i++, jobId);

			stmt.executeUpdate();
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static void deleteJob(int jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET DELETED = ? WHERE JOB_ID = ?");
			int i = 1;
			stmt.setString(i++, "Y");
			stmt.setInt(i++, jobId);

			stmt.executeUpdate();
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static String getAppId(int jobId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("SELECT EXEHDA_APP_ID FROM APPMAN_JOB WHERE JOB_ID = ?");
			stmt.setInt(1, jobId);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString("EXEHDA_APP_ID");
			}
			return null;
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static void finalizeApplication(String appId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("UPDATE APPMAN_JOB SET DTEND = ?, SUCCESS = ? WHERE EXEHDA_APP_ID = ?");
			int i = 1;
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setString(i++, "N");
			stmt.setString(i++, appId);

			stmt.executeUpdate();
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, null);
		}
	}

	public static ArrayList<AppManJob> searchRunningJobs() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("SELECT JOB_ID FROM APPMAN_JOB WHERE DELETED = ?" +
				" AND DTSTART IS NOT NULL AND DTEND IS NULL");
			stmt.setString(1, "N");
			rs = stmt.executeQuery();
			ArrayList<AppManJob> jobs = new ArrayList<AppManJob>();
			while (rs.next()) {
				AppManJob job = new AppManJob();
				job.setId(rs.getInt("JOB_ID"));
				jobs.add(job);
			}
			return jobs;
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, rs);
		}
	}

	public static boolean hasRunningJobs() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = WebAppDataSource.getConnection();
			stmt = conn.prepareStatement("SELECT JOB_ID FROM APPMAN_JOB WHERE DELETED = ?" +
				" AND DTSTART IS NOT NULL AND DTEND IS NULL");
			stmt.setString(1, "N");
			rs = stmt.executeQuery();
			return rs.next();
		} finally {
			WebAppDataSource.closeHandlers(conn, stmt, rs);
		}
	}
}
