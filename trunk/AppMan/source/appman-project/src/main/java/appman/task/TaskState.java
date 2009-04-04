package appman.task;

import java.io.Serializable;

public abstract class TaskState implements Serializable {

	public abstract int getCode();

	public abstract String getName();

	public abstract int getColor();
	
	public static final int TASK_DONT_KNOW = -1;
	public static final int TASK_DEPENDENT = 0;
	public static final int TASK_READY = 1;
	public static final int TASK_EXECUTING = 2;
	public static final int TASK_FINAL = 3;
	public static final int TASK_FOREIGN = 4;
	public static final int TASK_FOREIGN_FINAL = 5;

	public static TaskState getInstance(int code) {
		switch (code) {
		case TASK_DONT_KNOW:
			return new TaskUnknown();
		case TASK_DEPENDENT:
			return new TaskDependent();
		case TASK_READY:
			return new TaskReady();
		case TASK_EXECUTING:
			return new TaskExecuting();
		case TASK_FINAL:
			return new TaskFinal();
		case TASK_FOREIGN:
			return new TaskForeign();
		case TASK_FOREIGN_FINAL:
			return new TaskForeignFinal();			
			
		default:
			throw new IllegalArgumentException("código inválido");
		}
	}
	
}
