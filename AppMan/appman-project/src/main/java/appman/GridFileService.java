/*
 * Created on 08/06/2004
 */
package appman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.util.IOHelper;

/**
 * @author lucasa
 */
public class GridFileService implements GridFileServiceRemote, Serializable {

	private static final Log log = LogFactory.getLog(GridFileService.class);
	private static final long serialVersionUID = -4639171298174076708L;
	private String defaultdir = null;

	private final static class SandBoxUtil {
		public static String calculateSandBoxDirPath(String seed) {
			return "./appman-sandbox/"
					+ Integer.toOctalString(AppManUtil.getExecutor()
							.currentApplication().hashCode()
							+ seed.hashCode());
		}

		public static String calculateResultsSandBoxDirPath() {
			return "./appman-sandbox/results/"
					+ Integer.toOctalString(AppManUtil.getExecutor()
							.currentApplication().hashCode());
		}
	}

	public static String getTaskSandBoxPath(String taskName) {
		return SandBoxUtil.calculateSandBoxDirPath(taskName);
	}

	public GridFileService(String filepath_seed) {
		defaultdir = SandBoxUtil.calculateSandBoxDirPath(filepath_seed);
	}

	public GridFileService() {
		defaultdir = SandBoxUtil.calculateResultsSandBoxDirPath();
	}

	public static void removeDir(String filepath) throws Exception {
		File file = new File(filepath);
		log.debug("GridFileService removeDir [" + filepath + "]: "
				+ file.getAbsolutePath());

		IOHelper.removeDir(file);
	}

	/*
	 * Create a disk file in the default directory
	 */
	public synchronized void uploadFile(byte[] filedata, String filepath)
			throws RemoteException {
		try {
			File dir = new File(defaultdir);
			dir.mkdirs();
			File file = new File(dir, filepath);
			log.debug("GridFileService uploadFile upload to [" + file.getCanonicalPath() + "]");
			if (file.exists()) return;

			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file.getCanonicalPath()));
			output.write(filedata, 0, filedata.length);
			output.close();
			if (!file.exists()) {
				throw new Exception("Error on creating File: " + file.getCanonicalPath());
			}
		} catch (Exception e) {
			log.error("Error uploading File [" + filepath + "]: " + e.getMessage(), e);
			throw new RemoteException("Error uploading File: " + e.getMessage(), e);
		}
	}

	/**
	 * Load a disk file from the default directory
	 * 
	 * @param filepath
	 *            a <code>String</code> value
	 * @return a <code>byte[]</code> value
	 * @exception RemoteException
	 *                if an error occurs
	 */
	public synchronized byte[] downloadFile(String filepath)
			throws RemoteException {
		byte buffer[] = null;
		try {
			String dir = defaultdir;
			filepath = dir + "/" + filepath;
			File file = new File(filepath);
			buffer = new byte[(int) file.length()];
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(filepath));
			input.read(buffer, 0, buffer.length);
			input.close();
		} catch (Exception e) {
			log.error("Error downloading File: " + e.getMessage(), e);
			throw new RemoteException("Error downloading File: "
					+ e.getMessage(), e);
		}

		return buffer;
	}

	/**
	 * Return a File object from a file in default directory
	 * 
	 * @param filepath
	 *            a <code>String</code> value
	 * @return a <code>File</code> value
	 * @exception RemoteException
	 *                if an error occurs
	 */
	public synchronized File getFile(String filepath) throws RemoteException {
		try {
			filepath = defaultdir + "/" + filepath;
			File file = new File(filepath);
			if (file.exists())
				return file;
			throw new Exception("Error File [" + filepath + "] not exists!");
		} catch (Exception e) {
			log.error("Error downloading File: " + e.getMessage(), e);
			throw new RemoteException("Error downloading File: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Load a disk file from a path
	 * 
	 * @param filepath
	 *            a <code>String</code> value
	 * @return a <code>byte[]</code> value
	 * @exception RemoteException
	 *                if an error occurs
	 */
	public synchronized byte[] fileToByteArray(String filepath)
			throws RemoteException {
		try {
			File file = new File(filepath);
			byte[] buffer = new byte[(int) file.length()];
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
			input.read(buffer, 0, buffer.length);
			input.close();
			return buffer;
		} catch (Exception e) {
			log.error("Error downloading File: " + e.getMessage(), e);
			throw new RemoteException("Error downloading File: "
					+ e.getMessage(), e);
		}
	}

	public String getDefaultDir() throws RemoteException {
		return defaultdir;
	}

	/**
	 * Create a disk file in the default directory from a url file
	 * 
	 * @param url
	 *            a <code>String</code> value
	 * @param localFile
	 *            a <code>String</code> value
	 * @param chmod
	 *            a <code>boolean</code> value
	 * @exception RemoteException
	 *                if an error occurs
	 */
	public void installURLFile(String url, String localFile) throws RemoteException {
		try {
			log.debug("GridFileService installURLFile calculating default directory");
			String dir = defaultdir;
			log.debug("GridFileService installURLFile default directory[" + dir + "]");

			File installDir = new File(dir);
			installDir.mkdirs();
			File file = new File(installDir, localFile);

			//VDN: 17/01/06

			if ((url.indexOf("http") != -1) || (url.indexOf("ftp") != -1)) {
				//Baixa da URL
				log.debug("GridTask Installing file " + url + " to " + file.getCanonicalPath());
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

				URLConnection conn = new URL(url).openConnection();
				//conn.setRequestProperty("Cache-Control:","max-age=0,no-cache");
				//conn.setRequestProperty("Pragma:","no-cache");
				conn.connect();
				InputStream in = conn.getInputStream();
				IOHelper.transferInputStreamData(in, out);
				in.close();
				out.close();

			} else {
				//Copia do disco
				log.debug("GridTask Installing file " + url + " to " + file.getCanonicalPath());
				IOHelper.copyFile(new File(url), file);
			}

			log.debug("GridTask Files installation completed.");
		} catch (Exception e) {
			log.error("[GridFileService]:", e);
			throw new RemoteException(e.toString(), e);
		}
	}
}