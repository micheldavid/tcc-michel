package appman.portlets;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

public class LdapSession {

	private String serverURL;
	private String user;
	private String passwd;
	private DirContext dirCtx;

	public LdapSession(String sAddr, String rootDn, String user, String passwd) {
		serverURL = "ldap://" + sAddr + "/" + rootDn;
		this.user = user;
		this.passwd = passwd;
		dirCtx = null;
	}

	public void open() throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.provider.url", serverURL);
		env.put("java.naming.security.principal", user);
		env.put("java.naming.security.credentials", passwd);
		dirCtx = new InitialDirContext(env);
	}

	public void close() throws NamingException {
		dirCtx.close();
		dirCtx = null;
	}

	public void createNode(String dn, String ans[], String avs[]) throws NameNotFoundException,
		NameAlreadyBoundException, NamingException {
		if (ans.length != avs.length) {
			throw new IllegalArgumentException("Lengths of attribute names and values arrays do not match.");
		}
		Attributes initAtts = new BasicAttributes(true);
		for (int i = 0; i < ans.length; i++) {
			initAtts.put(ans[i], avs[i]);
		}

		dirCtx.createSubcontext(dn, initAtts);
	}

	public String getNodeAttribute(String dn, String attr) throws NamingException {
		return getNodeAttributes(dn, new String[] { attr })[0];
	}

	public String[] getMultiValuedNodeAttribute(String dn, String attr) throws NamingException {
		Attribute result = dirCtx.getAttributes(dn, new String[] { attr }).get(attr);
		if (result == null) {
			return new String[0];
		}
		String tmp[] = new String[result.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = (String) result.get(i);
		}

		return tmp;
	}

	public String[] getNodeAttributes(String dn, String atts[]) throws NamingException {
		Attributes result = dirCtx.getAttributes(dn, atts);
		String tmp[] = new String[atts.length];
		for (int i = 0; i < atts.length; i++) {
			tmp[i] = (String) result.get(atts[i]).get();
		}

		return tmp;
	}

	public void setNodeAttribute(String dn, String an, String av) throws NamingException {
		setNodeAttributes(dn, new String[] { an }, new String[] { av });
	}

	public void setMultiValuedNodeAttribute(String dn, String an, String avs[]) throws NamingException {
		Attribute newAtt = new BasicAttribute(an);
		for (int i = 0; i < avs.length; i++) {
			newAtt.add(avs[i]);
		}

		Attributes atts = new BasicAttributes(true);
		atts.put(newAtt);
		dirCtx.modifyAttributes(dn, 2, atts);
	}

	public void setNodeAttributes(String dn, String ans[], String avs[]) throws NamingException {
		if (ans.length != avs.length) {
			throw new IllegalArgumentException("Size of attribute names and values do not match");
		}
		Attributes atts = new BasicAttributes(true);
		for (int i = 0; i < ans.length; i++) {
			String an = ans[i];
			String av = avs[i];
			Attribute newAtt = new BasicAttribute(an);
			newAtt.add(av);
			if ("xAttr".equals(an)) {
				Attribute oldAtt = dirCtx.getAttributes(dn).get(an);
				if (oldAtt != null) {
					for (Enumeration<?> ave = oldAtt.getAll(); ave.hasMoreElements(); newAtt.add((String) ave
						.nextElement()));
				}
			}
			atts.put(newAtt);
		}

		dirCtx.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE, atts);
	}

	public String[] listNodeAttributes(String dn) throws NamingException {
		Attributes atts = dirCtx.getAttributes(dn);
		String results[] = new String[atts.size()];
		NamingEnumeration<?> all = atts.getAll();
		for (int i = 0; all.hasMore(); i++) {
			results[i] = (String) ((Attribute) all.next()).get();
		}

		return results;
	}

	public Object getObject(String dn) throws NamingException {
		return dirCtx.lookup(dn);
	}

	public void setObject(String dn, Object o) throws NamingException {
		dirCtx.bind(dn, o);
	}

	public NamingEnumeration<?> search(String subtree, String filter, String attributesToReturn[]) throws NamingException {
		if (subtree == null) {
			subtree = "";
		}
		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setReturningAttributes(attributesToReturn);
		return dirCtx.search(subtree, filter, ctls);
	}
	
	public void unbindDN(String dn) throws NamingException {
		dirCtx.unbind(dn);
	}

	@Override
	public String toString() {
		return serverURL + " / " + user + " / " + passwd.replaceAll(".", "*") + ": " + dirCtx;
	}
}
