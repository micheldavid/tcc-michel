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
import java.util.*;

  /**
   *
   */
  public class  TaskDescription implements Serializable 
  {
    private String taskName;
    private String executable;
    private Vector inputFiles;
    private Vector outputFiles;
    private int computationalCost;
    
    private String myClusterId = "";
    
    public TaskDescription (String taskName, String executable) {
       this.taskName = taskName;
       this.executable = executable;
       ///System.out.println("[GRAND]\tNew TaskDescription: name["+taskName+"], executable["+executable+"]");
    }
    /**
     *
     */
    public String getTaskName(){
       return this.taskName;
    }
 
    
    /**
     *
     */
    public void putInputFiles(Vector inputFiles){
       this.inputFiles = (Vector) inputFiles.clone();
    }
    
    public Vector getInputFiles(){
       return this.inputFiles;
    }

    /**
     *
     */
    public void putOutputFiles(Vector outputFiles){
       this.outputFiles = (Vector) outputFiles.clone();
    }
    
    public Vector getOutputFiles(){
       return this.outputFiles;
    }
    
   public void setClusterId(String id)
   {
   		myClusterId = new String(id);
   }
   
   public String getClusterId()
  {
	   return myClusterId;
  }
    
    public boolean hasOutputFile(String outputfile) {
       Enumeration e = outputFiles.elements();
       while (e.hasMoreElements()) {
          String o = (String)e.nextElement(); 
          if (o.equals(outputfile)) {
             return true;
          }
       }
       return false;
    }
    
    // lucas 20/07/2004
	public boolean hasInputFile(String inputfile) {
		   Enumeration e = inputFiles.elements();
		   while (e.hasMoreElements()) {
			  String o = (String)e.nextElement(); 
			  if (o.equals(inputfile)) {
				 return true;
			  }
		   }
		   return false;
		}

    /**
     *
     */
    public void putComputationalCost(int computationalCost){
       this.computationalCost = computationalCost;
    }

    public int getComputationalCost(){
       return this.computationalCost;
    }
    
    //lucas 30/07/2004
    public String getExcutable()
    {
    	return executable;
    }
    
    /**
     *pkvm 2005/05/16
     */
    public void setExecutable(String executable){
       this.executable = executable;
    }
    /**
     *vdn 2005/03/18
     */
    public String getExecutable(){
       return this.executable;
    }    
  }
