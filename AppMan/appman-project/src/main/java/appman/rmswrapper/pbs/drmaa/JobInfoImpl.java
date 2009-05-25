package appman.rmswrapper.pbs.drmaa;

import org.ggf.drmaa.JobInfo;

public class JobInfoImpl extends JobInfo {

	protected JobInfoImpl(String jobName, int exitStatus) {
		super(jobName, exitStatus, null);
	}

	private static final long serialVersionUID = 1870231605912138603L;

	public int getExitStatus() {
		return 0;
	}

	public String getTerminatingSignal() {
		return null;
	}

	public boolean hasCoreDump() {
		return false;
	}

	public boolean hasExited() {
		return false;
	}

	public boolean hasSignaled() {
		return false;
	}

	public boolean wasAborted() {
		return false;
	}

}