package appman;

public class SubmissionManagerInfo {

	private String id;
	private SubmissionManagerRemote remote;
	
	public SubmissionManagerInfo(String id, SubmissionManagerRemote remote) {
		this.id = id;
		this.remote = remote;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SubmissionManagerRemote getRemote() {
		return remote;
	}

	public void setRemote(SubmissionManagerRemote remote) {
		this.remote = remote;
	}

	@Override
	public String toString() {
		return "SMInfo[id:" + id + ", remote:" + remote + "]";
	}
}
