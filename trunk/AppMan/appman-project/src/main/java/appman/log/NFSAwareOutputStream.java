package appman.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class NFSAwareOutputStream extends OutputStream {

	private File file;

	public NFSAwareOutputStream(String fileName) throws IOException {
		this.file = new File(fileName);
		createFileOutputStream().close();
	}

	private FileOutputStream createFileOutputStream() throws FileNotFoundException {
		return new FileOutputStream(file, true);
	}

	@Override
	public void write(int b) throws IOException {
		FileOutputStream fout = createFileOutputStream();
		try {
			fout.write(b);
		} finally {
			fout.close();
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		FileOutputStream fout = createFileOutputStream();
		try {
			fout.write(b);
		} finally {
			fout.close();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		FileOutputStream fout = createFileOutputStream();
		try {
			fout.write(b, off, len);
		} finally {
			fout.close();
		}
	}
}
