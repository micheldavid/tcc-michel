package org.isam.exehda.services.worb;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

class WSServerRef extends WorbServiceRef {
	private final Object serviceImpl;
	private Hashtable<Long, Method> methodsByHash;

	public WSServerRef(Object impl, Class<?> iface) {
		super(iface);
		this.serviceImpl = impl;
		this.methodsByHash = null;
	}

	public Object invoke(Long opnum, Method m, Object[] params) throws Exception {
		return loadMethod(opnum).invoke(this.serviceImpl, params);
	}

	private Method loadMethod(Long opnum) throws NoSuchAlgorithmException, IOException {
		if (methodsByHash == null) {
			methodsByHash = new Hashtable<Long, Method>();

			Method[] methods = this.serviceInterface.getMethods();
			for (Method m : methods) {
				methodsByHash.put(computeMethodHash(m), m);
			}
		}

		return methodsByHash.get(opnum);
	}
}