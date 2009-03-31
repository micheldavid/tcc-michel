package appman.rmswrapper.pbs.drmaa;

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

public class SessionFactoryImpl extends SessionFactory {
	private Session session = null;

	public SessionFactoryImpl() {
	}

	public Session getSession() {
		synchronized (this) {
			if (session == null) {
				session = new SessionImpl();
			}
		}
		return session;
	}
}
