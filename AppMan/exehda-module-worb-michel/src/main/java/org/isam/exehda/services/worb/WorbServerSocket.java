package org.isam.exehda.services.worb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class WorbServerSocket extends ServerSocket
{
  WorbServerSocket(int port)
    throws IOException
  {
    super(port);
  }

  public Socket accept()
    throws IOException
  {
    WorbSocket ws = new WorbSocket();

    implAccept(ws);

    ws.doHostHandshake();

    return ws;
  }
}