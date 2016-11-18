package com.appdynamics.extensions.filewatcher;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;

public class FileWatcherMonitorTask implements Runnable{

	public static final Logger logger = Logger.getLogger(FileWatcherMonitorTask.class);
	private static final String METRIC_SEPARATOR = "|";
	private static final Map<String, String> filesToProcess = new HashMap<String, String>();
	private FileToProcess fileToProcess;
	private Configuration configuration;

	public FileWatcherMonitorTask(Configuration configuration, FileToProcess fileToProcess) {
		this.configuration = configuration;
		this.fileToProcess = fileToProcess;
	}

	public void run() {
		FileProcessor proc = new FileProcessor();
		proc.setMetricSeparator(METRIC_SEPARATOR);
		getFilestoprocess().putAll(proc.processDisplayName(configuration,fileToProcess));
		FileWatcherMonitor.getLatch().countDown();
	}

	//#TODO This method will no longer be needed and have to be referenced from FileWatcherMonitor once you have reporting piece moved in here.

	public static Map<String, String> getFilestoprocess() {
		return filesToProcess;
	}
}
