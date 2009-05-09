/*
 * Created on 30/06/2005
 */
package exehda;

import java.rmi.RemoteException;
import java.util.Vector;

import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;
import org.isam.exehda.services.OXManager.OXHandle;
import org.isam.exehda.services.ObjectSeed.Activator;
import org.isam.exehda.services.ObjectSeed.MarshaledOX;

/**
 * @author lucasa
 */
public class GeneralObjectActivator implements Activator {

	private static final long serialVersionUID = 4265596170317207245L;

	public static final String ATT_WORB_CONTACT = "object.contact:";

	private String objectClass;

	private String[] interfaceClass;

	private Class<?>[] oclass;

	private boolean run;

	private Vector<String> contactAddress = new Vector<String>();

	public static <REMOTE> REMOTE createRemoteObj(Class<?> objClass, Class<REMOTE> remoteInterface)
		throws RemoteException {

		GeneralObjectActivator activator = new GeneralObjectActivator(objClass.getSimpleName(),
			new Class[] { remoteInterface }, new String[] { remoteInterface.getSimpleName() }, true);

		ObjectId h = ExehdaUtil.getExecutor().createObject(objClass, new Object[0], activator, HostId.getLocalHost());

		return GeneralObjectActivator.getRemoteObjectReference(h, remoteInterface,
			remoteInterface.getSimpleName());
	}

	
	public GeneralObjectActivator(String sobjectClass, Class<?>[] cinterfaceClass, String[] sinterfaceClass,
		boolean execute) {
		this.objectClass = sobjectClass;
		this.oclass = cinterfaceClass;
		this.interfaceClass = sinterfaceClass;
		this.run = execute;
	}

	public String getContactAddress(int i) {
		return (String) contactAddress.elementAt(i);
	}

	public String getContactAddress(ObjectId handle, String remote_interface) {
		OXHandle oxh = ExehdaUtil.getOXManager().createHandle(handle);
		return (String) oxh.getAttribute(ATT_WORB_CONTACT + remote_interface);
	}

	public void activate(ObjectId oxID, Object obj, MarshaledOX extState) throws Exception {
		// export object to WORB
		for (int i = 0; i < oclass.length; i++) {
			String adress = ExehdaUtil.getWorb().exportService(obj, oclass[i], objectClass + oxID.toString() + i);
			contactAddress.addElement(adress);
		}

		// update the ox meta-attribute 'contact'
		OXHandle oxh = ExehdaUtil.getOXManager().createHandle(oxID);
		for (int i = 0; i < interfaceClass.length; i++) {
			oxh.setAttribute(ATT_WORB_CONTACT + interfaceClass[i], contactAddress.elementAt(i));
		}

		if ((obj instanceof Runnable) && (run)) {
			new Thread((Runnable) obj).start();
		}
	}

	public void deactivate(ObjectId oxID, Object obj, MarshaledOX oxState) throws Exception {}

	public static <C> C getRemoteObjectReference(ObjectId oxhandle, Class<C> chandle, String remote_interface) {

		// obtain master contact from oxm
		OXHandle oxh = ExehdaUtil.getOXManager().createHandle(oxhandle);
		String contact = (String) oxh.getAttribute(ATT_WORB_CONTACT + remote_interface);

		// get master from worb
		return getRemoteObjectReference(contact, chandle);
	}

	public static <C> C getRemoteObjectReference(String contact, Class<C> chandle) {
		return (C) ExehdaUtil.getWorb().lookupService(contact, chandle);
	}
}
