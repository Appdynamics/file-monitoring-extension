package com.appdynamics.extensions.filewatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class FileWatcherMonitor extends AManagedMonitor{

	public static final Logger logger = LoggerFactory.getLogger(FileWatcherMonitor.class);
	private Configuration configuration;
	private String metricPrefix;
	private boolean isFileCountRequired;
	private boolean isDirectoryDetailsRequired;
	private boolean ignoreHiddenFiles;
	private boolean isOldestFileAgeMetricRequired;
	private Map<String, String> filesToProcessMap = Maps.newHashMap();
	private static final String METRIC_SEPARATOR = "|";
	private static String logPrefix;
	private static CountDownLatch latch;

	public FileWatcherMonitor() {
		logger.info(String.format("Using NGinXMonitor Version [%s]", getImplementationVersion()));
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
		conf.setIsDirectoryDetailsRequired((Boolean)configYml.get("isDirectoryDetailsRequired"));
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
				f.setIsDirectoryDetailsRequired((Boolean) m.get("isDirectoryDetailsRequired"));
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
		logger.debug("The raw arguments are {}", map);
		String status = "Success";
		try {
			initialize(map);
			// no point continuing if we don't have this
			if (this.configuration.getFileToProcess().isEmpty()) {
				logger.debug("Nothing to do");
				return new TaskOutput("Failure");
			}

			isFileCountRequired = this.configuration.getIsFileCountRequired();
			isDirectoryDetailsRequired = this.configuration.getIsDirectoryDetailsRequired();
			ignoreHiddenFiles = this.configuration.getIgnoreHiddenFiles();
			isOldestFileAgeMetricRequired = this.configuration.getIsOldestFileAgeMetricRequired();
			logger.debug("Dumping the configurations: ");
			logger.debug("Total files to process = " + this.configuration.getFileToProcess().size());
			logger.debug("Options set in config file: isFileCountRequired = " + isFileCountRequired + " ,isDirectoryDetailsRequired = " + isDirectoryDetailsRequired +
					" ,ignoreHiddenFiles = " + ignoreHiddenFiles + " ,isOldestFileAgeMetricRequired = " + isOldestFileAgeMetricRequired);
			logger.debug("Metric prefix = " + metricPrefix);
			processMetricPrefix(this.configuration.getMetricPrefix());
			latch = new CountDownLatch(configuration.getFileToProcess().size());
			configuration.executeTask();
			//#TODO latch is not needed. Please move the reporting part in FileWatcherMonitorTask
			getLatch().await();
			status = getStatus(configuration, status);
			logger.info("Status = " + status);

		}
		catch(Exception e){
			e.printStackTrace();
			if(configuration != null && configuration.getMetricWriter() != null) {
				configuration.getMetricWriter().registerError(e.getMessage(), e);
			}
		}
		return null;
	}

	private void processMetricPrefix(String metricPrefix) {
		logger.debug("Processing the metric prefix");
		if (!metricPrefix.endsWith("|")) {
			metricPrefix = metricPrefix + "|";
		}
		if (!metricPrefix.startsWith("Custom Metrics|")) {
			metricPrefix = "Custom Metrics|" + metricPrefix;
		}

		this.metricPrefix = metricPrefix;
	}

	private String getStatus(Configuration config, String status) {
		try {
			Map<String, FileMetric> mapOfFilesToMonitor = Maps.newHashMap();
			filesToProcessMap = FileWatcherMonitorTask.getFilestoprocess();

			for (String key : filesToProcessMap.keySet()) {
				FileMetric fileMetric = FileProcessor.getFileMetric(key, ignoreHiddenFiles);
				if (fileMetric != null) {
					String displayName = filesToProcessMap.get(key);
					if (mapOfFilesToMonitor.containsKey(displayName)) {
						if (!fileMetric.getTimeStamp().equals(mapOfFilesToMonitor.get(displayName).getTimeStamp())) {
							fileMetric.setChanged(true);

						} else {
							fileMetric.setChanged(false);
						}
						mapOfFilesToMonitor.put(displayName, fileMetric);
					} else {
						mapOfFilesToMonitor.put(displayName, fileMetric);
					}
				}
			}
			processMetric(mapOfFilesToMonitor);
		} catch (Exception e) {
			logger.error("Error in processing the files:" + e);
			status = "Failure";
		}
		return status;
	}
	private void processMetric(Map<String, FileMetric> mapOfFiles) {
		if (!mapOfFiles.isEmpty()) {

			Set<String> keys = mapOfFiles.keySet();

			for (String key : keys) {
				StringBuffer metricPath = new StringBuffer();
				metricPath.append(metricPrefix).append(key).append(METRIC_SEPARATOR);

				FileMetric fileMetric = mapOfFiles.get(key);

				String metricName = "Size";
				String metricValue = fileMetric.getFileSize();
				printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);

				metricName = "IsModified";
				metricValue = toNumeralString(fileMetric.isChanged());
				printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);

				if (isFileCountRequired && fileMetric.getNumberOfFiles() >= 0) {
					metricName = "FileCount";
					metricValue = String.valueOf(fileMetric.getNumberOfFiles());
					printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);
				}

				if (isOldestFileAgeMetricRequired && fileMetric.getOldestFileAge() >= 0) {
					metricName = "OldestFileAge";
					metricValue = String.valueOf(fileMetric.getOldestFileAge());
					printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);
				}

			}
		}
	}

	private String toNumeralString(final Boolean input) {
		if (input == null) {
			return "null";
		} else {
			return input.booleanValue() ? "1" : "0";
		}
	}

	/**
	 * A helper method to report the metrics.
	 *
	 * @param metricPath
	 * @param metricValue
	 * @param aggType
	 * @param timeRollupType
	 * @param clusterRollupType
	 */
	public void printMetric(String metricPath, String metricValue, String aggType, String timeRollupType, String clusterRollupType) {
		MetricWriter metricWriter = getMetricWriter(metricPath,
				aggType,
				timeRollupType,
				clusterRollupType
				);

		if (logger.isDebugEnabled()) {
			logger.debug(getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
					+ "] metric = " + metricPath + " = " + metricValue);
		}

		/*System.out.println((getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                + "] metric = " + metricPath + " = " + metricValue));*/

		metricWriter.printMetric(metricValue);
	}


	private void printCollectiveObservedCurrent(String metricPath, String metricValue) {
		printMetric(metricPath, metricValue,
				MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
				);
	}


	public String getLogPrefix() {
		return logPrefix;
	}

	public void setLogPrefix(String logPrefix) {
		this.logPrefix = (logPrefix != null) ? logPrefix : "";
	}

	public static CountDownLatch getLatch() {
		return latch;
	}

}
