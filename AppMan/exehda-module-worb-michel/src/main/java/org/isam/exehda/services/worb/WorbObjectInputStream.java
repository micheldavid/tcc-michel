package org.isam.exehda.services.worb;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import org.isam.perv.IsamClassLoader;

public class WorbObjectInputStream extends ObjectInputStream {
	public WorbObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	protected Class<?> resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
		return IsamClassLoader.getInstance().loadClass(v.getName());
	}
}