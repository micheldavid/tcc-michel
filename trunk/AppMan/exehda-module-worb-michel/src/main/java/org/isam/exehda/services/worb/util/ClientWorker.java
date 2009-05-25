package org.isam.exehda.services.worb.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import org.isam.exehda.ApplicationId;
import org.isam.exehda.HostId;
import org.isam.exehda.services.worb.WorbImpl;
import org.isam.exehda.services.worb.WorbServiceRef;

public class ClientWorker extends Thread {
	private static final String CLS_NAME = "WorbImpl.ClientWorker";
	private WorbImpl impl;
	private Socket client;
	private HostId remoteHost;

	public ClientWorker(WorbImpl impl, Socket client) {
		setName("worb-client:" + client.getInetAddress());
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
		BufferedInputStream in = new BufferedInputStream(client.getInputStream(), 1024);
		OutputStream out = client.getOutputStream();

		try {
			doHandshake(in, out);

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

	private void doHandshake(InputStream in, OutputStream out) throws IOException, ClassNotFoundException {
		// handshake
		WorbHelper.writeLocalHostId(out, impl.LOCAL_HOST_BYTES);
		remoteHost = WorbHelper.readRemoteHostId(in);
	}

	private HashMap<String, String> readHeaders(InputStream in, OutputStream out) throws IOException {
		HashMap<String, String> headers = new HashMap<String, String>();
		InputStreamReader reader = new InputStreamReader(in);
		StringBuilder strBuff = new StringBuilder();
		int lastSize, endPos = -1;
		do {
			lastSize = strBuff.length();
			in.mark(1024);
			char[] cbuf = new char[1024];
			int read = reader.read(cbuf);
			if (read == -1) throw new IOException("impossível reconhecer cabeçalho: " + strBuff.toString());

			strBuff.append(cbuf, 0, read);
		} while ((endPos = strBuff.indexOf("\r\n\r\n")) == -1);
		in.reset();
		in.skip(endPos + 4 - lastSize);

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
		String resource = headers.get("Resource");
		Long opnum = Long.valueOf(headers.get("Isam-Worb-Opnum"));

		int idxSlash2 = (resource.endsWith("/")) ? resource.length() - 1 : resource.length();

		int idxSlash1 = resource.lastIndexOf("/", idxSlash2 - 1);

		if (idxSlash1 < 2) {
			impl.log.error(CLS_NAME, "handleRequest", "Malformed URL {0}", new String[] { resource });
			writeHttpResponse(out, 400, null);
		} else {
			String svc = resource.substring(1, idxSlash1);
			String mtd = resource.substring(idxSlash1 + 1, idxSlash2);

			impl.log.info(CLS_NAME, "handleRequest", "Incoming call to {0}.{1}(...)", new String[] { svc, mtd });

			WorbServiceRef wsref = (WorbServiceRef) impl.getServiceByName(svc);
			try {
//				client.setSoLinger(true, 2);
//				client.setReuseAddress(true);

				in.mark(2048);
				ObjectInputStream objIn = new ObjectInputStream(in);
				ApplicationId aid = (ApplicationId) objIn.readObject();
				in.reset();

				InvokeAction action = new InvokeAction(wsref, opnum, in, this, impl);
				WorbHelper.getExecutor().runAction(aid, action);
				byte[] resultData = action.getResult();

				writeHttpResponse(out, 200, resultData);
			} catch (Exception e) {
				impl.log.error(CLS_NAME, "processRequest", e);
				writeHttpResponse(out, 500, null);
			}
		}
	}

	private void writeHttpResponse(OutputStream out, int code, byte[] data) throws IOException {
		StringBuilder strOut = new StringBuilder();
		strOut.append("HTTP/1.0 ").append(code).append("\r\n");
		if (data != null) {
			strOut.append("Content-Length: ").append(data.length).append("\r\n\r\n");
			out.write(strOut.toString().getBytes());
			out.write(data);
		} else {
			strOut.append("\r\n");
			out.write(strOut.toString().getBytes());
		}
	}
}
