/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * SymbolTable.java - symbol table used by the parser for 
 *    input description file
 * 2004/04/28
 * 2004/07/05 - mySymbolTable was a String matrix and now is Object (due to list threatment)
 * 2004/08/02 - mySymbolTable is still a Object matrix, however, the second element (variable's
 *              value can deal with assignments with variables 
 */

package appman.parser;


  public class  SymbolTable {

    // first row has string values (variable name) and second row has String or Vector
    private Object[][] mySymbolTable; 
    private int maxNumberOfVar;
    private int numberOfVar;

    public SymbolTable (int max_number_of_variables) {
      mySymbolTable = new Object[max_number_of_variables][2];
      maxNumberOfVar = max_number_of_variables;
      numberOfVar = 0;
      // putVariable("dummy", "0");
    }

    public void putVariable(String varName, Object varValue) {
       String realVarName = getVarNameWithoutDelimiters(varName);
       int index = getVarIndex(realVarName);
       mySymbolTable[index][1] = varValue;

       ///log.debug("[GRAND]\t(putVariable) SymbolTable at "+index+
       ///                   ":  "+mySymbolTable[index][0]+
       ///                   " = "+mySymbolTable[index][1]);
    }

    public Object getVariable(String varName) {
       String realVarName = getVarNameWithoutDelimiters(varName);

       int index = getVarIndex(realVarName);
       Object varValue = mySymbolTable[index][1];

       ///log.debug("[GRAND]\t(getVariable) SymbolTable at "+index+
       ///                   ":  "+mySymbolTable[index][0]+
       ///                   " = "+mySymbolTable[index][1]);
       return varValue;
    }

    public Object getVariableValue(String varName) {
       String realVarName = getVarNameWithoutDelimiters(varName);

       int index = checkVarIndex(realVarName);
       if (index < 0) {
	   return null;
       } else {
           Object varValue = mySymbolTable[index][1];


           ///log.debug("[GRAND]\t(getVariable) SymbolTable at "+index+
           ///                   ":  "+mySymbolTable[index][0]+
          ///                   " = "+mySymbolTable[index][1]);
          return varValue;
       }
    }

      private String getVarNameWithoutDelimiters(String varName) {
	  if (varName.charAt(0) != '$') {
	      // nothing to do
	      return varName;
	  } else{
              String value = varName.substring(2,varName.length() -1);
	      // log.debug ("\t\tsubstring: "+value);
	      return value;
	  }
      }

    private int getVarIndex(String varName){
      int index = 0;

      while ((index < numberOfVar) && 
             (! (varName.equals(mySymbolTable[index][0])))){
      index++;
      }
      
      if (index == numberOfVar) {
      numberOfVar++;
          mySymbolTable[index][0]=varName;
      }
      return index;
    }

    private int checkVarIndex(String varName){
      int index = 0;

      while ((index < numberOfVar) && 
             (! (varName.equals(mySymbolTable[index][0])))){
      index++;
      }
      
      if (index == numberOfVar) {
	  return -1;
      }
      return index;
    }

 }
