package org.isam.exehda.services.worb.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.isam.exehda.Exehda;
import org.isam.exehda.HostId;
import org.isam.exehda.ResourceName;
import org.isam.exehda.services.CellInformationBase;
import org.isam.exehda.services.Executor;

public class WorbHelper {

	public static byte[] readBytes(InputStream is, int length) throws IOException {
		byte[] buff = new byte[length];
		int pos = 0;
		for (int read; pos != length && (read = is.read(buff, pos, length - pos)) != -1; pos += read);

		if (pos != length) throw new IOException("Unexpected end of stream");

		return buff;
	}

	public static void writeLocalHostId(OutputStream os, byte[] localHost) throws IOException {
		int n = localHost.length;

		// tamanho
		for (int i = 0; i < 32; i += 8)
			os.write(n >> i & 0xFF);

		os.write(localHost);
		os.flush();
	}

	public static HostId readRemoteHostId(InputStream is) throws IOException, ClassNotFoundException {
		byte[] nb = readBytes(is, 4);

		int n = 0;
		for (int i = 0, j = 0; j < nb.length; i += 8, ++j) {
			n |= (nb[j] & 0xFF) << i;
		}

		byte[] buf = readBytes(is, n);

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
		try {
			return (HostId) ois.readObject();
		} finally {
			ois.close();
		}
	}

	public static CellInformationBase getCib() {
		return ((CellInformationBase) Exehda.getService("cib"));
	}

	public static Executor getExecutor() {
		return ((Executor) Exehda.getService("executor"));
	}

	public static String toWorbURL(String uri) {
		if (uri.startsWith("worb:")) {
			uri = uri.substring(5);

			if (uri.startsWith("tcp://")) {
				return "worb:" + uri;
			}

			if (uri.startsWith("//hostid:")) {
				HostId hid = HostId.parseId(uri.substring(2, uri.indexOf(47, 3)));
				ResourceName wrn = new ResourceName("service", "worb", hid.toNameSpace());
				String ca = WorbHelper.getCib().getAttribute(wrn, "contactAddress");
				String service = uri.substring(uri.indexOf('/', 3));
				return "worb:" + ca + service;
			}

			throw new IllegalArgumentException("unknown addressing scheme url: " + uri);
		}

		throw new IllegalArgumentException("Unsuported protocol: " + uri);
	}

}
