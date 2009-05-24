/*
 * Created on 30/06/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package appman;

import java.io.FileWriter;
import java.util.Vector;

// import org.isam.exehda.Exehda;
import org.isam.exehda.ObjectId;
import org.isam.exehda.services.ObjectSeed.Activator;
import org.isam.exehda.services.ObjectSeed.MarshaledOX;
import org.isam.exehda.services.OXManager.OXHandle;

/**
 * @author lucasa
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class GeneralObjectActivator implements Activator {

	private static final long serialVersionUID = -5564402573494806186L;

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
			System.out.println("[VDN]Export Object:" + adress + "\n");
		}

		// update the ox meta-attribute 'contact'
		OXHandle oxh = AppManUtil.getOXManager().createHandle(oxID);
// 		Debug.debug("GeneralObjectActivator OXManager ObjectId: " + oxID + ": "
// 				+ obj, true);
		for (int i = 0; i < interfaceClass.length; i++) {
			System.out.println("VINDN: ADRESS" + contactAddress.elementAt(i)
					+ "interface class:" + interfaceClass[i]);
			oxh.setAttribute(ATT_WORB_CONTACT + interfaceClass[i],
					contactAddress.elementAt(i));
// 			Debug.debug(
// 					"GeneralObjectActivator getRemoteObjectReference setAttribute: "
// 							+ ATT_WORB_CONTACT
// 							+ interfaceClass[i]
// 							+ " - "
// 							+ oxh.getAttribute(ATT_WORB_CONTACT
// 									+ interfaceClass[i]), true);
		}
// 		Debug.debug("GeneralObjectActivator Object starting remote: " + obj
// 				+ "objectClass", true);
		//if is runnable, create a new thread and make it run
		if ((obj instanceof Runnable) && (run)) {
			thr = new Thread((Runnable) obj);
			thr.start();
		} else {
			thr = null;
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
		//
		//TODO: add implementation
		//					 
	}

	private transient Thread thr;

	public static Object getRemoteObjectReference(ObjectId oxhandle,
			Class chandle, String remote_interface) {
// 		Debug.debug("GeneralObjectActivator getRemoteObjectReference: "
// 				+ remote_interface, true);
		//obtain master contact from oxm
		OXHandle oxh = AppManUtil.getOXManager().createHandle(oxhandle);
// 		Debug.debug("GeneralObjectActivator OXManager ObjectId: " + oxhandle,
// 				true);
// 		Debug.debug(
// 				"GeneralObjectActivator getRemoteObjectReference getAttribute: "
// 						+ ATT_WORB_CONTACT + remote_interface, true);
		String contact = (String) oxh.getAttribute(ATT_WORB_CONTACT
				+ remote_interface);

		//VDN
		if (remote_interface.compareTo("ApplicationManagerRemote") == 0) {
			FileWriter file;

			String path = "appman_contact_adress.txt";

			try {
				file = new FileWriter(path);
				file.write(contact);
				file.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//VDN
		if (remote_interface.compareTo("SubmissionManagerRemote") == 0) {
			FileWriter file;
			String path = "hosts.txt";
			String str;
			try {
				str = contact.substring(contact.indexOf("hostid:") + 7, contact
						.indexOf('.'));
				file = new FileWriter(path, true);
				file.write(str + "\n");
				file.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}/*GridTaskID
		  * else{ FileWriter file; String path = "hosts.txt";
		  * 
		  * try{
		  * 
		  * file = new FileWriter(path, true);
		  * file.write(remote_interface+"\n"); file.close();
		  *  } catch(Exception e){ e.printStackTrace(); }
		  *  }
		  */

// 		Debug.debug("GeneralObjectActivator getRemoteObjectReference contact: "
// 				+ contact, true);
		//System.out.println("Contato ?????????????????????????? "+contact);

		//get master from worb
		Object object = AppManUtil.getWorb().lookupService(contact, chandle);
// 		Debug.debug(
// 				"GeneralObjectActivator getRemoteObjectReference lookupService REMOTE object: "
// 						+ object, true);
		return object;
	}

	public static Object getRemoteObjectReference(String contact, Class chandle) {
// 		Debug.debug("GeneralObjectActivator getRemoteObjectReference contact: "
// 				+ contact, true);
		Object object = AppManUtil.getWorb().lookupService(contact, chandle);
// 		Debug.debug(
// 				"GeneralObjectActivator getRemoteObjectReference lookupService REMOTE object: "
// 						+ object, true);
		return object;
	}

}

