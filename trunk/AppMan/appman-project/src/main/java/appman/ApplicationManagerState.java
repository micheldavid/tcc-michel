package appman;

public enum ApplicationManagerState {
	FINAL(2),
	READY(0),
	EXECUTING(1);
	
	private final int code;
	private ApplicationManagerState(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
