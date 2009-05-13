package appman.task;

public class TaskForeignFinal extends TaskState {

	private static final long serialVersionUID = -4463939750156020394L;

	@Override
	public int getCode() {
		return TaskState.TASK_FOREIGN_FINAL;
	}

	@Override
	public int getColor() {
		return -1;
	}

	@Override
	public String getName() {
		return "TASK_FOREIGN_FINAL";
	}

	
	
}
