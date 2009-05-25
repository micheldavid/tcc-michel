package org.isam.util.codegen.proxy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import org.isam.perv.IsamClassLoader;

class ProxyClassLoader extends ClassLoader
{
  Hashtable<Class<?>, Class<?>> proxyByIface;

  ProxyClassLoader()
  {
    this.proxyByIface = new Hashtable<Class<?>, Class<?>>();
  }

  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
  {
    Class<?> c = IsamClassLoader.getInstance().loadClass(name);

    if (resolve) {
      resolveClass(c);
    }

    return c;
  }

  public Class loadProxy(Class iface)
    throws ClassNotFoundException
  {
	  Class<?> c = null;
	  // estava gerando LinkageError: duplicate class
	  synchronized (proxyByIface) {
    c = this.proxyByIface.get(iface);

    if (c == null) {
      ProxyInfo pinfo = new ProxyInfo(iface);
      ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);

      DataOutputStream out = new DataOutputStream(buf);
      try
      {
        pinfo.writeExternal(out);
        out.close();
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace();
        throw new ClassNotFoundException(ioe.toString());
      }

      byte[] bytecodes = buf.toByteArray();

      c = defineClass(pinfo.getName(), bytecodes, 0, bytecodes.length);

      this.proxyByIface.put(iface, c);
    }
	  }
	  return c;
  }
}