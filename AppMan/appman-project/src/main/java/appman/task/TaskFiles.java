package appman.task;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.DataFile;

public class TaskFiles implements java.io.Serializable {

	private static final Log log = LogFactory.getLog(TaskFiles.class);
	private static final long serialVersionUID = -1155977077422538783L;
	private java.util.Vector inputFiles;
	private java.util.Vector outputFiles;

	public TaskFiles(Vector input, Vector output) {
		if (input == null && output == null) {
			inputFiles = new Vector();
			outputFiles = new Vector();
		} else {
			inputFiles = new Vector(input);
			outputFiles = new Vector(output);
		}
	}

	public synchronized void addDataFileToInputList(DataFile data) {
		inputFiles.addElement(data);
	}

	public synchronized void addDataFileToInputList(Vector data) {
		inputFiles.addAll(data);
	}

	public synchronized void addDataFileToOutputList(DataFile data) {
		outputFiles.addElement(data);
	}

	public synchronized void addDataFileToOutputList(Vector data) {
		outputFiles.addAll(data);
	}

	/**
	 * @return DataFile
	 */
	public synchronized DataFile[] getInputFiles() {
		return (DataFile[]) inputFiles.toArray(new DataFile[0]);
	}

	/**
	 * @return
	 */
	public synchronized DataFile[] getOutputFiles() {
		return (DataFile[]) outputFiles.toArray(new DataFile[0]);
	}

	public synchronized void updateOutputFilesState() {
		// atualiza o status dos arquivos de saída
		for (int i = 0; i < outputFiles.size(); i++) {
			log.debug("Task FINAL set output file exists: "
					+ ((DataFile) outputFiles.elementAt(i)).getName());
			((DataFile) outputFiles.elementAt(i)).setDataFileExist(true);
		}
	}
	
	public synchronized void setAllOutputFileAsNotExist() {
		// atualiza o status dos arquivos de saída
		for (int i = 0; i < outputFiles.size(); i++) {
			((DataFile) outputFiles.elementAt(i)).setDataFileExist(false);
		}
	}
	
}
