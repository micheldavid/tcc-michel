package appman.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;

public class NFSAwareFileAppender extends FileAppender {

	public NFSAwareFileAppender() {}

	public NFSAwareFileAppender(Layout layout, String filename, boolean append, boolean bufferedIO, int bufferSize)
		throws IOException {
		this.layout = layout;
		this.setFile(filename, append, bufferedIO, bufferSize);
	}

	public NFSAwareFileAppender(Layout layout, String filename, boolean append) throws IOException {
		this.layout = layout;
		this.setFile(filename, append, false, bufferSize);
	}

	public NFSAwareFileAppender(Layout layout, String filename) throws IOException {
		this(layout, filename, true);
	}

	/**
	 * @see FileAppender#setFile(String, boolean, boolean, int)
	 */
	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
		throws IOException {
		LogLog.debug("setFile called: " + fileName + ", " + append);

		reset();

		NFSAwareOutputStream ostream = null;
		try {
			ostream = new NFSAwareOutputStream(fileName);
		} catch (FileNotFoundException ex) {
			File parent = new File(fileName).getParentFile();
			if (parent != null) {
				if (!parent.exists() && parent.mkdirs()) {
					ostream = new NFSAwareOutputStream(fileName);
				} else {
					throw ex;
				}
			} else {
				throw ex;
			}
		}

		Writer fw = createWriter(ostream);
		if (bufferedIO) {
			fw = new BufferedWriter(fw, bufferSize);
		}
		this.setQWForFiles(fw);
		this.fileName = fileName;
		this.fileAppend = append;
		writeHeader();
		LogLog.debug("setFile ended");
	}
}
