package appman.task;

public class TaskFinal extends TaskState {

	private static final long serialVersionUID = 1251628670436506694L;

	@Override
    public int getCode() {
		return TaskState.TASK_FINAL;
	}

	@Override
    public int getColor() {
		return 2; // blue
	}

	@Override
    public String getName() {
		return "TASK_FINAL";
	}

	
	
}
