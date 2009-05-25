package org.isam.exehda.services.worb.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import org.isam.exehda.services.worb.WorbImpl;
import org.isam.exehda.services.worb.WorbObjectInputStream;
import org.isam.exehda.services.worb.WorbServiceRef;

public class InvokeAction implements Runnable {
	private static final String CLS_NAME = "WorbImpl.InvokeAction";
	private WorbServiceRef wsref;
	private InputStream rawin;
	private Long opnum;
	private byte[] resultData;
	private Exception e;
	private ClientWorker client;
	private WorbImpl impl;

	public InvokeAction(WorbServiceRef wsref, Long opnum, InputStream rawin, ClientWorker client, WorbImpl impl) {
		this.wsref = wsref;
		this.opnum = opnum;
		this.rawin = rawin;
		this.e = null;
		this.client = client;
		this.impl = impl;
	}

	public void run() {
		try {
			WorbObjectInputStream in = new WorbObjectInputStream(rawin);

			in.readObject();

			Object[] params = (Object[]) in.readObject();
			if (params == null) params = new Object[0];

			Object result = null;
			Exception exception = null;
			try {
				impl.setThreadClient(client);

				result = this.wsref.invoke(this.opnum, null, params);

				impl.removeThreadClient();
			} catch (Exception ex) {
				exception = ex;
			} finally {
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(buf);

				out.writeObject(exception);
				out.writeObject(result);
				out.close();

				this.resultData = buf.toByteArray();
			}
		} catch (Exception ex) {
			e = ex;
			impl.log.error(CLS_NAME, "run", ex);
		}
	}

	public byte[] getResult() throws Exception {
		if (e != null) throw e;

		return this.resultData;
	}
}