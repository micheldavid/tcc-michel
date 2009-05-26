package org.isam.exehda.services.bda.util;

import java.io.IOException;
import java.net.ServerSocket;

import org.isam.exehda.services.bda.BdaImpl;

public class ServerWorker extends Thread {

	private static final String CLS_NAME = "BdaImpl.ServerWorker";
	private BdaImpl impl;
	private ServerSocket server;

	public ServerWorker(BdaImpl impl, int port) throws IOException {
		setName("bda-server:" + port);
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
