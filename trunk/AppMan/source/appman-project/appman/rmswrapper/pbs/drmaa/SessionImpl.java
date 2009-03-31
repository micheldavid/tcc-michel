package appman.rmswrapper.pbs.drmaa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.Version;

public class SessionImpl implements Session {

	class InputStreamHandler extends Thread {

		private boolean done;

		private StringBuffer m_captureBuffer;

		private InputStream m_stream;

		InputStreamHandler(StringBuffer captureBuffer, InputStream stream) {
			m_stream = stream;
			m_captureBuffer = captureBuffer;
			done = false;
			start();
		}

		public boolean isDone() {
			return done;
		}

		public void run() {
			try {
				int nextChar;
				while ((nextChar = m_stream.read()) != -1) {
					m_captureBuffer.append((char) nextChar);
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Error capturing process output", e);
			}
			done = true;
		}
	}

	class QStatThread extends Thread {

		public void run() {

			while (true) {
				try {
					if (submittedJobs.size() > 0) {
						String qStatCommand = buildQStatCommand(submittedJobs);
						logger.log(Level.INFO, "qstat command to be executed: '" + qStatCommand + "'");

						Process process = Runtime.getRuntime().exec(qStatCommand);
						InputStream inStream = process.getInputStream();
						StringBuffer inBuffer = new StringBuffer();
						InputStreamHandler inStreamHandler = new InputStreamHandler(inBuffer, inStream);

						InputStream errStream = process.getErrorStream();
						StringBuffer errBuffer = new StringBuffer();
						InputStreamHandler errStreamHandler = new InputStreamHandler(errBuffer, errStream);

						int exitValue = process.waitFor();
						while (!(inStreamHandler.isDone() && errStreamHandler.isDone())) {
							Thread.sleep(500);
						}

						logger.log(Level.INFO, "qstat process return value: " + exitValue);
						logger.log(Level.INFO, "qstat err stream: " + errBuffer);
						logger.log(Level.INFO, "qstat in stream: " + inBuffer);

						qStatOutput = inBuffer.toString();
					} else {
						qStatOutput = null;
					}
					Thread.sleep(5000);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error getting qstat output", e);
				}
			}
		}
	}

	class ResourceDescriptor {

		private String executablesPath;

		private long memoryPerNode;

		private int numberOfNodes;

		private int numberOfProcessors;

		private String serverName;

		protected ResourceDescriptor(String serverName) {
			this.serverName = serverName;
			this.executablesPath = "/usr/local/bin/";
		}

		public String getExecutablesPath() {
			return executablesPath;
		}

		public long getMemoryPerNode() {
			return memoryPerNode;
		}

		public int getNumberOfNodes() {
			return numberOfNodes;
		}

		public int getNumberOfProcessors() {
			return numberOfProcessors;
		}

		public String getServerName() {
			return serverName;
		}

		public void setExecutablesPath(String executablesPath) {
			this.executablesPath = executablesPath;
		}

		public void setMemoryPerNode(long memoryPerNode) {
			this.memoryPerNode = memoryPerNode;
		}

		public void setNumberOfNodes(int numberOfNodes) {
			this.numberOfNodes = numberOfNodes;
		}

		public void setNumberOfProcessors(int numberOfProcessors) {
			this.numberOfProcessors = numberOfProcessors;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

	}

	private static Logger logger = Logger.getLogger("appman.rmswrapper.pbs.cl.PBSWrapper");

	private String qStatOutput;

	private ResourceDescriptor resourceDescriptor;

	private List submittedJobs;

	SessionImpl() {
		this.resourceDescriptor = new ResourceDescriptor(null);
		this.submittedJobs = new Vector();
		new QStatThread().start();
	}

