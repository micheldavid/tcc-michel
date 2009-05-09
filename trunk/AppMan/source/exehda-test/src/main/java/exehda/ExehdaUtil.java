package exehda;

import org.isam.exehda.Exehda;
import org.isam.exehda.services.CellInformationBase;
import org.isam.exehda.services.Collector;
import org.isam.exehda.services.Executor;
import org.isam.exehda.services.OXManager;
import org.isam.exehda.services.Worb;

public class ExehdaUtil {

	public static Executor getExecutor() {
		return (Executor) Exehda.getService(Executor.SERVICE_NAME);
	}

	public static OXManager getOXManager() {
		return (OXManager) Exehda.getService(OXManager.SERVICE_NAME);
	}

	public static Worb getWorb() {
		return (Worb) Exehda.getService(Worb.SERVICE_NAME);
	}

	/*
	 * O collector é interface de acesso aos sensores disponibilizados na plataforma. Para isso, ele agrega/gerencia um
	 * conjunto (que pode ser extendido) de objetos Monitor. Cada objeto Monitor exporta 1 ou mais sensores. Para usar o
	 * Collector é necessário registrar-se como cosumidor, obtendo assim um ID. Esse ID é usado para suportar diferentes
	 * visões da ativação/parametrização dos sensores para cada monitor. Ele pode ser usado para consulta explicita
	 * (pooling) a um determinado sensor. Opcionalmente, o consumidor pode tambem optar pelo modelo publish-subscribe e
	 * se registrar para receber o valor de um determinado sensor, sempre que este varie acima de um determinado limiar.
	 * Originalmente, esse limiar seria configurável, mas atualmente, se não me engano, esté hardcoded: qq variação do
	 * sensor é notificada.
	 */
	public static Collector getCollector() {
		return (Collector) Exehda.getService(Collector.SERVICE_NAME);
	}

	public static CellInformationBase getCellInformationBase() {
		return (CellInformationBase) Exehda.getService(CellInformationBase.SERVICE_NAME);
	}

}
