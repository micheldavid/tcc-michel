package appman.portlets;

import java.util.ArrayList;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AppManLdapHelper {

	private static final Log log = LogFactory.getLog(AppManLdapHelper.class);

	public static LdapSession createSession() throws NamingException {
		String server = AppManConfig.get().getString("exehda.ldap.server");
		String rootDN = AppManConfig.get().getString("exehda.ldap.rootDN");
		String user = AppManConfig.get().getString("exehda.ldap.user");
		String password = AppManConfig.get().getString("exehda.ldap.password");
		LdapSession session = new LdapSession(server, rootDN, user, password);
		session.open();
		return session;
	}

	/**
	 * @param sessão do ldap
	 * @param appid da aplicação
	 * @param status running, done
	 * @return lista com os xAppID's
	 * @throws NamingException 
	 */
	public static ArrayList<String> searchApplications(LdapSession session, String appid, String status)
	throws NamingException {

	String filter = "objectClass=exehdaApplication";
	if (appid != null) filter = "(&(objectClass=exehdaApplication)(xAppID=" + appid + "))";
	
	ArrayList<String> found = new ArrayList<String>();
	NamingEnumeration<SearchResult> e = (NamingEnumeration<SearchResult>) session.search("",
		filter, new String[] { "xAppID", "xAttr" });
	while (e.hasMore()) {
		SearchResult res = e.next();
		if (status != null) {
			NamingEnumeration<String> attrs = (NamingEnumeration<String>) res.getAttributes().get("xAttr").getAll();
			while (attrs.hasMore()) {
				String[] pair = attrs.next().split(":");
				if ("status".equals(pair[0]) && status.equals(pair[1])) {
					found.add((String) res.getAttributes().get("xAppID").get());
					break;
				}
			}
			attrs.close();
		} else {
			found.add((String) res.getAttributes().get("xAppID").get());
		}
	}
	e.close();
	return found;
}

	public static boolean hasRunningApps(LdapSession session) throws NamingException {

		return !searchApplications(session, null, "running").isEmpty();
	}

	public static void finalizeApplication(LdapSession session, String appid) throws NamingException {

		String filter = "(&(objectClass=exehdaApplication)(xAppID=" + appid + "))";

		NamingEnumeration<SearchResult> e = session.search("", filter, null);
		while (e.hasMore()) {
			SearchResult res = e.next();
			String[] values = session.getMultiValuedNodeAttribute(res.getName(), "xAttr");
			for (int i = 0; i < values.length; i++) {
				String[] pair = values[i].split(":");
				if ("status".equals(pair[0])) {
					values[i] = "status:done";
					session.setMultiValuedNodeAttribute(res.getName(), "xAttr", values);
					break;
				}
			}
		}
		e.close();
	}

	public static ArrayList<String> searchAppIDs() {
		ArrayList<String> appIDs = new ArrayList<String>();
		try {
			LdapSession session = createSession();
			NamingEnumeration<SearchResult> e = session.search("", "objectClass=exehdaApplication",
				new String[] { "xAppID" });
			while (e.hasMore()) {
				SearchResult o = (SearchResult) e.next();
				appIDs.add((String) o.getAttributes().get("xAppID").get());
			}
			e.close();
			session.close();
		} catch (NamingException e) {
			log.error("erro na sessão do ldap", e);
		}
		return appIDs;
	}

	public static void removeAppID(String appID) {
		try {
			LdapSession session = createSession();
			removeAppID(session, appID);
			session.close();
		} catch (NamingException e) {
			log.error("erro na sessão do ldap", e);
		}
	}
	
	public static void removeAppID(LdapSession session, String appID) {
		try {
			NamingEnumeration<SearchResult> e = session.search("", "xAppID=" + appID, new String[] { "xAttr:status" });
			if (e.hasMore()) {
				SearchResult res = e.next();
				Attribute attribute = res.getAttributes().get("xAttr");
				if (attribute != null) {
					NamingEnumeration<String> attrs = (NamingEnumeration<String>) attribute.getAll();
					while (attrs.hasMore()) {
						String[] pair = attrs.next().split(":");
						if ("status".equals(pair[0]) && "running".equals(pair[1])) {
							throw new IllegalStateException(
								"A aplicação que está sendo removida está em execução. Impossível remover.");
						}
					}
					attrs.close();
				}
				if (e.hasMore()) throw new IllegalStateException("mais de um xAppID encontrado " + appID);

				session.unbindDN(res.getName());
			}
			e.close();
		} catch (NamingException e) {
			log.error("erro na sessão do ldap", e);
		}
	}
}
