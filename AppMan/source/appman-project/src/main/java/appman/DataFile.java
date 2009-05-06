/**
 * Criado em Ter Mai 4 2004 at 15:32:53.
 */
package appman;

import java.io.Serializable;

import appman.task.Task;

/**
 * @author lucasa
 */
public class DataFile implements Serializable {
	/* {src_lang=Java} */

	private static final long serialVersionUID = 2586879788018644561L;

	/** which task owns this datafile */
	private Task fromtask;

	private String name;

	private String datafileId;

	private boolean exist;

	public DataFile(String str, String id, Task task) {
		name = str;
		datafileId = id;
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

    @Override
	public int hashCode() {
        return name.hashCode();
    }

    @Override
	public boolean equals(Object o) {
        if ( o != null && o.getClass().equals(getClass())) {
            DataFile other = (DataFile) o;

                //
                // FIX ME: need to validate this checks, are them enough?
                //
            return name.equals(other.name)
//                && fromtask.equals(other.fromtask)
                && datafileId.equals(other.datafileId);
        }
        return false;
    }
}
