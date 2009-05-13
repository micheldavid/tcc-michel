package appman.parser;

public enum GraphType {
	INDEP(0), LOW(1), HIGH(2);

	private int code;

	private GraphType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static GraphType fromCode(int code) {
		switch (code) {
		case 0:
			return INDEP;
		default:
		case 1:
			return LOW;
		case 2:
			return HIGH;
		}
	}
}
