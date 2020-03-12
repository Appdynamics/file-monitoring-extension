# AppDynamics File Watcher Extension

## Use Case
The AppDynamics File Watcher Extension can be used to provide metrics from configured files and directories. 

## Prerequisites
In order to use this extension, you need a [Standalone JAVA Machine Agent](https://docs.appdynamics.com/display/PRO44/Java+Agent) or a [SIM Agent](https://docs.appdynamics.com/display/PRO44/Server+Visibility).
For more details on downloading these products, please visit https://download.appdynamics.com/.

The extension can be deployed on the same box as the one with the files to be monitored, or remotely if monitoring shared network paths.

## Installation
1. To build from source, clone this repository and run 'mvn clean install'. This will produce a FileWatcher-VERSION.zip in the target directory
Alternatively, download the latest release archive from [GitHub](https://github.com/Appdynamics/file-monitoring-extension/releases)
2. Unzip the file FileWatcher-[version].zip into `<MACHINE_AGENT_HOME>/monitors/`
3. In the newly created directory "FileWatcher", edit the config.yml to configure the parameters (See Configuration section below)
4. Restart the Machine Agent
5. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance|\<Tier\>|Custom Metrics|File Watcher. If SIM is enabled, look for the 
metric browser under the Servers tab. 

## Configuration

Configure the File Watcher Extension by editing the ```config.yml``` & ```monitor.xml``` files in `<MACHINE_AGENT_HOME>/monitors/FileWatcher/`.

### 1. Tier Configuration

Configure the Tier under which the metrics should be reported. This can be done by adding the Tier ID to the metric prefix. 
```metricPrefix: "Server|Component:<TIER_ID>|Custom Metrics|File Watcher|"``` 

If SIM is enabled, please use the default metric prefix. 
```metricPrefix: "Custom Metrics|File Watcher|```

### 2. Path Configuration

The paths to be monitored must be configured under ```pathsToProcess```. The following fields are present in each:

####2.1 displayName
A mandatory field that represents the current path in the ```metricPrefix```

#### 2.2 path
The actual path to the directories or files to be monitored. Consider our directory to be ```/src/test/resources/TestFiles```. 
There are multiple scenarios that can be configured in the ```pathsToProcess``` section. The use cases supported here as as follows: 

##### 2.2.1 Monitoring a specific directory
```path: "src/test/resources/TestFiles"```
Directory metrics for 'TestFiles' will be generated. Please ensure that the directory does not end with a slash for this scenario. 

##### 2.2.2 Monitoring a specific file
```path: "src/test/resources/TestFiles/TF1.txt"```
File metrics for TF1.txt will be generated in this case. Refer to the Metrics section to differentiate between Directory metrics & File metrics. 

##### 2.2.3. Monitoring files of a specific type
```path: "src/test/resources/TestFiles/*.txt"```
This will generate file metrics for only ```txt``` files withing TestFiles.

##### 2.2.4. Monitoring files of any type
```path: "src/test/resources/TestFiles/*.*"```
This will generate file metrics for files of all extensions within TestFiles.

##### 2.2.5. Directory and File wildcards
```path: "src/test/resources/TestFiles/2020*/*.log"```
This will generate file metrics for all log files within subdirectories of TestFiles that begin with '2020'. 

##### 2.2.6 Non-recursive, single level
```path: "src/test/resources/TestFiles/*"```
This will generate file and directory metrics for all files and subdirectories within TestFiles only at the first level. 

##### 2.2.7 Fully recursive
```path: "src/test/resources/TestFiles/**"```
This will recursively generate file and directory metrics for all files and subdirectories within TestFiles at all levels. 



### 3. Metric Character Replacers

This section can be used to replace any characters in a match with the specified characters. They come into effect only a match is found for the 
original pattern. We have pre-configured three metric character replacers for characters considered invalid by the AppDynamics Metric Browser. 

```
#Replaces characters in metric name with the specified characters.
# "replace" takes any regular expression
# "replaceWith" takes the string to replace the matched characters
metricCharacterReplacer:
    - replace: ":"
      replaceWith: ";"
    - replace: "\\|"
      replaceWith: "#"
    - replace: "\\,"
      replaceWith: "#"
```

### 4. Number of Threads 
The extension uses one thread per configured log, and one thread per log file within. Let's consider our initial example: 

```
logs:
     - displayName: "Test Log"
       logDirectory: "/Users/XYZ/MyApplication/logs"
       logName: "myLog.log*"
       searchStrings:
           #displayName Should be unique across the various patterns.
          - displayName: "Errors"
            pattern: "ERROR"
            matchExactString: true
            caseSensitive: true
            printMatchedString: true
```

Assuming that your logger settings make ```myLog.log``` rollover to a max of five files (myLog.log.1 to myLog.log.5), the number of threads needed in this case would be 7. 
(One for the log directory, and six for the files within). 

This can be configured using the ```numberOfThreads``` field in the config.yml. 


### 5. Configuring the monitor.xml

Configure the path to the config.yml by editing the ```<task-arguments>``` in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/LogMonitor/` directory: 

```
<task-arguments>
     <!-- config file-->
     <argument name="config-file" is-required="true" default-value="monitors/LogMonitor/config.yml" />
      ....
</task-arguments>
```

Restart the machine agent once this is done. 

### 6. Events Service

Before proceeding with this step, please ensure that all the prerequisites and steps to install the AppDynamics Events Service have been met. Refer to the sub-pages under 
https://docs.appdynamics.com/display/PRO45/Events+Service+Deployment for detailed information. 

The extension can now publish log matches to the Events Service. Use the following section in the config.yml for this feature: 

```
sendDataToEventsService: true

logMatchOffset: 5

# This field contains the various parameters required to initiate a connection and send data to the AppDynamics Events Service.
eventsServiceParameters:
  host: 
  port: 
  globalAccountName:
  eventsApiKey: 
  useSSL: 
```

The logMatchOffset section appends the specified number of lines with the line containing the actual log match and all 
of this makes the body of an event. This can be particularly useful while trying to search for exceptions and also retrieving the stack trace that follows. 

Note that enabling this feature will not impact the regular delivery of metrics to the metric browser. 


## Metrics

The extension publishes the following metrics: 

**1. File size in bytes** 

For the next two metrics, let's use the following configuration as an example: 

myLog.log 
(This is a static log. It does not rollover and no further content is added to this file)
```
[Thread-1] 29 Apr 2014 12:31:18,647  INFO DynamicServiceManager - Scheduling DynamicServiceManager at interval of 30 seconds
[Thread-1] 29 Apr 2014 12:31:18,647  INFO LifeCycleManager - Started service [DynamicServiceManager]
```

config.yml 
```
metricPrefix: "Server|Component:<TIER_ID>|Custom Metrics|Log Monitor"

logs:
     - displayName: "Test Log"
       logDirectory: "/Users/XYZ/MyApplication/logs"
       logName: "myLog.log"
       searchStrings:
           #displayName Should be unique across the various patterns.
          - displayName: "Info Statements"
            pattern: "INFO"
            matchExactString: true
            caseSensitive: true
            printMatchedString: true
```

**2. Occurrences of each configured pattern**
When the extension starts, a base occurrence metric for the pattern ```INFO``` is initialized with a value of 0. This value will represent the unique occurrences of ```INFO``` 
observed every minute. It will reset to 0 if no occurrences of ```INFO``` are found in any given minute. This metric is always reported, regardless of the state of 
the ```printMatchedString``` flag and can be used to set up alerts and health rules. 

In this case, the metric will be reported as: 

```Application Infrastructure Performance|<TIER>|Custom Metrics|Log Monitor|Test Log|Info Statements|Occurrences``` with a value of 2 for when the log is read for the first time. The metric then resets to 
0 in the next minute, until the log is repopulated with more ```INFO``` statements. 


**3. Occurrences of matched strings**
This metric is reported only when the ```printMatchedString``` flag is set to true for a searchString. In this case, the following metrics will be reported when the log is read for the first time: 

```Application Infrastructure Performance|<TIER>|Custom Metrics|Log Monitor|Test Log|Info Statements|Occurrences``` with a value of 2 
```Application Infrastructure Performance|<TIER>|Custom Metrics|Log Monitor|Test Log|Info Statements|Matches|INFO``` with a value of 2

Both metrics reset to a value of 0 in the next minute, until the log is repopulated with more ```INFO``` statements. 

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following [document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130) for how to use the Extensions WorkBench

## Troubleshooting
Please follow the steps listed in the [extensions troubleshooting document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might face during the installation of the extension. If these don't solve your issue, please follow the last step on the troubleshooting-document to contact the support team.

## Support Tickets
If after going through the Troubleshooting Document, you haven't been able to get your extension working, please file a ticket with the following information.

1. Stop the running machine agent .
2. Delete all existing logs under <MachineAgent>/logs .
3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug. 

```
   <logger name="com.singularity">
   <logger name="com.appdynamics">
```

4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
5. Attach the zipped <MachineAgent>/conf/* directory.
6. Attach the zipped <MachineAgent>/monitors/LogMonitor directory.

For any support related questions, you can also contact help@appdynamics.com.

## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/log-monitoring-extension).

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |4.0.0       |
|Controller Compatibility  |4.0 or Later|
|Last Update               |12/11/2019 |
|List of Changes           |[Change log](https://github.com/Appdynamics/log-monitoring-extension/blob/master/CHANGELOG.md) |
