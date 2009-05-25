package org.isam.exehda.services.worb.util;

import java.io.IOException;
import java.net.ServerSocket;

import org.isam.exehda.services.worb.WorbImpl;

public class ServerWorker extends Thread {

	private static final String CLS_NAME = "WorbImpl.ServerWorker";
	private WorbImpl impl;
	private ServerSocket server;

	public ServerWorker(WorbImpl impl, int port) throws IOException {
		setName("worb-server:" + port);
		this.impl = impl;
		this.server = new ServerSocket(port, 100);
	}
	
	public void close() throws IOException {
		if (server != null) {
			server.close();
		}
	}

	@Override
	public void run() {
		try {
			for (;;)
				new ClientWorker(impl, server.accept()).start();
		} catch (IOException e) {
			impl.log.error(CLS_NAME, "ServerWorker.run()", e);
		}
	}
}
