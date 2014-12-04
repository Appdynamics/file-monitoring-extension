package com.appdynamics.extensions.fileWatcher;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.fileWatcher.config.Configuration;
import com.appdynamics.extensions.fileWatcher.config.FileToProcess;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by abhi.pandey on 8/19/14.
 */
public class FileWatcherMonitor extends AManagedMonitor {

    protected final Logger logger = Logger.getLogger(FileWatcherMonitor.class.getName());
    private String metricPrefix;
    private boolean isFileCountRequired;
    public static final String CONFIG_ARG = "config-file";
    public static final String LOG_PREFIX = "log-prefix";
    private static String logPrefix;
    public static final String METRIC_SEPARATOR = "|";

    private static Map<String, FileMetric> mapOfFilesToMonitor = Maps.newHashMap();
    private Map<String, String> filesToProcessMap = Maps.newHashMap();

    /**
     * This is the entry point to the monitor called by the Machine Agent
     *
     * @param taskArguments
     * @param taskContext
     * @return
     * @throws com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException
     */
    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskContext) throws TaskExecutionException {
        if (taskArguments != null && !taskArguments.isEmpty()) {
            setLogPrefix(taskArguments.get(LOG_PREFIX));
            logger.info("Using Monitor Version [" + getImplementationVersion() + "]");
            logger.info(getLogPrefix() + "Starting the FileWatcher Monitoring task.");
            if (logger.isDebugEnabled()) {
                logger.debug(getLogPrefix() + "Task Arguments Passed ::" + taskArguments);
            }
            String status = "Success";

            String configFilename = getConfigFilename(taskArguments.get(CONFIG_ARG));

            try {
                //read the config.
                Configuration config = YmlReader.readFromFile(configFilename, Configuration.class);

                // no point continuing if we don't have this
                if (config.getFileToProcess().isEmpty()) {
                    return new TaskOutput("Failure");
                }

                this.isFileCountRequired = config.getIsFileCountRequired();
                processMetricPrefix(config.getMetricPrefix());

                status = getStatus(config, status);
            } catch (Exception e) {
                logger.error("Exception", e);
            }

            return new TaskOutput(status);
        }
        throw new TaskExecutionException(getLogPrefix() + "FileWatcher monitoring task completed with failures.");

    }

    private String getStatus(Configuration config, String status) {
        try {
            List<FileToProcess> files = config.getFileToProcess();
            FileProcessor fp = new FileProcessor();
            fp.setMETRIC_SEPARATOR(METRIC_SEPARATOR);
            fp.createListOfPaths(files);
            filesToProcessMap = fp.processDisplayName(files);

            for (String key : filesToProcessMap.keySet()) {
                FileMetric fileMetric = fp.getFileMetric(key);
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
            processMetric();
        } catch (Exception e) {
            logger.error("Error in processing the files:" + e);
            status = "Failure";
        }
        return status;
    }


    private void processMetric() {
        if (!mapOfFilesToMonitor.isEmpty()) {

            Set<String> keys = mapOfFilesToMonitor.keySet();

            for (String key : keys) {
                StringBuffer metricPath = new StringBuffer();
                metricPath.append(metricPrefix).append(key).append(METRIC_SEPARATOR);

                FileMetric fileMetric = mapOfFilesToMonitor.get(key);

                String metricName = "Size";
                String metricValue = fileMetric.getFileSize();
                printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);

                metricName = "IsModified";
                metricValue = toNumeralString(fileMetric.isChanged());
                printCollectiveObservedCurrent(metricPath.toString() + metricName, metricValue);

                if (this.isFileCountRequired && fileMetric.getNumberOfFiles() >= 0) {
                    metricName = "FileCount";
                    metricValue = String.valueOf(fileMetric.getNumberOfFiles());
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
        metricWriter.printMetric(metricValue);
    }


    private void printCollectiveObservedCurrent(String metricPath, String metricValue) {
        printMetric(metricPath, metricValue,
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
        );
    }


    /**
     * Returns a config file name,
     *
     * @param filename
     * @return String
     */

    private String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }
        //for absolute paths
        if (new java.io.File(filename).exists()) {
            return filename;
        }
        //for relative paths
        java.io.File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + java.io.File.separator + filename;
        }
        return configFileName;
    }

    private void processMetricPrefix(String metricPrefix) {

        if (!metricPrefix.endsWith("|")) {
            metricPrefix = metricPrefix + "|";
        }
        if (!metricPrefix.startsWith("Custom Metrics|")) {
            metricPrefix = "Custom Metrics|" + metricPrefix;
        }

        this.metricPrefix = metricPrefix;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        this.logPrefix = (logPrefix != null) ? logPrefix : "";
    }

    private static String getImplementationVersion() {
        return FileWatcherMonitor.class.getPackage().getImplementationTitle();
    }
}
