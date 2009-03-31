package appman.rmswrapper.pbs.drmaa;

import org.ggf.drmaa.JobTemplate;

public class JobTemplateImpl extends JobTemplate {

	private String appManJobName;

	JobTemplateImpl() {

	}

	public String getAppManJobName() {
		return appManJobName;
	}

	public void setAppManJobName(String appmanJobName) {
		this.appManJobName = appmanJobName;
	}
}
