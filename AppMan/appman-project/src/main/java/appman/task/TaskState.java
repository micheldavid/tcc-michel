package appman.task;

public enum TaskState {
	TASK_DONT_KNOW(-1, -1), TASK_DEPENDENT(0, 3 /* red */), TASK_READY(1, 1 /* yellow */), TASK_EXECUTING(2, 0 /* green */), TASK_FINAL(
		3, 2 /*blue*/), TASK_FOREIGN(4, -1), TASK_FOREIGN_FINAL(5, -1);

	private int code;
	private int color;
	private TaskState(int code, int color) {
		this.code = code;
		this.color = color;
	}
	public int getCode() {
		return code;
	}

	public int getColor() {
		return color;
	}
}
