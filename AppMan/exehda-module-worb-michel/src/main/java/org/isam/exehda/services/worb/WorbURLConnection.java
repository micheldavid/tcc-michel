package org.isam.exehda.services.worb;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class WorbURLConnection extends URLConnection implements WorbProtocolConstants {
	private static final int BUFF_SIZE = 4096;
	private static final String HTTP_VERSION_1_0 = "HTTP/1.0";
	private static final String HTTP_VERSION_1_1 = "HTTP/1.1";
	private static final String HTTP_PROP_CONTENT_TYPE = "Content-Type";
	private static final String HTTP_PROP_CONTENT_LENGTH = "Content-Length";
	private Socket sock;
	private ByteArrayOutputStream outs;
	private int rpContentLength = -1;
	private String rpProtoVersion = null;
	private String rpOpnum = null;
	private String rpOpHash = null;
	private String rpOpSession = null;
	private String rpOpResume = null;
	private String rpContentType = "application/x-java-serialized-object";

	public WorbURLConnection(URL url) {
		super(url);

		this.sock = null;
		this.outs = null;

		if (!("worb".equals(this.url.getProtocol())))
			throw new IllegalArgumentException("Unsupported protocol: " + this.url.getProtocol());
	}

	public void connect() throws IOException {
		if (!(this.connected)) {
			this.sock = createSocket(this.url.getHost(), this.url.getPort());

			this.connected = true;
		}
	}

	public void setRequestProperty(String k, String v) {
		if (keysMatch("Isam-Worb-Opnum", k)) {
			this.rpOpnum = v;
		} else if (keysMatch("X-Worb-Op-Hash", k)) {
			this.rpOpHash = v;
		} else if (keysMatch("X-Worb-Op-Session", k)) {
			this.rpOpSession = v;
		} else if (keysMatch("X-Worb-Op-Resume", k)) {
			this.rpOpResume = v;
		} else if (keysMatch("X-Worb-Version", k)) this.rpProtoVersion = v;
	}

	public String getRequestProperty(String k) {
		if (keysMatch("Isam-Worb-Opnum", k)) return this.rpOpnum;

		if (keysMatch("X-Worb-Op-Hash", k)) return this.rpOpHash;

		if (keysMatch("X-Worb-Op-Session", k)) return this.rpOpSession;

		if (keysMatch("X-Worb-Op-Resume", k)) return this.rpOpResume;

		if (keysMatch("X-Worb-Version", k)) {
			return this.rpProtoVersion;
		}

		return null;
	}

	public InputStream getInputStream() throws IOException {
		connect();

		OutputStream out = this.sock.getOutputStream();

		this.outs.flush();
		byte[] reqData = this.outs.toByteArray();

		this.rpContentLength = reqData.length;

		writeHeaders(out);

		out.write(reqData);
		out.flush();

		return checkResponseCode(this.sock.getInputStream());
	}

	public OutputStream getOutputStream() throws IOException {
		if (this.doOutput) {
			if (this.outs == null) {
				this.outs = new ByteArrayOutputStream(BUFF_SIZE);
			}

			return this.outs;
		}

		throw new ProtocolException("Try to write to a read-only stream.");
	}

	public String getContentType() {
		return this.rpContentType;
	}

	public int getContentLength() {
		return this.rpContentLength;
	}

	public void disconnect() {
		if (this.connected) try {
			this.sock.close();
		} catch (Exception e) {} finally {
			this.connected = false;
			this.sock = null;
		}
	}

	private final Socket createSocket(String ca, int port) throws IOException {
		if (ca.startsWith("tcp:")) {
			return new WorbSocket(ca.substring(4), port);
		}

		return new Socket(ca, port);
	}

	private final void writeHeaders(OutputStream out) throws IOException {
		String CRLF = "\r\n";
		String PROP_SEP = ": ";
		String HTTP_CHARSET = "ISO8859-1";

		StringBuffer hbuf = new StringBuffer(512);

		hbuf.append("POST ").append(this.url.getFile()).append(' ').append(HTTP_VERSION_1_0).append(CRLF);

		hbuf.append(HTTP_PROP_CONTENT_TYPE).append(PROP_SEP).append(this.rpContentType).append(CRLF);

		hbuf.append(HTTP_PROP_CONTENT_LENGTH).append(PROP_SEP).append(this.rpContentLength).append(CRLF);

		if (this.rpOpnum != null) {
			hbuf.append("Isam-Worb-Opnum").append(PROP_SEP).append(this.rpOpnum).append(CRLF);
		}

		if (this.rpOpHash != null) {
			hbuf.append("X-Worb-Op-Hash").append(PROP_SEP).append(this.rpOpHash).append(CRLF);
		}

		if (this.rpOpSession != null) {
			hbuf.append("X-Worb-Op-Session").append(PROP_SEP).append(this.rpOpSession).append(CRLF);
		}

		if (this.rpOpResume != null) {
			hbuf.append("X-Worb-Op-Resume").append(PROP_SEP).append(this.rpOpResume).append(CRLF);
		}

		hbuf.append(CRLF);

		out.write(hbuf.toString().getBytes(HTTP_CHARSET));
	}

	private final InputStream checkResponseCode(InputStream in) throws IOException {
		int BUFF_SIZE = 512;
		in = new BufferedInputStream(in);
		int c = -1;
		int pos = 0;
		byte buff[] = new byte[BUFF_SIZE];
		int respCode = -1;
		while ((c = in.read()) != -1)
			if (13 == c) {
				if (10 == in.read()) {
					if (pos == 0) return in;
					String line = new String(buff, 1, pos);
					if (respCode < 0)
						respCode = parseResponseCode(line);
					else
						parseResponseHeader(line);
					pos = 0;
				} else {
					buff[++pos] = (byte) c;
					try {
						sock.close();
					} catch (IOException ioe) {}
					throw new ProtocolException("Malformed response: " + new String(buff, 1, pos));
				}
			} else {
				buff[++pos] = (byte) c;
			}
		try {
			sock.close();
		} catch (IOException ioe) {}
		throw new EOFException("reached end-of-stream while parsing response headers");
	}

	private final boolean keysMatch(String k1, String k2) {
		return ((k1.length() == k2.length()) ? k1.endsWith(k2) : false);
	}

	private final int parseResponseCode(String line) throws IOException {
		String pv = line.substring(0, 8);

		if ((HTTP_VERSION_1_0.equals(pv)) || (HTTP_VERSION_1_1.equals(pv))) {
			int respCode = Integer.parseInt(line.substring(9, 12));

			if ((respCode >= 200) && (respCode < 299)) {
				return respCode;
			}
			try {
				this.sock.close();
			} catch (IOException ioe) {}
			throw new ProtocolException(line.substring(9));
		}

		try {
			this.sock.close();
		} catch (IOException ioe) {}
		throw new ProtocolException("Unsupported protocol version: <" + pv + ">");
	}

	private final void parseResponseHeader(String line) throws IOException {
		String HEADER_CTYPE = "Content-Type: ";
		String HEADER_CLEN = "Content-Length: ";

		if (line.startsWith(HEADER_CTYPE)) {
			this.rpContentType = line.substring(HEADER_CTYPE.length());
		} else if (line.startsWith(HEADER_CLEN))
			this.rpContentLength = Integer.parseInt(line.substring(HEADER_CLEN.length()));
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("java.protocol.handler.pkgs", "org.isam.perv.protocols");

		String url = (args.length > 0) ? args[0] : "worb:tcp://127.0.0.1:1980/obj/mtd";

		URLConnection wcon = new URL(url).openConnection();
		wcon.setDoOutput(true);

		System.out.println("X-Worb-Op-Hash=" + wcon.getRequestProperty("X-Worb-Op-Hash"));

		wcon.setRequestProperty("X-Worb-Op-Hash", "AABBDD");

		System.out.println("X-Worb-Op-Hash=" + wcon.getRequestProperty("X-Worb-Op-Hash"));

		OutputStream out = wcon.getOutputStream();

		out.write("teste teste teste teste\n".getBytes());

		InputStream in = wcon.getInputStream();

		System.out.println("Content-Length=" + wcon.getContentLength());
		System.out.println("Content-Type=" + wcon.getContentType());
		System.out.println("----------");

		System.out.print((char) in.read());
	}
}
