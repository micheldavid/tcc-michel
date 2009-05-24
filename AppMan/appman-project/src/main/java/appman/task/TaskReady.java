package appman.task;

public class TaskReady extends TaskState {

	@Override
	public int getCode() {
		return TaskState.TASK_READY;
	}

	@Override
	public int getColor() {
		return 1; // yellow
	}

	@Override
	public String getName() {
		return "TASK_READY";
	}

	
	
}
