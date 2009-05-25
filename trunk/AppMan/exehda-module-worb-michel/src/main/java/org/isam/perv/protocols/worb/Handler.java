package org.isam.perv.protocols.worb;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.isam.exehda.services.worb.WorbURLConnection;

public class Handler extends URLStreamHandler
{
  protected URLConnection openConnection(URL url)
    throws IOException
  {
    return new WorbURLConnection(url);
  }

  protected void parseURL(URL url, String spec, int start, int limit)
  {
    int idx = spec.indexOf(47, start);
    String transp = spec.substring(start, idx);

    super.parseURL(url, spec, idx, limit);

    String host = url.getHost();
    int port = url.getPort();
    String file = url.getFile();
    String ref = url.getRef();

    setURL(url, "worb", transp + host, port, file, ref);
  }
}