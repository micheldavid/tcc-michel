package appman.task;

public class TaskForeign extends TaskState {

	private static final long serialVersionUID = 9147073567604205908L;

	@Override
	public int getCode() {
		return TaskState.TASK_FOREIGN;
	}

	@Override
	public int getColor() {
		return -1;
	}

	@Override
	public String getName() {
		return "TASK_FOREIGN";
	}

	
	
}
