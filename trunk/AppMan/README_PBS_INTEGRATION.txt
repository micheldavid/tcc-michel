This file list requirements for using PBS to handle AppMan tasks and tries to explain how this integration works.

* Requirements
PBS Client (qsub and qstat) must be available in the same machine where a SubmissionManager is started.


* Changes in AppMan
For a SubmissionManager to submit tasks to a PBS system the configuration file gridnodes.properties must be changed.
AppMan is now able to deal with a new property called grid.targetHosts.submissionmanagers.hosts[<HOST_NAME>].concreteTaskClassName.

For instance, consider the following gridnodes.properties file content:
	grid.targetHosts.submissionmanagers.hosts = 1.ktx, 1.gradep
	grid.targetHosts.host-final-results = 1.ktx
	grid.targetHosts.localscheduler = exehda
	
	# host configuration
	grid.targetHosts.submissionmanagers.hosts[1.ktx].concreteTaskClassName=GridTaskDrmaa
	grid.targetHosts.submissionmanagers.hosts[1.gradep].concreteTaskClassName=GridTask
	
The above example sets the SubmissionManager created in host 1.ktx to use class GridTaskDrmaa, responsible for talking to PBS, and host 1.gradep to use GridTask.
This way is possible to mix grid nodes using EXEHDA and using PBS for task execution.

Up until now there was only class GridTask to represent a task, now there is also GridTaskDrmaa.
GridTask still exists and it works just as it did before. It is the default choice for creating a task, in case when no value is set for this property.

This is the only required change in AppMan to use a PBS system for task execution. 

* How it works
Basically, what runJob does is the following:
	1 Create a script (called script.pbs) using task attributes (command, input files, etc.) inside the task directory in the file system
	2 Submit this script to PBS, calling qsub with Runtime.getRuntime().exec(qsubCommand)
	3 Checking if task execution has finished, calling qstat with Runtime.getRuntime().exec(qstatCommand) periodically until execution end is detected

* Code organization
All functionality required to use a PBS system is isolated from AppMan code by class appman.rmswrapper.pbs.drmaa.SessionImpl.
GridTaskDrmaa only calls methods from this class to submit the task.