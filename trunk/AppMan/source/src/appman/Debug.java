/*
 * Created on 25/11/2004
 */
package appman;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author lucasa
 * @author rbrosinha (200611)
 */
public class Debug {

	private static PrintWriter pw;

	private static final Logger logger = Logger.getLogger("appman.Debug");

	public static void close() {
		if (pw != null) {
			pw.flush();
			pw.close();
		}
	}

	public static void debug(Object str) {
		log(String.valueOf(str));
	}

	public static void debug(Object str, boolean b) {
		log(String.valueOf(str));
	}

	public static void debugToFile(Object str, String filepath, boolean b) {
	}

	public static void init(String filepath) throws IOException {

		Handler fh = new FileHandler(filepath);
		fh.setFormatter(new SimpleFormatter());
		Logger rootLogger = Logger.getLogger("");
		rootLogger.setUseParentHandlers(false);
		Handler[] handlers = rootLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			rootLogger.removeHandler(handlers[i]);
		}
		rootLogger.addHandler(fh);

		//    
		// try {
		// if (pw != null) {
		// pw.flush();
		// pw.close();
		// }
		// pw = new PrintWriter(new FileWriter(new File(filepath)), true);
		// eventCounter = 0;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public static void log(String message) {
		// if (pw == null) {
		// Debug.init("default.log");
		// }
		// pw.println(String.valueOf(eventCounter++) + "\t" + message);
		logger.log(Level.INFO, message);
	}

	public static void log(String message, Throwable throwable) {
		// if (pw == null) {
		// Debug.init("default.log");
		// }
		// pw.println(String.valueOf(eventCounter++) + "\t" + message);
		// throwable.printStackTrace(pw);
		logger.log(Level.SEVERE, message, throwable);
	}

	public static void newDebugFile(String str, String filepath) {
	}

}
