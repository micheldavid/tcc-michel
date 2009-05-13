/*
 * Created on 25/11/2004
 */
package appman.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lucasa
 */
public class Debug {
	private static final Log log = LogFactory.getLog(Debug.class);

	public static void newDebugFile(String str, String filepath) {
		File file = new File(filepath);
		if (file.exists()) file.delete();

		debugToFile(str, filepath, false);
	}

	public static void debugToFile(Object str, String filepath, boolean stdOut) {
		File file = new File(filepath);
		try {
			if (!file.exists()) file.createNewFile();

			FileOutputStream fout = new FileOutputStream(file, true);
			OutputStreamWriter output = new OutputStreamWriter(fout);
			output.write(String.valueOf(str) + "\n");
			output.close();
			fout.close();
			if (stdOut) {
				System.out.println(str);
			}
		} catch (IOException e) {
			log.error("debug para arquivo [" + filepath + "]: " + str, e);
		}
	}
}
