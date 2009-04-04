package appman.task;

public class TaskUnknown extends TaskState {

	@Override
	public int getCode() {
		return TaskState.TASK_DONT_KNOW;
	}

	@Override
	public int getColor() {
		return -1;
	}

	@Override
	public String getName() {
		return "TASK_DONT_KNOW";
	}

	
	
}
