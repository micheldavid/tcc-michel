package appman.task;

public class TaskDependent extends TaskState {

	@Override
	public int getCode() {
		return TaskState.TASK_DEPENDENT;
	}

	@Override
	public int getColor() {
		return 3; // red
	}

	@Override
	public String getName() {
		return "TASK_DEPENDENT";
	}

	
	
}
