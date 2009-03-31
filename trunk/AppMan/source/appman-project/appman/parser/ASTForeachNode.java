/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * ASTForeachNode.java - edited from jjtree class: allows "running" foreach
 * 2004/07/27
 */

package appman.parser;
 
import java.util.*;
/**
 * edited from jjtree class: allows "running" foreach
 */
public class ASTForeachNode extends SimpleNode {
    /* included by kayser in 2004/07/27*/
  private int numberOfIterations=1;
  private String variableName;
  private Vector rangeElements;
    /**/

  public ASTForeachNode(int id) {
    super(id);
  }

  public ASTForeachNode(SimpleParser p, int id) {
    super(p, id);
  }



    /* included by kayser in 2004/07/27*/
  public void  setVariableName (String varname) {
      this.variableName = varname;
  }

  public String  getVariableName () {
      return variableName;
  }

  public void  setNumberOfIterations (int n) {
      this.numberOfIterations = n;
  }

  public int  getNumberOfIterations () {
      return numberOfIterations;
  }

  public void  setRangeElements (Vector rangeElements) {
      this.rangeElements = rangeElements;
  }

  public Vector  getRangeElements () {
      return rangeElements;
  }

  public Object  getRangeElementAt (int i) {
      return rangeElements.elementAt(i);
  }


    /**/


  /** Accept the visitor. **/
  public Object jjtAccept(SimpleParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}