	private String buildQStatCommand(List jobTemplates) {
		StringBuffer sb = new StringBuffer();
		sb.append(resourceDescriptor.getExecutablesPath() + "qstat ");
		for (Iterator iter = jobTemplates.iterator(); iter.hasNext();) {
			JobTemplate jobTemplate = (JobTemplate) iter.next();
			sb.append(jobTemplate.getJobName());
			sb.append(" ");
		}
		return sb.toString();
	}

	private String buildQSubCommand(String scriptPath) {
		return resourceDescriptor.getExecutablesPath() + "qsub " + scriptPath;
	}

	public void control(String jobName, int action) throws DrmaaException {
	}

	public JobTemplate createJobTemplate() throws DrmaaException {
		return new JobTemplateImpl();
	}

	private File createScript(JobTemplateImpl jobTemplate, String serverName) {
		File scriptFile = null;
		try {
			scriptFile = new File(jobTemplate.getWorkingDirectory() + "/script.pbs");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(scriptFile)));

			// write PBS prologue to file first
			pw.println("#!/bin/sh");

			// -e path
			// Defines the path to be used for the standard error stream of the batch
			// job. The
			// path argument is of the form:
			// [hostname:]path_name
			pw.println("#PBS -e " + jobTemplate.getWorkingDirectory() + "/err");

			// -I
			// Declares that the job is to be run "interactively". The job will be
			// queued and
			// scheduled as any PBS batch job, but when executed, the standard input,
			// output,
			// and error streams of the job are connected through qsub to the terminal
			// session in which qsub is running.
			// pw.println("#PBS -I");

			// -j join
			// Declares if the standard error stream of the job will be merged with
			// the standard
			// output stream of the job.
			// pw.println("#PBS -j " + ???);

			// -k keep
			// Defines which (if either) of standard output or standard error will be
			// retained
			// on the execution host. If set for a stream, this option overrides the
			// path name
			// for that stream. If not set, neither stream is retained on the
			// execution host.
			// pw.println("#PBS -k " + ???);

			// -N name
			// Declares a name for the job. The name specified may be up to and
			// including 15
			// characters in length. It must consist of printable, non white space
			// characters
			// with the first character alphabetic.
			pw.println("#PBS -N appman[" + ((jobTemplate.getAppManJobName().length() <= 7) ? jobTemplate.getAppManJobName() : jobTemplate.getAppManJobName().substring(0, 7)) + "]");

			// -o path
			// Defines the path to be used for the standard output stream of the batch
			// job.
			// The path argument is of the form:
			// [hostname:]path_name
			pw.println("#PBS -o " + jobTemplate.getWorkingDirectory() + "/out");

			// -q destination
			// Defines the destination of the job. The destination names a queue, a
			// server, or
			// a queue at a server.
			if (serverName != null) {
				pw.println("#PBS -q @" + serverName);
			}

			// -S path_list
			// Declares the shell that interprets the job script.
			// pw.println("#PBS -S " + ???);

			pw.println("");
			pw.println("cd $PBS_O_WORKDIR");
			pw.println("mkdir -p " + jobTemplate.getWorkingDirectory());
			pw.println("cd " + jobTemplate.getWorkingDirectory());
			pw.println(jobTemplate.getRemoteCommand());

			pw.close();

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error creating script file", e);
		}
		return scriptFile;
	}

	public void deleteJobTemplate(JobTemplate jobTemplate) throws DrmaaException {
	}

	public void exit() throws DrmaaException {
	}

	public String getContact() {
		return null;
	}

	public String getDrmaaImplementation() {
		return null;
	}

	public String getDrmSystem() {
		return null;
	}

	public int getJobProgramStatus(String jobName) throws DrmaaException {
		return 0;
	}

	public ResourceDescriptor getResourceDescriptor() {
		return resourceDescriptor;
	}

	public Version getVersion() {
		return null;
	}

	private boolean hasTaskProducedErr(JobTemplate jobTemplate) {
		File f = new File(jobTemplate.getWorkingDirectory() + "/err");
		return f.exists() && f.length() > 0;
	}

