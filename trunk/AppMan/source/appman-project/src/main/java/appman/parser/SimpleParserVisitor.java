/* Generated By:JJTree: Do not edit this line. SimpleParserVisitor.java */

package appman.parser;

public interface SimpleParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTRoot node, Object data);
  public Object visit(ASTTask node, Object data);
  public Object visit(ASTForeachNode node, Object data);
  public Object visit(ASTAssignment node, Object data);
}
