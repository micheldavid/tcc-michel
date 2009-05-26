package org.isam.exehda.services.bda;

import org.isam.exehda.Exehda;
import org.isam.exehda.services.Logger;
import org.isam.exehda.services.Service;
import org.isam.perv.protocols.bda.Handler;

public class BdaImplClient implements Service {
	private static final String SERVICE_NAME = "bda";
	private static final int DEFAULT_PORT = 1978;
	private static final String DEFAULT_HOST = "localhost";

	public void start() throws Exception {
		String bdaHost = Exehda.getServiceProperty(SERVICE_NAME, "bdaHost", DEFAULT_HOST);

		int bdaPort = Exehda.getServiceProperty(SERVICE_NAME, "bdaPort", DEFAULT_PORT);

		Handler.config(bdaHost, bdaPort);

		((Logger) Exehda.getService("logger")).info("BdaImplClient", "start()", "Set bdaHost to \"{0}\"",
			new String[] { bdaHost });
	}

	public void stop() throws Exception {}
}