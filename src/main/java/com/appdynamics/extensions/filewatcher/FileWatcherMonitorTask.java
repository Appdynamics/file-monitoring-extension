/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.filewatcher;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.PerMinValueCalculator;
import com.singularity.ee.agent.systemagent.api.MetricWriter;

public class FileWatcherMonitorTask implements Runnable{

	public static final Logger logger = Logger.getLogger(FileWatcherMonitorTask.class);
	private static final String METRIC_SEPARATOR = "|";
	private FileToProcess fileToProcess;
	private Configuration configuration;
	private Map<String,FileMetric> fileMetricsMap = new HashMap<String, FileMetric>();
	private static PerMinValueCalculator perMinValueCalculator = new PerMinValueCalculator();

	public FileWatcherMonitorTask(Configuration configuration, FileToProcess fileToProcess) {
		this.configuration = configuration;
		this.fileToProcess = fileToProcess;
	}

	public void run() {
		FileProcessor proc = new FileProcessor();
		proc.processFilePath(configuration,fileToProcess,fileMetricsMap);
		processMetric(fileMetricsMap);
	}


	private void processMetric(Map<String, FileMetric> mapOfFiles) {
		if (!mapOfFiles.isEmpty()) {

			Set<String> keys = mapOfFiles.keySet();

			for (String key : keys) {
				StringBuffer metricPath = new StringBuffer();
				metricPath.append(this.configuration.getMetricPrefix()).append(key).append(METRIC_SEPARATOR);

				FileMetric fileMetric = mapOfFiles.get(key);

				String metricName = "Size";
				String metricValue = fileMetric.getFileSize();
				printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);

				//perMinValue backing cache is thread safe
				BigDecimal prevTs = perMinValueCalculator.getPerMinuteValue(metricPath.toString() + metricName, new BigDecimal(fileMetric.getTimeStamp()));
				if(prevTs != null && (prevTs.compareTo(BigDecimal.ZERO) > 0)){
					fileMetric.setChanged(true);
				}

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
		logger.debug("printing metric for : " + metricPath);
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
			logger.debug("Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
					+ "] metric = " + metricPath + " = " + metricValue);
		}

		/*System.out.println((getLogPrefix() + "Sending [" + aggType + METRIC_SEPARATOR + timeRollupType + METRIC_SEPARATOR + clusterRollupType
                + "] metric = " + metricPath + " = " + metricValue));*/
		//This is causing issues with metricPrefix having a ":" (case of reporting to a particular tier). Not sure why this was added but commented for now.
		/*if(metricPath.contains(":")){
			metricPath = metricPath.replace(":", "");
		}*/
		metricWriter.printMetric(metricPath, metricValue, aggType, timeRollupType, clusterRollupType);
	}


	private String toNumeralString(final Boolean input) {
		if (input == null) {
			return "null";
		} else {
			return input.booleanValue() ? "1" : "0";
		}
	}
}
