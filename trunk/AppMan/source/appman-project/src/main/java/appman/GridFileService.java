/*
 * Created on 08/06/2004
 */
package appman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.log.Debug;

/**
 * @author lucasa
 *  
 */
public class GridFileService implements GridFileServiceRemote, Serializable {

	private static final long serialVersionUID = -4639171298174076708L;
	private static final Log log = LogFactory.getLog(GridFileService.class);

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

	public static boolean removeDir(String filepath) throws Exception {
		File file = new File(filepath);
		Debug.debug("GridFileService removeDir [" + filepath + "]: "
				+ file.getAbsolutePath());
		if (file.isDirectory()) {
			String[] cmd = { "/bin/bash", "--login", "-c",
					"rm -r " + file.getAbsolutePath() };
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			if (!file.exists())
				return true;
			else
				return false;
		}
		return false;
	}

	/*
	 * Create a disk file in the default directory
	 */
	public synchronized void uploadFile(byte[] filedata, String filepath)
			throws RemoteException {
		try {
			String dir = defaultdir;
			String[] cmd = { "/bin/bash", "--login", "-c", "mkdir -p " + dir };
			Runtime.getRuntime().exec(cmd).waitFor();
			filepath = dir + "/" + filepath;
			Debug.debug("GridFileService uploadFile upload to [" + filepath + "]");
			File file = new File(filepath);
			if (file.exists()) {
				return;
			}
			BufferedOutputStream output = new BufferedOutputStream(
					new FileOutputStream(file.getCanonicalPath()));
			output.write(filedata, 0, filedata.length);
			output.flush();
			output.close();
			file = new File(filepath);
			if (!file.exists()) {
				throw new Exception("Error on creating File: " + filepath);
			}
		} catch (Exception e) {
			log.error("Error uploading File [" + filepath + "]: "
					+ e.getMessage(), e);
			throw new RemoteException("Error uploading File: " + e.getMessage());
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
			throw new RemoteException("Error downloading File: " + e.getMessage(), e);
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
			String dir = defaultdir;
			filepath = dir + "/" + filepath;
			File file = new File(filepath);
			if (file.exists())
				return file;
			else
				throw new Exception("Error File [" + filepath + "] not exists!");
		} catch (Exception e) {
			log.error("Error downloading File: " + e.getMessage(), e);
			throw new RemoteException("Error downloading File: " + e.getMessage(), e);
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
		byte buffer[] = null;
		try {
			File file = new File(filepath);
			buffer = new byte[(int) file.length()];
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(filepath));
			input.read(buffer, 0, buffer.length);
			input.close();
		} catch (Exception e) {
			log.error("Error downloading File: " + e.getMessage(), e);
			throw new RemoteException("Error downloading File: " + e.getMessage(), e);
		}
		return buffer;
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
	public synchronized void installURLFile(String url, String localFile,
			boolean chmod) throws RemoteException {
		try {
			Debug.debug("GridFileService installURLFile calculating default directory");
			String dir = defaultdir;
			Debug.debug("GridFileService installURLFile default directory[" + dir + "]");
			String[] cmd = { "/bin/bash", "--login", "-c", "mkdir -p " + dir };
			Runtime.getRuntime().exec(cmd).waitFor();
			String localpath = dir + "/" + localFile;
			File f = new File(localpath);
			if (f.exists()) {
				Debug.debug("GridFileService File already installed.");
				if (chmod) {
					Runtime.getRuntime().exec("chmod u+x " + localpath)
							.waitFor();
				}
				return;
			}

			//VDN: 17/01/06

			if ((url.indexOf("http") != -1) || (url.indexOf("ftp") != -1)) {
				//Baixa da URL
				Debug.debug("GridTask Installing file " + url + " to " + localpath);
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(new File(localpath)));

				BufferedInputStream in;

				URLConnection conn = (new URL(url)).openConnection();
				//conn.setRequestProperty("Cache-Control:","max-age=0,
				// no-cache");
				//conn.setRequestProperty("Pragma:","no-cache");
				conn.connect();
				in = new BufferedInputStream(conn.getInputStream());

				//Transfere arquivo
				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}

				in.close();
				out.close();

			} else {
				//Copia do disco

				Debug.debug("GridTask Installing file " + url + " to " + localpath);

				try {
					// Create channel on the source
					FileChannel srcChannel = new FileInputStream(url)
							.getChannel();

					// Create channel on the destination
					FileChannel dstChannel = new FileOutputStream(localpath)
							.getChannel();

					// Copy file contents from source to destination
					dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

					// Close the channels
					srcChannel.close();
					dstChannel.close();
				} catch (IOException e) {
					log.error("cópia do arquivo " + url + " para " + localpath, e);
				}
			}

			//					if ( chmod ) {
			//						Runtime.getRuntime().exec("chmod u+x "+localpath).waitFor();
			//					}

			Debug.debug("GridTask Files installation completed.");
		} catch (Exception e) {
			System.out.println("[GridFileService]:");
			throw new RemoteException(e.toString());
		}
	}

	/**
	 * Describe class <code>InputStreamHandler</code> here.
	 *  
	 */
	class InputStreamHandler extends Thread {
		/**
		 * Stream being read
		 */
		private InputStream m_stream;

		/**
		 * The StringBuffer holding the captured output
		 */
		private StringBuffer m_captureBuffer;

		/**
		 * Constructor.
		 * 
		 * @param
		 */
		InputStreamHandler(StringBuffer captureBuffer, InputStream stream) {
			m_stream = stream;
			m_captureBuffer = captureBuffer;
			start();
		}

		/**
		 * Stream the data.
		 */
		@Override
		public void run() {
			try {
				int nextChar;
				while ((nextChar = m_stream.read()) != -1) {
					m_captureBuffer.append((char) nextChar);
				}
			} catch (IOException ioe) {
			}
		}
	}
}