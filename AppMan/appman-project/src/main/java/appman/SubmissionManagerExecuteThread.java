package appman;

/**
 * Executa SubmissionManagerRemote.runSubmissionManager e chama handler quando finalizar.
 */
public class SubmissionManagerExecuteThread extends Thread {

	private String submissionManagerId;
	private SubmissionManagerRemote submissionManagerRemote;
	private SubmissionManagerExecuteHandler handler;

	public SubmissionManagerExecuteThread(String submissionManagerId, SubmissionManagerRemote submissionManagerRemote,
		SubmissionManagerExecuteHandler handler) {
		setName("smExecutor:" + submissionManagerId);
		this.submissionManagerId = submissionManagerId;
		this.submissionManagerRemote = submissionManagerRemote;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			submissionManagerRemote.runSubmissionManager();
			handler.submissionManagerFinished(this, null);
		} catch (Exception e) {
			handler.submissionManagerFinished(this, e);
		}
	}

	public String getSubmissionManagerId() {
		return submissionManagerId;
	}

	public void setSubmissionManagerId(String submissionManagerId) {
		this.submissionManagerId = submissionManagerId;
	}

	public SubmissionManagerRemote getSubmissionManagerRemote() {
		return submissionManagerRemote;
	}

	public void setSubmissionManagerRemote(SubmissionManagerRemote submissionManagerRemote) {
		this.submissionManagerRemote = submissionManagerRemote;
	}
}
