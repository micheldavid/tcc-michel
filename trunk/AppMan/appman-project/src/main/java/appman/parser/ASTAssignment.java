/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * ASTAssignment.java 
 * 2004/08/04
 */

package appman.parser;

/** 
 * edited from jjtree class: store assignment info 
 */

public class ASTAssignment extends SimpleNode {
   /*
   * Attributes included
   */
 private String variableName;
 private AssignmentBody assignmentBody;
 

 public ASTAssignment(int id) {
    super(id);
  }

  public ASTAssignment(SimpleParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  @Override
public Object jjtAccept(SimpleParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  /*
   * Methods included 
   */
  public void setAssignmentBody (AssignmentBody value) {
    this.assignmentBody = value;
  }

  public AssignmentBody getAssignmentBody () {
    return this.assignmentBody;
  }

  public void setVariableName(String variableName) {
     this.variableName = variableName;
  }

  public String getVariableName() {
     return this.variableName;
  }

}
