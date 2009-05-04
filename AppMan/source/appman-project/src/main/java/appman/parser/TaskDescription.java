/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * ApplicationDescription.java - stores info obtained during the 
 *     application parsing
 *     - it stores info about a specific task which  will represent
 *     a node in the application DAG
 * 2004/04/30
 */

//package grand.parsing;
package appman.parser;

import java.io.Serializable;
import java.util.Vector;

public class TaskDescription implements Serializable {

	private static final long serialVersionUID = -929117541596781442L;

	private String taskName;
	private String executable;
	private Vector<String> inputFiles;
	private Vector<String> outputFiles;
	private int computationalCost;

	private String myClusterId = "";

	public TaskDescription(String taskName, String executable) {
		this.taskName = taskName;
		this.executable = executable;
		// System.out.println("[GRAND]\tNew " + toString());
	}

	public String getTaskName() {
		return this.taskName;
	}

	public void putInputFiles(Vector<String> inputFiles) {
		this.inputFiles = (Vector<String>) inputFiles.clone();
	}

	public Vector<String> getInputFiles() {
		return this.inputFiles;
	}

	public void putOutputFiles(Vector<String> outputFiles) {
		this.outputFiles = (Vector<String>) outputFiles.clone();
	}

	public Vector<String> getOutputFiles() {
		return this.outputFiles;
	}

	public void setClusterId(String id) {
		myClusterId = id;
	}

	public String getClusterId() {
		return myClusterId;
	}

	public boolean hasOutputFile(String outputfile) {
		if (outputFiles == null) return false;
		for (String o : outputFiles) {
			if (o.equals(outputfile)) {
				return true;
			}
		}
		return false;
	}

	// lucas 20/07/2004
	public boolean hasInputFile(String inputfile) {
		if (inputFiles == null) return false;

		for (String o : inputFiles) {
			if (o.equals(inputfile)) {
				return true;
			}
		}
		return false;
	}

	public void putComputationalCost(int computationalCost) {
		this.computationalCost = computationalCost;
	}

	public int getComputationalCost() {
		return this.computationalCost;
	}

	// lucas 30/07/2004
	public String getExcutable() {
		return executable;
	}

	/**
	 *pkvm 2005/05/16
	 */
	public void setExecutable(String executable) {
		this.executable = executable;
	}

	/**
	 *vdn 2005/03/18
	 */
	public String getExecutable() {
		return this.executable;
	}

	@Override
	public String toString() {
		return "TaskDescription: name[" + taskName + "], executable[" + executable + "]";
	}
}
