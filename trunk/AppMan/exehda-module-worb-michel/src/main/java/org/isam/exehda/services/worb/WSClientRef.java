package org.isam.exehda.services.worb;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import org.isam.exehda.ApplicationId;
import org.isam.exehda.Exehda;
import org.isam.exehda.services.Executor;
import org.isam.util.codegen.proxy.InvocationHandler;
import org.isam.util.codegen.proxy.Proxy;

class WSClientRef extends WorbServiceRef implements InvocationHandler {
	private final String serviceBaseURL;
	private transient Hashtable hashByMethods;

	WSClientRef(String serviceBaseURL, Class serviceIface) {
		super(serviceIface);
		this.serviceBaseURL = serviceBaseURL;
		this.hashByMethods = null;
	}

	public Object invoke(Long opnum, Method m, Object[] args) throws Exception {
		Object remResult = null;
		Exception remException = null;
		URLConnection conn = null;
		try {
			conn = createConnection(m);

			ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());

			out.writeObject(getCurrentApplication());
			out.writeObject(args);
			out.close();

			WorbObjectInputStream in = new WorbObjectInputStream(conn.getInputStream());

			remException = (Exception) in.readObject();
			remResult = in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disposeConnection(conn);
		}

		if (remException == null) {
			return remResult;
		}

		throw remException;
	}

	public Object invoke(Proxy p, Method m, Object[] args) throws Exception {
		return invoke((Long) null, m, args);
	}

	private final synchronized Long getMethodHash(Method m) throws NoSuchAlgorithmException, IOException {
		Method[] mtds;
		int i;
		if (this.hashByMethods == null) {
			mtds = this.serviceInterface.getDeclaredMethods();

			this.hashByMethods = new Hashtable(mtds.length);

			for (i = 0; i < mtds.length; ++i) {
				this.hashByMethods.put(mtds[i], new Long(computeMethodHash(mtds[i])));
			}

		}

		return ((Long) this.hashByMethods.get(m));
	}

	private final URLConnection createConnection(Method m) throws MalformedURLException, IOException,
		NoSuchAlgorithmException {
		URLConnection conn = new URL(encodeMethod(m)).openConnection();

		conn.setDoOutput(true);

		Long opnum = getMethodHash(m);
		conn.setRequestProperty("Isam-Worb-Opnum", opnum.toString());

		return conn;
	}

	private final void disposeConnection(URLConnection conn) {
		if (conn == null) return;
		if (conn instanceof WorbURLConnection) {
			((WorbURLConnection) conn).disconnect();
		} else if (conn instanceof HttpURLConnection) ((HttpURLConnection) conn).disconnect();
	}

	private final String encodeMethod(Method m) {
		return this.serviceBaseURL + '/' + m.getName();
	}

	private final ApplicationId getCurrentApplication() {
		return ((Executor) Exehda.getService("executor")).currentApplication();
	}
}