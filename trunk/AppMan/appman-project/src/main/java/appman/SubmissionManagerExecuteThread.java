package appman;

/**
 * Executa SubmissionManagerRemote.runSubmissionManager e chama handler quando finalizar.
 */
public class SubmissionManagerExecuteThread extends Thread {

	private SubmissionManagerInfo submissionManagerInfo;
	private SubmissionManagerExecuteHandler handler;

	public SubmissionManagerExecuteThread(SubmissionManagerInfo submissionManagerInfo,
		SubmissionManagerExecuteHandler handler) {
		setName("smExecutor:" + submissionManagerInfo.getId());
		this.submissionManagerInfo = submissionManagerInfo;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			submissionManagerInfo.getRemote().runSubmissionManager();
			handler.submissionManagerFinished(this, null);
		} catch (Exception e) {
			handler.submissionManagerFinished(this, e);
		}
	}

	public SubmissionManagerInfo getSubmissionManagerInfo() {
		return submissionManagerInfo;
	}

	public void setSubmissionManagerInfo(SubmissionManagerInfo submissionManagerInfo) {
		this.submissionManagerInfo = submissionManagerInfo;
	}
}
