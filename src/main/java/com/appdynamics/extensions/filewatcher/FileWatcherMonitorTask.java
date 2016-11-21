package com.appdynamics.extensions.filewatcher;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.singularity.ee.agent.systemagent.api.MetricWriter;

public class FileWatcherMonitorTask implements Runnable{

	public static final Logger logger = Logger.getLogger(FileWatcherMonitorTask.class);
	private static final String METRIC_SEPARATOR = "|";

	private FileToProcess fileToProcess;
	private Configuration configuration;
	private String metricPrefix;
	private static Map<String,FileMetric> fileMetricsMap = new ConcurrentHashMap<String, FileMetric>();
	private static String logPrefix;

	public FileWatcherMonitorTask(Configuration configuration, FileToProcess fileToProcess) {
		this.configuration = configuration;
		this.fileToProcess = fileToProcess;
	}

	public void run() {
		FileProcessor proc = new FileProcessor();
		proc.processFilePath(configuration,fileToProcess,fileMetricsMap);
		processMetricPrefix(this.configuration.getMetricPrefix());
		processMetric(fileMetricsMap);
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

				if (configuration.getIsFileCountRequired() && fileMetric.getNumberOfFiles() >= 0) {
					metricName = "FileCount";
					metricValue = String.valueOf(fileMetric.getNumberOfFiles());
					printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);
				}

				if (configuration.getIsOldestFileAgeMetricRequired() && fileMetric.getOldestFileAge() >= 0) {
					metricName = "OldestFileAge";
					metricValue = String.valueOf(fileMetric.getOldestFileAge());
					printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);
				}

			}
		}
	}

	private void printCollectiveObservedCurrent(String metricPath, String metricValue) {
		printMetric(metricPath, metricValue,
				MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
				);
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
		MetricWriteHelper metricWriter = configuration.getMetricWriter();

		if (logger.isDebugEnabled()) {
			logger.debug(getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
					+ "] metric = " + metricPath + " = " + metricValue);
		}

		/*System.out.println((getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                + "] metric = " + metricPath + " = " + metricValue));*/

		metricWriter.printMetric(metricPath, metricValue, aggType, timeRollupType, clusterRollupType);
	}


	private String toNumeralString(final Boolean input) {
		if (input == null) {
			return "null";
		} else {
			return input.booleanValue() ? "1" : "0";
		}
	}

	public String getLogPrefix() {
		return logPrefix;
	}

	public void setLogPrefix(String logPrefix) {
		this.logPrefix = (logPrefix != null) ? logPrefix : "";
	}

}
