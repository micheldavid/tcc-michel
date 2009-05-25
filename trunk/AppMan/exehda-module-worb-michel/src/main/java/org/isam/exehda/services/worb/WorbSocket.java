package org.isam.exehda.services.worb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.isam.exehda.HostId;

class WorbSocket extends Socket
{
  private HostId remoteHost = null;
  private static final byte[] LOCAL_HOST_BYTES;

  WorbSocket()
  {
  }

  WorbSocket(String addr, int port)
    throws IOException
  {
    super(addr, port);

    doHostHandshake();
  }

  void doHostHandshake()
    throws IOException
  {
    try
    {
      writeLocalHostId();
      readRemoteHostId();
    }
    catch (ClassNotFoundException cnfe) {
      throw new IOException("Host hand-shake failed: " + cnfe);
    }
  }

  HostId getRemoteHost()
  {
    return this.remoteHost;
  }

  private final void readRemoteHostId()
    throws IOException, ClassNotFoundException
  {
    byte[] nb = new byte[4];
    readBytes(nb, 0, nb.length);

    int n = 0;
    int i = 0; for (int j = 0; j < nb.length; ) {
      n |= (nb[j] & 0xFF) << i;

      i += 8; ++j;
    }

    byte[] buf = new byte[n];
    readBytes(buf, 0, buf.length);

    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
    this.remoteHost = ((HostId)ois.readObject());
  }

  private final void writeLocalHostId()
    throws IOException
  {
    OutputStream os = getOutputStream();

    int n = LOCAL_HOST_BYTES.length;

    for (int i = 0; i < 32; ) { os.write(n >> i & 0xFF); i += 8;
    }

    os.write(LOCAL_HOST_BYTES);
    os.flush();
  }

  private final void readBytes(byte[] buf, int offset, int n)
    throws IOException
  {
    InputStream is = getInputStream();

    while (n > 0) {
      int num = is.read(buf, offset, n);

      if (num > 0) {
        n -= num;
        offset += num;
      }
      else {
        throw new IOException("Unexpected end-of-stream");
      }
    }
  }

  static {
    ByteArrayOutputStream bout;
    try {
      bout = new ByteArrayOutputStream(4096);
      ObjectOutputStream out = new ObjectOutputStream(bout);

      out.writeObject(HostId.getLocalHost());
      out.close();

      LOCAL_HOST_BYTES = bout.toByteArray();
    }
    catch (IOException ioe) {
      throw new ExceptionInInitializerError(ioe);
    }
  }
}