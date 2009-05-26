package org.isam.exehda.services.bda;

import java.io.IOException;

import org.isam.exehda.Exehda;
import org.isam.exehda.services.Logger;
import org.isam.exehda.services.Service;
import org.isam.exehda.services.bda.util.ServerWorker;
import org.isam.perv.protocols.bda.Handler;

public class BdaImpl implements Service {

	public static final String CLS_NAME = "BdaImpl";
	public static final int BDA_OP_READ = 0;
	public static final int BDA_OP_WRITE = 1;

	public final Logger log = (Logger) Exehda.getService("logger");
	private String docRoot;

	private int port;
	private ServerWorker server;

	public void start() throws Exception {

		this.docRoot = Exehda.getServiceProperty("bda", "docroot", "./");

		port = Exehda.getServiceProperty("bda", "port", 1978);

		Handler.config("localhost", port);
		setupServer();
	}

	private void setupServer() throws IOException {
		server = new ServerWorker(this, port);
		server.start();
	}

	public void stop() throws Exception {
		if (server != null) {
			server.close();
			server = null;
		}
	}

	public String getDocRoot() {
		return docRoot;
	}

	public boolean checkAccess(String fileName, String userName, int op) {
		return true;
	}

}
