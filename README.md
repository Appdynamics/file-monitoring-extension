# AppDynamics File Watcher Extension

## Use Case
The AppDynamics File Watcher Extension can be used to provide metrics from configured files and directories. 

## Prerequisites
1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.

2. Download and install [Apache Maven](https://maven.apache.org/) which is configured with `Java 8` to build the extension artifact from source. You can check the java version used in maven using command `mvn -v` or `mvn --version`. If your maven is using some other java version then please download java 8 for your platform and set JAVA_HOME parameter before starting maven.

3. The extension can be deployed on the same box as the one with the files to be monitored, or remotely if monitoring shared network paths. For Windows network paths, it is recommended to map the paths locally prior to monitoring.

## Installation
1. Clone the "file-monitoring-extension" repo using `git clone <repoUrl>` command.
2. Run 'mvn clean install' from "file-monitoring-extension". This will produce a FileWatcher-VERSION.zip in the target directory
3. Unzip the file FileWatcher-[version].zip into `<MACHINE_AGENT_HOME>/monitors/`
4. In the newly created directory "FileWatcher", edit the config.yml to configure the parameters (See Configuration section below)
5. Restart the Machine Agent
6. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance|\<Tier\>|Custom Metrics|File Watcher. If SIM is enabled, look for the 
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

### 2.1 displayName
A mandatory field that acts as an alias for a configured path in the Metric Browser. 

#### 2.2 path
The actual path to the directories or files to be monitored. Consider our directory to be ```/src/test/resources/TestFiles```. 
There are multiple scenarios that can be configured in the ```pathsToProcess``` section. The use cases supported are as follows: 

(Note: For Windows, path should be configured with 4 backslashes as separator - ```C:\\\\src\\\\test\\\\resources\\\\TestFiles```.
For Windows Network paths, you can configure path like - ```\\\\\\\\1.2.3.4\\\\abc\\\\def\\\\ProductI```)

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

##### 2.2.5. Directory and File Glob Patterns
```path: "src/test/resources/TestFiles/2020*/*.log"```
This will generate file metrics for all log files within subdirectories of TestFiles that begin with '2020'. 

##### 2.2.6 Non-recursive, single level
```path: "src/test/resources/TestFiles/*"```
This will generate file and directory metrics for all files and subdirectories within TestFiles only at the first level. 

##### 2.2.7 Fully Recursive
```path: "src/test/resources/TestFiles/**"```
This will recursively generate file and directory metrics for all files and subdirectories within TestFiles at all levels. 

##### 2.2.8 Fully Recursive + File Extensions
```path: "src/test/resources/TestFiles/**/*.*"```
This will recursively generate file metrics for all files that match the glob pattern within TestFiles at all levels. 


#### 2.3 ignoreHiddenFiles
A flag to include or exclude any hidden files or directories encountered. 

#### 2.4 excludeSubdirectoriesFromFileCount
Every directory has a metric that counts the number of files within that directory. When this flag is set to true, the 
extension will simply exclude any subdirectories from this count. 

#### 2.5 recursiveFileCounts
The count mentioned in 2.4 only includes the files within a directory at the immediate next level. When this flag is set 
to true, the extension will publish a new metric that recursively counts the number of files within the configured directory 
and within all subdirectories. This can be used in conjunction with 2.3 & 2.4.  

#### 2.6 recursiveFileSizes
The File Size metric for each directory only shows the size of the directory's contents (in bytes) at the first level. 
When this flag is set to true, the extension publishes a new metric that shows the size of the directory on the disk. Please note that 
this metric is only available for directories. 


## Metrics
The extension provides the following metrics: 

### 1. File Size (Bytes)
Available for both, files and directories. 

### 2. Oldest File Age
Available only for directories.

### 3. File Count
Available only for directories. 

### 4. Number of Lines
Available only for files. 

### 5. Last Modified Time
Available for both, files and directories. 

### 6. Available
Available for both, files and directories. Will have a value of 0 when a previously 'available' file gets deleted.

### 7. Recursive File Count
Available only for directories. Refer to 2.5 for detailed information. 

### 8. Size on Disk (Bytes)
Available only for directories. Refer to 2.6 for detailed information. 

### 9. Modified
Available for both, files and directories. Will have a value of 1 if a file/directory being monitored was modified in the last 
60 seconds. 


### Number of Threads 
Always include one thread per base directory + 1. 


### Configuring the monitor.xml

Configure the path to the config.yml by editing the ```<task-arguments>``` in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/FileWatcher/` directory: 

```
<task-arguments>
     <!-- config file-->
     <argument name="config-file" is-required="true" default-value="monitors/FileWatcher/config.yml" />
      ....
</task-arguments>
```

Restart the machine agent once this is done. Note that this is a continuous extension, as it actively watches each configured path for changes. 

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following [document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130) for how to use the Extensions WorkBench

## Troubleshooting
Please follow the steps listed in the [extensions troubleshooting document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might face during the installation of the extension.

## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/file-monitoring-extension).

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |3.1.4       |
|Last Update               |22/06/2021 |
|List of Changes           |[Change log](https://github.com/Appdynamics/file-monitoring-extension/blob/master/CHANGELOG.md) |

**Note**: While extensions are maintained and supported by customers under the open-source licensing model, they interact with agents and Controllers that are subject to [AppDynamics’ maintenance and support policy](https://docs.appdynamics.com/latest/en/product-and-release-announcements/maintenance-support-for-software-versions). Some extensions have been tested with AppDynamics 4.5.13+ artifacts, but you are strongly recommended against using versions that are no longer supported.
