package appman.task;

public class TaskExecuting extends TaskState {

	@Override
	public int getCode() {
		return TaskState.TASK_EXECUTING;
	}

	@Override
	public int getColor() {
		return 0; // green
	}

	@Override
	public String getName() {
		return "TASK_EXECUTING";
	}

	
	
}
