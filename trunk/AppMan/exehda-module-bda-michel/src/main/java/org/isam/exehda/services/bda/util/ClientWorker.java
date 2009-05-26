package org.isam.exehda.services.bda.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import org.isam.exehda.HostId;
import org.isam.exehda.services.bda.BdaImpl;

public class ClientWorker extends Thread {
	private static final String CLS_NAME = "BdaImpl.ClientWorker";
	private BdaImpl impl;
	private Socket client;
	private HostId remoteHost;

	public ClientWorker(BdaImpl impl, Socket client) {
		setName("bda-client:" + client.getInetAddress());
		this.impl = impl;
		this.client = client;
	}

	public HostId getRemoteHost() {
		return remoteHost;
	}

	@Override
	public void run() {
		try {
			doRun();
		} catch (Throwable th) {
			impl.log.error(CLS_NAME, "ClientWorker.run", th);
		}
	}

	private void doRun() throws IOException {
		BufferedInputStream in = new BufferedInputStream(client.getInputStream());
		OutputStream out = client.getOutputStream();

		try {
			// lendo cabeçalhos
			HashMap<String, String> headers = readHeaders(in, out);

			// processando requisição
			processRequest(headers, in, out);
		} catch (Exception ex) {
			impl.log.error(CLS_NAME, "ClientWorker.doRun", ex);
		} finally {
			in.close();
			out.close();
			client.close();
		}
	}

	private HashMap<String, String> readHeaders(InputStream in, OutputStream out) throws IOException {
		HashMap<String, String> headers = new HashMap<String, String>();
		StringBuilder strBuff = new StringBuilder(2048);
		// evitando InputStream.mark
		int c;
		while ((c = in.read()) != -1) {
			if (c == '\r') {
				strBuff.append((char) c);
				strBuff.append((char) (c = in.read()));
				if (c != '\n') continue;
				strBuff.append((char) (c = in.read()));
				if (c != '\r') continue;
				strBuff.append((char) (c = in.read()));
				if (c != '\n') continue;
				break;
			} else {
				strBuff.append((char) c);
			}
		}
		if (c == -1) throw new IOException("end of stream");

		for (String line : strBuff.toString().split("\r\n")) {
			if (line.length() == 0) break;
			if (headers.isEmpty())
				headers.put("Resource", line.substring(line.indexOf(' ') + 1, line.lastIndexOf(' ')));
			else {
				int sepIdx = line.indexOf(':');
				if (sepIdx < 0)
					impl.log.error(CLS_NAME, "readingHeaders-sem':'", line, null);
				else {
					headers.put(line.substring(0, sepIdx), line.substring(sepIdx + 1).trim());
				}
			}
		}
		return headers;
	}

	private void processRequest(HashMap<String, String> headers, InputStream in, OutputStream out) throws IOException {

		// trabalhando a requisição
		if (isBdaWrite(headers)) {
			handleBdaWrite(headers, in, out);
		} else {
			handleBdaRead(headers, in, out);
		}
	}

	private boolean isBdaWrite(HashMap<String, String> headers) {
		return "write".equals(headers.get("Isam-Bda-Operation"));
	}

	protected void handleBdaRead(HashMap<String, String> headers, InputStream in, OutputStream out) throws IOException {
		String fileName = headers.get("Resource");
		String userName = headers.get("Isam-User");

		if (impl.checkAccess(fileName, userName, BdaImpl.BDA_OP_READ)) {
			File file = new File(impl.getDocRoot() + fileName);
			InputStream ins = new FileInputStream(file);
			writeHttpResponse(out, 200, ins, (int) file.length());
			ins.close();
		} else {
			writeHttpResponse(out, 400, null, 0);
		}
	}

	protected void handleBdaWrite(HashMap<String, String> headers, InputStream in, OutputStream out) {}

	private void writeHttpResponse(OutputStream out, int code, InputStream data, int length) throws IOException {
		StringBuilder strOut = new StringBuilder();
		strOut.append("HTTP/1.0 ").append(code).append("\r\n");
		if (data != null) {
			strOut.append("Content-Length: ").append(length).append("\r\n\r\n");
			out.write(strOut.toString().getBytes());
			byte[] buf = new byte[1048576];
			for (int read; (read = data.read(buf)) != -1;)
				out.write(buf, 0, read);
		} else {
			strOut.append("\r\n");
			out.write(strOut.toString().getBytes());
		}
	}
}
