/*
 * Created on 30/06/2005
 */
package appman;

import java.io.FileWriter;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.ObjectId;
import org.isam.exehda.services.OXManager.OXHandle;
import org.isam.exehda.services.ObjectSeed.Activator;
import org.isam.exehda.services.ObjectSeed.MarshaledOX;

/**
 * @author lucasa
 */

public class GeneralObjectActivator implements Activator {

	private static final long serialVersionUID = -5564402573494806186L;
	private static final Log log = LogFactory.getLog(GeneralObjectActivator.class);

	public static final String ATT_WORB_CONTACT = "object.contact:";

	private String objectClass;

	private String[] interfaceClass;

	private Class[] oclass;

	private boolean run;

	private Vector contactAddress = new Vector();

	/** Creates a new instance of WorkerActivator */
	public GeneralObjectActivator(String sobjectClass, Class[] cinterfaceClass,
			String[] sinterfaceClass, boolean execute) {
		this.objectClass = sobjectClass;
		this.oclass = cinterfaceClass;
		this.interfaceClass = sinterfaceClass;
		this.run = execute;
	}

	public String getContactAddress(int i) {
		return (String) contactAddress.elementAt(i);
	}

	public String getContactAddress(ObjectId handle, String remote_interface) {
		OXHandle oxh = AppManUtil.getOXManager().createHandle(handle);
		String contact = (String) oxh.getAttribute(ATT_WORB_CONTACT
				+ remote_interface);
		return contact;
	}

	public void activate(ObjectId oxID, Object obj, MarshaledOX extState)
			throws Exception {
		String adress;
		// export object to WORB
		for (int i = 0; i < oclass.length; i++) {
			adress = AppManUtil.getWorb().exportService(obj, oclass[i],
					objectClass + oxID.toString() + i);
			contactAddress.addElement(adress);
			log.debug("[VDN]Export Object:" + adress);
		}

		// update the ox meta-attribute 'contact'
		OXHandle oxh = AppManUtil.getOXManager().createHandle(oxID);
// 		log.debug("GeneralObjectActivator OXManager ObjectId: " + oxID + ": " + obj);
		for (int i = 0; i < interfaceClass.length; i++) {
			log.debug("VINDN: ADRESS" + contactAddress.elementAt(i)
					+ "interface class:" + interfaceClass[i]);
			oxh.setAttribute(ATT_WORB_CONTACT + interfaceClass[i],
					contactAddress.elementAt(i));
// 			log.debug(
// 					"GeneralObjectActivator getRemoteObjectReference setAttribute: "
// 							+ ATT_WORB_CONTACT
// 							+ interfaceClass[i]
// 							+ " - "
// 							+ oxh.getAttribute(ATT_WORB_CONTACT
// 									+ interfaceClass[i]));
		}
// 		log.debug("GeneralObjectActivator Object starting remote: " + obj
// 				+ "objectClass");
		//if is runnable, create a new thread and make it run
		if ((obj instanceof Runnable) && (run)) {
			new Thread((Runnable) obj).start();
		}
	}

	public void deactivate(ObjectId oxID, Object obj, MarshaledOX oxState)
			throws Exception {
		// unexport object
		/*
		 * // update the ox meta-attribute 'stub' OXManager oxm = (OXManager)
		 * Exehda.getService(OXManager.SERVICE_NAME); OXHandle oxh =
		 * oxm.createHandle(oxID); oxh.setAttribute(ATT_WORB_CONTACT, null);
		 * 
		 * //if runnable, interrupt the thread if(thr != null){ thr.interrupt();
		 * thr.join(); thr = null; }
		 */
		// TODO implementar desativação do serviço no Exehda primeiro
	}

	public static Object getRemoteObjectReference(ObjectId oxhandle,
			Class chandle, String remote_interface) {

		//obtain master contact from oxm
		OXHandle oxh = AppManUtil.getOXManager().createHandle(oxhandle);
		String contact = (String) oxh.getAttribute(ATT_WORB_CONTACT
				+ remote_interface);

		// gravando os contatos caso alguém mais queira interagir com estes serviços
		try {
			if (ApplicationManagerRemote.class.isAssignableFrom(chandle)) {
				FileWriter file = new FileWriter("appman_contact_adress.txt");
				file.write(contact);
				file.close();

			} else if (SubmissionManagerRemote.class.isAssignableFrom(chandle)) {
				FileWriter file = new FileWriter("hosts.txt", true);
				file.write(contact.substring(contact.indexOf("hostid:") + 7, contact.indexOf('.')) + "\n");
				file.close();
			}
		} catch (Exception e) {
			log.error("escrevendo contatos", e);
		}

		//get master from worb
		return getRemoteObjectReference(contact, chandle);
	}

	public static Object getRemoteObjectReference(String contact, Class chandle) {
		return AppManUtil.getWorb().lookupService(contact, chandle);
	}

}

