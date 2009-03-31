package appman;

import java.io.Serializable;

/*
 * DataFile.java - Copyright lucasa
 * 
 * Here you can write a license for your code, some comments or any other
 * information you want to have in your generated code. To to this simply
 * configure the "headings" directory in uml to point to a directory where you
 * have your heading files.
 * 
 * or you can just replace the contents of this file with your own. If you want
 * to do this, this file is located at
 * 
 * /usr/share/apps/umbrello/headings/heading.java
 * 
 * -->Code Generators searches for heading files based on the file extension
 * i.e. it will look for a file name ending in ".h" to include in C++ header
 * files, and for a file name ending in ".java" to include in all generated java
 * code. If you name the file "heading. <extension>", Code Generator will always
 * choose this file even if there are other files with the same extension in the
 * directory. If you name the file something else, it must be the only one with
 * that extension in the directory to guarantee that Code Generator will choose
 * it.
 * 
 * you can use variables in your heading files which are replaced at generation
 * time. possible variables are : author, date, time, filename and filepath.
 * just write %variable_name%
 * 
 * This file was generated on Ter Mai 4 2004 at 15:32:53 The original location
 * of this file is
 */
/**
 * 
 * @author lucasa
 *
 */
public class DataFile implements Serializable {
	/* {src_lang=Java} */

	/** which task owns this datafile */
	private Task fromtask;

	private String name;

	private String datafileId;

	private boolean exist;

	public DataFile(String str, String id, Task task) {
		name = new String(str);
		datafileId = new String(id);
		exist = false;
		fromtask = task;
	}

	public String getName() {
		return name;
	}

	public String getDataFileId() {
		return datafileId;
	}

	public Task getFromTask() {
		return fromtask;
	}

	public void setDataFileExist(boolean t) {
		exist = t;
	}

	public boolean dataFileExist() {
		return exist;
	}

}