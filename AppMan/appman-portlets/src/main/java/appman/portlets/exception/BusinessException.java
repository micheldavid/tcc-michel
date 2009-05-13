package appman.portlets.exception;

public class BusinessException extends Exception {

	private static final long serialVersionUID = -2509811988195786096L;

	/**
	 * Código do erro, se houver algum
	 */
	private String code;

	public BusinessException() {
		super();
	}

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, String code) {
		super(message);
		this.setCode(code);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(String message, String code, Throwable cause) {
		super(message, cause);
		this.setCode(code);
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
