package appman.parser;
/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * ASTTask.java - edited from jjtree class: store task info before
 *   storing into ApplicationDescription.java
 * 2004/07/09
 */

import java.util.*;

/**
 * 
 *	edited from jjtree class: store task info before
 */

public class ASTTask extends SimpleNode {
  /*
   * Attributes included
   */
  private String taskName;
  private String executable;
  private Vector inputfile=null;
  private Vector outputfile=null;
  private int compCost=-1; // negative number means user didn't specified a value

  /*
   * Default Methods
   */
  public ASTTask(int id) {
    super(id);
  }

  public ASTTask(SimpleParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SimpleParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  /*
   * Methods included start here
   */

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public String getTaskName() {
    return this.taskName;
  }

  public void setExecutable(String executable) {
    this.executable = executable;
  }

  public String getExecutable() {
    return this.executable;
  }

  public void setInput(Vector inputfile) {
    this.inputfile =inputfile;
  }

  public Vector getInput() {
    return this.inputfile;
  }

  public void setOutput(Vector outputfile) {
    this.outputfile =outputfile;
  }

  public Vector getOutput() {
    return this.outputfile;
  }

  public void setCompCost(int compCost) {
    this.compCost = compCost;
  }

  public int getCompCost() {
    return this.compCost;
  }


  public void addTask(ApplicationDescription appDescription) {
    appDescription.addTask(taskName,executable,inputfile,outputfile,compCost);
  }
}
