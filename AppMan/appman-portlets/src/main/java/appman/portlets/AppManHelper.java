package appman.portlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.portlets.exception.BusinessException;
import appman.portlets.model.AppManJob;

public class AppManHelper {

	private static final Log log = LogFactory.getLog(AppManHelper.class);

	public static File getJobDir(int jobId) {
		return new File(AppManConfig.get().getString("appman.portlets.job.dir"), String.valueOf(jobId));
	}

	public static int setupJob(String userName, FileItem dagFile) throws Exception {

		int id = WebAppDataSource.getSequenceNextVal("APPMAN_JOB_ID");

		// inicializando pasta de trabalho e dag
		File jobDir = new File(AppManConfig.get().getString("appman.portlets.job.dir"), String.valueOf(id));
		jobDir.mkdirs();

		File dagOut = new File(jobDir, dagFile.getName());
		dagFile.write(dagOut);

		AppManDBHelper.createJob(userName, dagFile.getName(), id);

		if (!isExehdaRunning()) {
			AppManDBHelper.updateJobFailed(id);
			throw new BusinessException("Exehda não está disponível.");
		}

		return id;
	}

	private static void deleteFile(File f) {
		if (!f.exists()) return;
		if (f.isDirectory()) {
			for (File child : f.listFiles())
				deleteFile(child);
		}
		f.delete();
	}

	public static void moveFile(File srcFile, File destFile) throws IOException {
		if (srcFile.isDirectory()) {
			destFile.mkdirs();
			for (File child : srcFile.listFiles())
				moveFile(child, new File(destFile, child.getName()));
		} else {
			if (!destFile.exists()) {
				destFile.createNewFile();
			}
			FileChannel src = new FileInputStream(srcFile).getChannel();
			FileChannel dest = new FileOutputStream(destFile).getChannel();
			dest.transferFrom(src, 0, src.size());
			src.close();
			dest.close();
		}
		srcFile.delete();
	}

	public static void startJob(AppManJob job) throws IOException, InterruptedException, SQLException {
		AppManDBHelper.updateJobStart(job.getId());

		File logs = new File(AppManConfig.get().getString("exehda.log.dir"));
		File[] logFiles = logs.listFiles();
		if (logFiles != null) {
			for (File log : logFiles) {
				if (!"exehda.log".equals(log.getName())) deleteFile(log);
			}
		}

		// ${exehda.isam.run} ${appman.isam.console} -- ${appman.portlets.dag.dir}/dagfile
		ArrayList<String> commandParams = new ArrayList<String>();

		commandParams.add(AppManConfig.get().getString("exehda.isam.run"));
		commandParams.add(AppManConfig.get().getString("appman.isam.console"));
		commandParams.add("--");

		File jobDir = new File(AppManConfig.get().getString("appman.portlets.job.dir"), String.valueOf(job.getId()));
		jobDir.mkdirs();
		File dagFile = new File(jobDir, job.getFile());
		commandParams.add(dagFile.getCanonicalPath());
		commandParams.add(String.valueOf(job.getId()));

		ProcessBuilder builder = new ProcessBuilder(commandParams.toArray(new String[commandParams.size()]));
//		builder.redirectErrorStream(true);
		Process proc = builder.start();
		proc.waitFor();

		String out = readStreamData(proc.getInputStream());
		if (out.length() != 0) writeToFile(out, new File(jobDir, "std.out"));

		out = readStreamData(proc.getErrorStream());
		if (out.length() != 0) {
			writeToFile(out, new File(jobDir, "std.err"));
			if (out.contains("Exception in thread \"main\"")) {
				AppManDBHelper.updateJobFailed(job.getId());
			}
		}
	}

	private static void writeToFile(String data, File file) throws IOException {
		FileWriter writer = new FileWriter(file, false);
		writer.write(data);
		writer.close();
	}

	private static String readStreamData(InputStream is) throws IOException {
		StringBuilder buff = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(is);
		char[] cbuff = new char[1024];
		for (int read; (read = reader.read(cbuff)) != -1;) {
			buff.append(cbuff, 0, read);
		}
		reader.close();
		return buff.toString();
	}

	public static void organizeFinishedJob(AppManJob job) throws IOException {
		File jobDir = new File(AppManConfig.get().getString("appman.portlets.job.dir"), String.valueOf(job.getId()));
		jobDir.mkdirs();

		File logs = new File(AppManConfig.get().getString("exehda.log.dir"));
		File[] logFiles = logs.listFiles();
		if (logFiles != null) {
			for (File log : logFiles) {
				if (!"exehda.log".equals(log.getName())) moveFile(log, new File(jobDir, log.getName()));
			}
		}
	}

	public static boolean isExehdaRunning() throws BusinessException {
		return isExehdaRunning(AppManConfig.get().getString("exehda.gatekeeper"));
	}

	public static boolean isExehdaRunning(String machine) throws BusinessException {
		if (true) return true;

		Properties props = new Properties();
		props.put("isam.gatekeeper.host", "localhost");
		props.put("isam.gatekeeper.port", "29901");

		File isamConfig = new File(System.getProperty("user.home"), ".isam_config");
		if (isamConfig.exists()) {
			try {
				FileInputStream fis = new FileInputStream(isamConfig);
				props.load(fis);
				fis.close();
			} catch (IOException e) {
				log.error("erro carregando " + isamConfig.getAbsolutePath(), e);
			}
		}
		String host = props.getProperty("isam.gatekeeper.host");
		int port = Integer.parseInt(props.getProperty("isam.gatekeeper.port"));
		if (machine != null) {
			String[] parts = machine.split(":");
			host = parts[0];
			if (parts.length == 2) port = Integer.parseInt(parts[1]);
		}

		try {
			new Socket(host, port).close();
		} catch (UnknownHostException e) {
			throw new BusinessException("Não foi possível resolver host do exehda: " + host, e);
		} catch (IOException e) {
			log.warn("exehda não está aceitando conexões: " + host + ":" + port);
			return false;
		}
		return true;
	}

	public static void deleteJob(int jobId) throws SQLException, NamingException {

		LdapSession session = AppManLdapHelper.createSession();
		AppManLdapHelper.finalizeApplication(session, AppManDBHelper.getAppId(jobId));
		session.close();
		AppManDBHelper.deleteJob(jobId);

		// faz exclusão lógica, e mantém os arquivos de log
//		deleteFile(new File(AppManConfig.get().getString("appman.portlets.job.dir"), String.valueOf(jobId)));

	}

	public static void stopApplication(LdapSession session, String appId) throws NamingException, SQLException {
		AppManLdapHelper.finalizeApplication(session, appId);
		AppManDBHelper.finalizeApplication(appId);
	}

	public static boolean isJobFinished(int id) throws SQLException {

		boolean finished = AppManDBHelper.isJobFinished(id);
		if (finished) return true;

/*		File file = new File(AppManConfig.get().getString("exehda.log.dir")
					+ "/appman_contact_adress.txt");
		if (file.exists()) {
			try {
				RandomAccessFile fReader = new RandomAccessFile(file, "r");
				String contact_address = fReader.readLine();
				fReader.close();
				try {
					ApplicationManagerRemote appman = (ApplicationManagerRemote) AppManUtil.getWorb().lookupService(
						contact_address, ApplicationManagerRemote.class);
					appman.isAlive();
					return false;

				} catch (Exception e) {
					log.error("verificando se processo está ativo", e);
					return true;
				}
			} catch (IOException e) {
				log.warn("abrindo arquivo para comunicação", e);
			}
		}
*/		return false;
	}
}
