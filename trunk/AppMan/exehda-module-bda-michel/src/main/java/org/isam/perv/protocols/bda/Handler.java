package org.isam.perv.protocols.bda;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler
{
  static transient String bdaHost = null;
  static transient int bdaPort;

  public static void config(String h, int port)
  {
    if (bdaHost == null) {
      bdaHost = h;
      bdaPort = port;
    }
  }

  protected URLConnection openConnection(URL url)
    throws IOException
  {
    String resourcePath = "/" + url.getHost() + "/" + url.getFile();

    URLConnection bdaConn = new URL("http", bdaHost, bdaPort, resourcePath).openConnection();

    bdaConn.setDoOutput(true);
    bdaConn.setRequestProperty("Isam-Bda-Operation", "read");
    return bdaConn;
  }

}