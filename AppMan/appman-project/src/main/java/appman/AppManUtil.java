

package appman;

import java.awt.Window;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.isam.exehda.ApplicationId;
import org.isam.exehda.Exehda;
import org.isam.exehda.services.CellInformationBase;
import org.isam.exehda.services.Collector;
import org.isam.exehda.services.Executor;
import org.isam.exehda.services.OXManager;
import org.isam.exehda.services.Worb;

import appman.log.Debug;


public class AppManUtil
{
    private static Vector frames = new Vector();

    public static Executor getExecutor()
        {
            return (Executor) Exehda.getService(Executor.SERVICE_NAME);
        }

    public static OXManager getOXManager()
        {
            return (OXManager) Exehda.getService(OXManager.SERVICE_NAME);
        }

    public static Worb getWorb()
        {
            return (Worb) Exehda.getService(Worb.SERVICE_NAME);
        }

    
    /*
     * O collector é interface de acesso aos sensores disponibilizados na
     * plataforma. Para isso, ele agrega/genrencia um conjunto (que pode ser
     * extendido) de objetos Monitor. Cada objeto Monitor exporta 1 ou mais
     * sensores.
     * Para usar o Coletor é necessário registrar-se como cosumidor, obtendo
     * assim um ID. Esse ID é usado para suportar diferentes visões da
     * ativação/parametrização dos sensores para cada monitor. Ele pode ser
     * usado para consulta explicita (pooling) a um determinado sensor.
     * Opcionalmente, o consumidor pode tambem optar pelo modelo
     * publish-subscribe e se registrar para receber o valor de um determinado
     * sensor, sempre que este varie acima de um determinado limiar.
     * Originalmente, esse limiar seria configurável, mas atualmente, se não me
     * engano, esté hardcoded: qq variação do sensor é notificada.
     */
    public static Collector getCollector()
        {
            return (Collector) Exehda.getService(Collector.SERVICE_NAME);
        }

    public static CellInformationBase getCellInformationBase()
        {
            return (CellInformationBase) Exehda.getService(CellInformationBase.SERVICE_NAME);
        }

    public static void exitApplication()
        {
//             (new Throwable()).printStackTrace();
            
                // ensure all application frames are disposed 
            for (int i=0; i<frames.size(); i++) {
                Window w = (Window) frames.elementAt(i);

                try { w.dispose(); }
                catch (Exception e) { /* empty */ }
            }

            
            ((Executor) Exehda.getService(Executor.SERVICE_NAME))
                .exitApplication();
        }

    public static void exitApplication(String msg, Throwable t)
        {
            if ( msg != null ) {
                Debug.debug(msg, true);
            }
            
            if ( t != null ) {
                Debug.debug(t, true);
                t.printStackTrace();
            }

            exitApplication();
        }

    
        /**
         * Registers a window (tipically a Frame or JFrame) to be automatically disposed
         * up on application exit);
         *
         * @param w a <code>java.awt.Window</code> value
         */
    public static void registerWindow(java.awt.Window w)
        {
            if ( w != null && !frames.contains(w) ) {
                frames.add(w);
            }
        }

    public static void runAssynchronousAction(ApplicationId appId, Runnable action)
        {
            final ApplicationId aid = appId;
            final Runnable a = action;
            
            (new Thread() {
                @Override
				public void run() {
                    getExecutor().runAction(aid, a);
                }
                }).start();
        }
}