	private boolean hasTaskProducedOut(JobTemplate jobTemplate) {
		File f = new File(jobTemplate.getWorkingDirectory() + "/out");
		return f.exists();
	}

	public void init(String contact) throws DrmaaException {
	}

	private boolean isTaskOnQStat(JobTemplate jobTemplate) {
		return qStatOutput != null && qStatOutput.indexOf(jobTemplate.getJobName().substring(0, jobTemplate.getJobName().indexOf(".") + 1)) >= 0;
	}

	public List runBulkJobs(JobTemplate jt, int start, int end, int incr) throws DrmaaException {
		return null;
	}

	public String runJob(JobTemplate jobTemplate) throws DrmaaException {
		File scriptFile = null;
		int exitValue = -1;
		try {
			scriptFile = createScript((JobTemplateImpl) jobTemplate, null);
			String qSubCommand = buildQSubCommand(scriptFile.getAbsolutePath());
			logger.log(Level.INFO, "qsub command to be executed: '" + qSubCommand + "'");
			Process process = Runtime.getRuntime().exec(qSubCommand);

			InputStream inStream = process.getInputStream();
			StringBuffer inBuffer = new StringBuffer();
			InputStreamHandler inStreamHandler = new InputStreamHandler(inBuffer, inStream);

			InputStream errStream = process.getErrorStream();
			StringBuffer errBuffer = new StringBuffer();
			InputStreamHandler errStreamHandler = new InputStreamHandler(errBuffer, errStream);

			exitValue = process.waitFor();
			while (!(inStreamHandler.isDone() && errStreamHandler.isDone())) {
				Thread.sleep(500);
			}
			logger.log(Level.INFO, "qsub process return value: " + exitValue);
			logger.log(Level.INFO, "qsub err stream: " + errBuffer);
			logger.log(Level.INFO, "qsub in stream: " + inBuffer);

			if (exitValue == 0) {
				jobTemplate.setJobName(inBuffer.toString().trim());
				this.submittedJobs.add(jobTemplate);
			}

		} catch (IOException e1) {
			logger.log(Level.SEVERE, "I/O error running task " + jobTemplate.getJobName() + ".", e1);
		} catch (InterruptedException e2) {
			logger.log(Level.SEVERE, "Thread error running task " + jobTemplate.getJobName() + ".", e2);
		} finally {
			if (scriptFile != null) {
				if (false && scriptFile.delete()) {
					logger.log(Level.INFO, "File " + scriptFile.getAbsolutePath() + " was successfully deleted.");
				} else {
					logger.log(Level.INFO, "File " + scriptFile.getAbsolutePath() + " was not deleted.");
				}
			}
		}
		return jobTemplate.getJobName();
	}

	public void synchronize(List jobIds, long timeout, boolean dispose) throws DrmaaException {

	}

	public JobInfo wait(String jobName, long timeout) throws DrmaaException {
		JobTemplate jobTemplate = getJobTemplate(jobName);
		int exitValue = Integer.MAX_VALUE;
		while (exitValue == Integer.MAX_VALUE) {
			if (isTaskOnQStat(jobTemplate)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "Error waiting for task completion", e);
				}
			} else if (hasTaskProducedErr(jobTemplate)) {
				exitValue = -1;
			} else if (hasTaskProducedOut(jobTemplate)) {
				exitValue = 0;
			}
		}
		this.submittedJobs.remove(jobTemplate);
		return new JobInfoImpl(jobTemplate.getJobName(), exitValue);
	}

	private JobTemplate getJobTemplate(String jobName) {
		JobTemplate jobTemplate = null;
		for (Iterator iter = submittedJobs.iterator(); iter.hasNext() && jobTemplate == null;) {
			JobTemplate jt = (JobTemplate) iter.next();
			if (jt.getJobName().equals(jobName)) {
				jobTemplate = jt;
			}
		}
		return jobTemplate;
	}
}
