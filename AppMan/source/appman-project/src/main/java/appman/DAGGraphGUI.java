/*
 * appmanGUI.java
 *
 * Created on June 15, 2004, 5:41 PM
 */

package appman;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.ApplicationId;
import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;

import appman.clustering.ClusteringPhase;
import appman.parser.ApplicationDescription;
import appman.parser.SimpleParser;

/**
 *
 * @author  lucasa
 */
public class DAGGraphGUI extends javax.swing.JFrame
{
	private static final long serialVersionUID = -548735690290363468L;
	private static final Log log = LogFactory.getLog(DAGGraphGUI.class);

	ApplicationManagerRemote appman;
	final ApplicationId appId;
    
    /** Creates new form appmanGUI */
    public DAGGraphGUI()
    {
        appId = AppManUtil.getExecutor().currentApplication();
        initComponents();
        AppManUtil.registerWindow(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
            	exitForm(evt);
            	//AppManUtil.exitApplication();
            }
        });

        jPanel1.setLayout(new java.awt.GridLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(320,200));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(320, 200));
        jPanel1.add(jScrollPane1);
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("Menu");
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        jMenuItem1.setText("Carregar Arquivo DAG");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });

        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // Add your handling code here:
        AppManUtil.exitApplication();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
            // Add your handling code here:
        AppManUtil.runAssynchronousAction(appId, new Runnable(){
                public void run()
                    {
                        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser("../..");
                        chooser.addChoosableFileFilter(new DagFileFilter());
                        int option = chooser.showOpenDialog(DAGGraphGUI.this);
                        if (option == javax.swing.JFileChooser.APPROVE_OPTION)
                        {
                            try
                            {

                                java.io.File file = chooser.getSelectedFile();
                                if (file == null) return;
            
                                String[] args = new String[1];
                                args[0] = file.getAbsolutePath();
                                ApplicationDescription appdesc;
                                try
                                {
                                        //SimpleParser.ReInit((InputStream)null);
                                    appdesc = SimpleParser.parseGRIDADL(args);
                                } catch (Exception e)
                                {
                                    appdesc = null;
                                    AppManUtil.exitApplication("Error on parser: " + e, e);
                                }
			
                                Random rand  = new Random();
                                //VDN
                        		ClusteringPhase cp = new ClusteringPhase(appdesc.getDAG());
                        		Vector clusterP = cp.clustering();
                        		                                
                                int nclusters = cp.getNumberOfLevels(); // numero de clusters
                                String[] clusters = new String[nclusters];
                                for(int i=0; i<nclusters; i++)
                                {				
                                    clusters[i] = "cluster"+String.valueOf(i);
                                }
                                int n = 1;
                                for(int i=0; i < n; i++)
                                {
                                        //ApplicationManager defaultapp = createLocalApplicationManager();
                                        //defaultapp.startDefaultAppGUI();
                                    appman = createApplicationManager("teste("+i+")");
					
                                    //GraphGenerator.clusteringAlgorithm(clusters, appdesc); //VDN comentou
                                    GraphGenerator.clusteringPhaseAlgorithm(clusterP, clusters, appdesc); //VDN Inseriu
                                    String graph_name[] = new String[nclusters];
                                    for(int j=0;j<nclusters;j++)
                                    {
                                        graph_name[j] = "grafo"+String.valueOf(rand.nextInt());												
                                        appman.addApplicationDescriptionRemote(graph_name[j], clusters[j], appdesc);
                                        appman.startAppGUIRemote(graph_name[j]);					
                                    }
                                    appman.startApplicationManager();
                                    while(!ApplicationManagerState.FINAL.equals(appman.getApplicationState()))
                                    {
                                        Thread.sleep(5000);						
                                    }
                                }
                            }catch (IOException e1)
                            {
                                AppManUtil.exitApplication("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO: ", e1);
                            }
                            catch (Exception e2)
                            {
                                AppManUtil.exitApplication(null, e2);
                            }
                                //this.jPanel1.add(appman.startAppGUI("grafo1"));
                                //this.pack();
                        }
                    }
            }); // fim Action
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_jMenu1ActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        AppManUtil.exitApplication();
 
    }//GEN-LAST:event_exitForm
        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception
    {
    	log.debug("This program is going to test the AppMan package");
		DAGGraphGUI daggui = new DAGGraphGUI();
		daggui.pack();
		daggui.setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
 
    
    private static class DagFileFilter extends javax.swing.filechooser.FileFilter {
        
        @Override
		public boolean accept(java.io.File file) {
            if (file == null)
                return false;
            return file.isDirectory() || file.getName().toLowerCase().endsWith(".dag");
        }
        
        @Override
		public String getDescription() {
            return "Dag Script files (*.dag)";
        }
        
    }
    
    private ApplicationManagerRemote createApplicationManager(String appmanId)
    {
        try
        {
					
            GeneralObjectActivator activator = new GeneralObjectActivator("ApplicationManager",
                                                                          new Class[] {ApplicationManagerRemote.class},
                                                                          new String[] {"ApplicationManagerRemote"},
                                                                          true);
						
            ObjectId h = AppManUtil.getExecutor().createObject(ApplicationManager.class,
                                                               new Object[] {appmanId},
                                                               activator,
                                                               HostId.getLocalHost());
												 
                // if h is null, so get some error in the remote object
            if(h == null)
            {
                RemoteException e = new RemoteException("Host falhou");
                throw e;
            }
							
                //ApplicationManagerRemote stub = (ApplicationManagerRemote)h.getStub();
            ApplicationManagerRemote stub = (ApplicationManagerRemote)
                GeneralObjectActivator.getRemoteObjectReference(h, ApplicationManagerRemote.class, "ApplicationManagerRemote");
                            
                //stub.setStubRemote(stub);
            String contact = activator.getContactAddress(0);
// 							log.debug("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO!");
            stub.setMyObjectContactAddressRemote(contact);
            return stub ;
							
        }catch (Exception e)
        {
            AppManUtil.exitApplication("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO", e);
        }
		return null;
    }
	private ApplicationManager createLocalApplicationManager() throws RemoteException
        {
            return new ApplicationManager();
        }
       
}
