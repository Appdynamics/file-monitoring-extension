FILE WATCHER EXTENSION
======================

Use case
--------

An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics from file and directory monitoring.

The extension can be configured with various comibinations of patterns to include/exclude files and directories. 

Prerequisites
-------------
 
This extension requires a AppDynamics Java Machine Agent installed and running. 

Rebuilding the Project 
----------------------

1. Clone the repo AppDynamics-File-Watcher-Extension from GitHub https://github.com/Appdynamics
3. Run 'mvn clean install' from the cloned AppDynamics-File-Watcher-Extension directory.
4. The AppDynamics-File-Watcher-Extension-<version>.zip should get built and found in the 'target' directory.

Installation
------------

1. Unzip contents of AppDynamics-File-Watcher-Extension-<version>.zip file and copy to <code><machine-agent-dir>/monitors</code> directory.

2. Edit the config.yaml file.  An example config.yaml file follows these installation instructions.

3. Restart the Machine Agent.

Metrics Provided
----------------

We provide following metric related to the state of a directory/file :

- IsModified : 1 if file is modified in last minute and 0 if not.
- Size : A positive integer indicating the current size of the file.
- FileCount: Count of files in a directory.
- OldestFileAge: Time in seconds from current time to the oldest modified file of the directory

Sample config.yaml
------------------
 
The following is a sample config.yaml file that depicts three different file paths defined. The different fields are explained in the in-line comments.

Note :
Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a yaml validator [here](http://yamllint.com/)

1. Configure the file to be watched by editing the config.yaml file in `<MACHINE_AGENT_HOME>/monitors/AppDynamics-File-Watcher-Extension/`
Below is the format :

```

    #For most purposes, no need to change this.
    numberOfThreads: 5

    #This will create this metric in all the tiers, under this path. Please make sure to have a trailing |
    #metricPrefix: Custom Metrics|FileWatcher|

    #This will create it in specific Tier. Replace <TIER_ID>. Please make sure to have a trailing |
    metricPrefix: "Server|Component:<TIER_ID>|Custom Metrics|FileWatcher|"

    fileToProcess:
       -# List of file Paths
		# Wildcards supported for field 'path' : "**" and "*".
		# The following use cases are covered :

		    # 1. Match only files and no directories. Eg. : /foo/bar/A*.java | It will match all java files starting with capital A letter inside bar directory of root directory foo.

		    # 2. All files and all directories. Eg : /foo/bar/** | It will match all files and directories recursively(hunt down sub-directories) present inside bar directory of foo directory and add them all to monitoring.

		    # 3. All files and directories non recusrsively. Eg : /foo/bar/* | It will match all files and directories present inside bar. It will not drill down another level from there.

		    # 4. Only directories, no files inside them. Use isDirectoryDetailsRequired flag and set it to 'false'. This will make the monitor skip files for this path. Eg : /foo/bar/* , /foo/bar/**

			# The above convention is very similar to ant-style conventions for defining directory tasks. For more info : http://ant.apache.org/manual/dirtasks.html#patterns

		  - displayName: "test1"
		    path: "/Users/deepak.kalra/Documents/appdynamics-docs/**"
		    ignoreHiddenFiles: false
		    isDirectoryDetailsRequired: true

		    #ignoreHiddenFiles : When true, this will cause all hidden files under this file path to be ignored from metric reporting
     		#isDirectoryDetailsRequired : When false, this will cause only directory details to be considered and files to be ignored from metric reporting
            #Important - displayName has to be unique for the file/folder

		  - displayName: "test2"
		    path: "/Users/deepak.kalra/Documents/test/**"
		    ignoreHiddenFiles: false
		    isDirectoryDetailsRequired: true    

		  - displayName: "test3"
		    path: "/Users/deepak.kalra/Desktop/https___www.myutiitsl.pdf"
		    ignoreHiddenFiles: false
		    isDirectoryDetailsRequired: true   
		    
		    #For windows based searching regexes the paths should be like : 
		    
		  - displayName: "test4"
		    path: "F:\\\\Users\\\\deepak.kalra\\\\Documents\\\\test\\\\**"
		    ignoreHiddenFiles: false
		    isDirectoryDetailsRequired: true   
		    

    # This is the global isDirectoryDetailsRequired flag. When an individual fileToProcess path has this flag missing, this global one will be used.    
	# If false then all the files under the directory will be ignored and only the directory level metrics will be published
	# for the directories specified above.
	# For files given above it will have no effect
	isDirectoryDetailsRequired: false

	# If true then an extra metric will be shown that will give age of the oldest file (in seconds when the file was last
	# modified and compared to current time) in the directory(ies) specified above.
	# For files given above it will not display this metric
	isOldestFileAgeMetricRequired: true

	# If true then an extra metric will be shown that will give number of files in the directories specified above.
	# For files given above it will not display this metric
	isFileCountRequired: true

	# This is the global ignoreHiddenFiles flag. When an individual fileToProcess path has this flag missing, this global one will be used.   
	# If true then all the hidden files will be ignored monitoring.
	ignoreHiddenFiles: true

```

2. Configure the path to the config.yaml file by editing the <task-arguments> in the monitor.xml file. Below is the sample

     <task-arguments>
         <!-- config file-->
             <argument name="config-file" is-required="true" default-value="monitors/AppDynamics-File-Watcher-Extension/config.yml" />
          ....
     </task-arguments>


FILEWATCHER METRIC BROWSER
--------------------------

![alt tag](/screenshots/FW_IsModified.png)
File Watcher Snapshot For File Modified
 
![alt tag](/screenshots/FW_Directory_Size_Change.png)
File Watcher Snapshot For Directory Size Change
 
![alt tag](/screenshots/FW_Count_Directory_Files.png)
File Watcher Snapshot For Directory File Count Change
 
![alt tag](/screenshots/FW_Oldest_File_Age.png)
File Watcher Snapshot For Directory Oldest File Age

Troubleshooting
---------------

1. Verify Machine Agent Data: Please start the Machine Agent without the extension and make sure that it reports data. Verify that the machine agent status is UP and it is reporting Hardware Metrics.
2. config.yml: Validate the file [here](http://www.yamllint.com/)
3. Collect Debug Logs: Edit the file, <MachineAgent>/conf/logging/log4j.xml and update the level of the appender com.appdynamics to debug Let it run for 5-10 minutes and attach the logs to a support ticket

Contributing
-------------

Always feel free to fork and contribute any changes directly via [GitHub][].

Community
----------

Find out more in the [Community][].

Support
--------

For any questions or feature request, please contact [AppDynamics Center of Excellence][].
**Version:** 1.3 : Added support for wildcards
**Version:** 1.2
**Controller Compatibility:** 3.7 or later

[GitHub]: https://github.com/Appdynamics/AppDynamics-File-Watcher-Extension
[Community]: http://community.appdynamics.com/
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com
