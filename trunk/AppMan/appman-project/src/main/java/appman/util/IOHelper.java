package appman.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class IOHelper {

	private IOHelper() {}

	public static byte[] readInputStream(InputStream in) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		for (int read; (read = in.read(buff)) != -1;)
			bout.write(buff, 0, read);
		in.close();
		return bout.toByteArray();
	}

	/**
	 * Este método deve ser evitado, pois cria uma String baseada nos bytes convertidos do encoding padrão do sistema.
	 * Se uma das máquinas estiver usando outro encoding (ex: ISO-8859-1) provavelmente teremos um erro.
	 * 
	 * @param in
	 * @return String de in convertido pelo Encoding padrão do sistema
	 * @throws IOException
	 */
	public static String readCharInputStream(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] cbuff = new char[1024];
		InputStreamReader reader = new InputStreamReader(in);
		for (int read; (read = reader.read(cbuff)) != -1;)
			sb.append(cbuff, 0, read);
		in.close();
		return sb.toString();
	}

	/**
	 * Copia os dados de entrada de in para out
	 * @param in
	 * @param out
	 * @throws IOException 
	 */
	public static void transferInputStreamData(InputStream in, OutputStream out) throws IOException {
		byte[] buff = new byte[8192];
		for (int read; (read = in.read(buff)) != -1;)
			out.write(buff, 0, read);
	}

	public static void copyFile(File from, File to) throws IOException {
		// Create channel on the source
		FileChannel srcChannel = new FileInputStream(from).getChannel();

		// Create channel on the destination
		FileChannel dstChannel = new FileOutputStream(to).getChannel();

		// Copy file contents from source to destination
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		// Close the channels
		srcChannel.close();
		dstChannel.close();
	}
	
	public static void removeDir(File dir) {
		File[] files = dir.listFiles();
		if (files == null) return;
		for (File f : files) {
			if (f.isDirectory()) removeDir(f);
			f.delete();
		}
		dir.delete();
	}

}
