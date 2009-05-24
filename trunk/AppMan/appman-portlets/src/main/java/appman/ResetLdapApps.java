package appman;

import org.apache.commons.configuration.Configuration;

import appman.portlets.AppManLdapHelper;
import appman.portlets.LdapSession;
import appman.portlets.ResourceUtil;

public class ResetLdapApps {

	public static void main(String[] args) throws Exception {
		Configuration envConfig = ResourceUtil.getEnvConfig();
		String server = envConfig.getString("exehda.ldap.server");
		String rootDN = envConfig.getString("exehda.ldap.rootDN");
		String user = envConfig.getString("exehda.ldap.user");
		String password = envConfig.getString("exehda.ldap.password");
		LdapSession session = new LdapSession(server, rootDN, user, password);
		session.open();

		for (String appId : AppManLdapHelper.searchApplications(session, null, null)) {
			AppManLdapHelper.removeAppID(session, appId);
		}
		session.close();
	}
}
