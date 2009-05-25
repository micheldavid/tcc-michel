package org.isam.exehda.services.worb;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.isam.util.codegen.Types;

public abstract class WorbServiceRef {
	protected Class<?> serviceInterface;

	protected WorbServiceRef(Class<?> serviceIface) {
		this.serviceInterface = serviceIface;
	}

	public abstract Object invoke(Long paramLong, Method paramMethod, Object[] paramArrayOfObject) throws Exception;

	protected long computeMethodHash(Method m) throws NoSuchAlgorithmException, IOException {
		String methodName = m.getName() + Types.getDescriptor(m);

		ByteArrayOutputStream bs = new ByteArrayOutputStream(512);

		MessageDigest md = MessageDigest.getInstance("SHA");

		DataOutputStream ds = new DataOutputStream(new DigestOutputStream(bs, md));

		ds.writeUTF(methodName);
		ds.flush();

		byte[] sha = md.digest();

		long hash = 0L;

		int i = 0;
		for (int s = 0; i < 8;) {
			hash += ((sha[i] & 0xFF) << s);

			++i;
			s += 8;
		}

		return hash;
	}
}