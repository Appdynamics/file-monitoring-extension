/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.filewatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class FileWatcherMonitor extends AManagedMonitor{

	public static final Logger logger = LoggerFactory.getLogger(FileWatcherMonitor.class);
	private Configuration configuration;
	private String metricPrefix;
	private boolean isFileCountRequired;
	private boolean ignoreHiddenFiles;
	private boolean isOldestFileAgeMetricRequired;


	public FileWatcherMonitor() {
		System.out.println(String.format("Using FileWatcherMonitor Version [%s]", getImplementationVersion()));
	}

	private static String getImplementationVersion() {
		return FileWatcherMonitor.class.getPackage().getImplementationTitle();
	}

	protected void initialize(Map<String, String> argsMap) {
		if (configuration == null) {
			MetricWriteHelper metricWriter = MetricWriteHelperFactory.create(this);
			Configuration conf = new Configuration("Custom Metrics|FileWatcher|", new TaskRunner(),metricWriter);
			final String configFilePath = argsMap.get("config-file");
			conf.setConfigYml(configFilePath);
			conf.checkIfInitialized(MonitorConfiguration.ConfItem.METRIC_PREFIX, MonitorConfiguration.ConfItem.CONFIG_YML
					, MonitorConfiguration.ConfItem.EXECUTOR_SERVICE);
			initialiseConfigProperties(conf,conf.getConfigYml());
			this.configuration = conf;
		}
	}

	private void initialiseConfigProperties(Configuration conf, Map<String, ?> configYml) {
		conf.setIgnoreHiddenFiles((Boolean)configYml.get("ignoreHiddenFiles"));
		conf.setIsOldestFileAgeMetricRequired((Boolean)configYml.get("isOldestFileAgeMetricRequired"));
		conf.setIsFileCountRequired((Boolean)configYml.get("isFileCountRequired"));
		conf.setMetricPrefix((String) configYml.get("metricPrefix"));
		if(configYml.get("fileToProcess")!=null){
			List<FileToProcess> files = new ArrayList<FileToProcess>();
			List<Map> filesToProcess = (List<Map>) configYml.get("fileToProcess");
			for(Map m : filesToProcess){
				FileToProcess f = new FileToProcess();
				f.setDisplayName((String) m.get("displayName"));
				f.setIgnoreHiddenFiles((Boolean) m.get("ignoreHiddenFiles"));
				f.setIncludeDirectoryContents((Boolean) m.get("includeDirectoryContents"));
				f.setPath((String) m.get("path"));
				files.add(f);
			}
			conf.setFileToProcess(files);
		}
		else{
			logger.debug("Empty files to process");
		}
		
	}

	private class TaskRunner implements Runnable{

		public void run() {
			List<FileToProcess> files = configuration.getFileToProcess();
			for(FileToProcess file : files){
				FileWatcherMonitorTask task = new FileWatcherMonitorTask(configuration, file);
				configuration.getExecutorService().execute(task);
			}
		}
	}

	public TaskOutput execute(Map<String, String> map, TaskExecutionContext arg1) throws TaskExecutionException {
		logger.info(String.format("Using FileWatcherMonitor Version [%s]", getImplementationVersion()));
		logger.debug("The raw arguments are {}", map);
		try {
			initialize(map);
			// no point continuing if we don't have this
			if (this.configuration.getFileToProcess().isEmpty()) {
				logger.debug("Nothing to do");
				return new TaskOutput("Failure");
			}

			isFileCountRequired = this.configuration.getIsFileCountRequired();
			ignoreHiddenFiles = this.configuration.getIgnoreHiddenFiles();
			isOldestFileAgeMetricRequired = this.configuration.getIsOldestFileAgeMetricRequired();
			logger.debug("Dumping the configurations: ");
			logger.debug("Total files to process = " + this.configuration.getFileToProcess().size());
			logger.debug("Options set in config file: isFileCountRequired = " + isFileCountRequired  +
					" ,ignoreHiddenFiles = " + ignoreHiddenFiles + " ,isOldestFileAgeMetricRequired = " + isOldestFileAgeMetricRequired);
			logger.debug("Metric prefix = " + metricPrefix);
			configuration.executeTask();
		}
		catch(Exception e){
			e.printStackTrace();
			if(configuration != null && configuration.getMetricWriter() != null) {
				configuration.getMetricWriter().registerError(e.getMessage(), e);
			}
		}
		return null;
	}

	public static void main(String[] args) throws TaskExecutionException {
		FileWatcherMonitor fileWatcherMonitor = new FileWatcherMonitor();
		Map<String, String> argsMap = new HashMap<String, String>();
		argsMap.put("config-file", "/Users/aditya.jagtiani/repos/appdynamics/extensions/AppDynamics-File-Watcher-Extension/src/main/resources/conf/config.yml");
		fileWatcherMonitor.execute(argsMap, null);
	}
}
