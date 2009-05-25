package org.isam.exehda.services.worb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.isam.exehda.Exehda;
import org.isam.exehda.HostId;
import org.isam.exehda.ResourceName;
import org.isam.exehda.services.CellInformationBase;
import org.isam.exehda.services.Logger;
import org.isam.exehda.services.Service;
import org.isam.exehda.services.Worb;
import org.isam.exehda.services.worb.util.ClientWorker;
import org.isam.exehda.services.worb.util.ServerWorker;
import org.isam.exehda.services.worb.util.WorbHelper;
import org.isam.util.codegen.proxy.Proxy;

public class WorbImpl implements Service, Worb, WorbProtocolConstants {

	public static final String CLS_NAME = "WorbImpl";
	public final Logger log = (Logger) Exehda.getService("logger");
	public final byte[] LOCAL_HOST_BYTES;

	private ThreadLocal<ClientWorker> currentClient = new ThreadLocal<ClientWorker>();
	private int port;
	private ServerWorker server;
	private HashMap<String, WSServerRef> servicesByName;

	public WorbImpl() throws IOException {
		servicesByName = new HashMap<String, WSServerRef>();

		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(HostId.getLocalHost());
		out.close();

		LOCAL_HOST_BYTES = bout.toByteArray();
	}

	public void start() throws Exception {

		port = Exehda.getServiceProperty("worb", "port", 1980);

		setupServer();

		registerWorbResource();
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

	private void registerWorbResource() {
		(new Thread() {
			public void run() {
				try {
					Thread.sleep(5000);

					CellInformationBase cib = WorbHelper.getCib();

					ResourceName worbRN = new ResourceName("service", "worb", HostId.getLocalHost().toNameSpace());
					cib.addResource(worbRN);

					String contactAddress = "tcp://" + HostId.getLocalHost().getInetAddress().getHostAddress() + ":"
						+ port;
					cib.setAttribute(worbRN, "contactAddress", contactAddress);
				} catch (Exception e) {
					log.error(CLS_NAME, "registerWorbResource", e);
				}
			}
		}).start();
	}

	public WSServerRef getServiceByName(String name) {
		return servicesByName.get(name);
	}

	public String exportService(Object obj, Class iface, String name) {
		if (iface.isAssignableFrom(obj.getClass())) {
			this.servicesByName.put(name, new WSServerRef(obj, iface));
			return "worb://" + HostId.getLocalHost().toExternalForm() + "/" + name;
		}

		throw new ClassCastException(obj + " does not support the provided interface (" + iface + ")");
	}

	public HostId getClientHost() {
		ClientWorker client = currentClient.get();
		if (client == null) return null;
		return client.getRemoteHost();
	}

	public Object lookupService(String service, Class iface) {
		String serviceURL = WorbHelper.toWorbURL(service);

		return Proxy.getInstance(iface, new WSClientRef(serviceURL, iface));
	}

	public void setExceptionHandler(Object paramObject, WorbExceptionHandler paramWorbExceptionHandler) {
		throw new NullPointerException("Not yet implemented.");
	}

	public void unexportService(Object paramObject, String paramString) {
		throw new NullPointerException("Not yet implemented.");
	}

	public void setThreadClient(ClientWorker client) {
		currentClient.set(client);
	}

	public void removeThreadClient() {
		currentClient.remove();
	}

}
