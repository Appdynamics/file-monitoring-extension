package com.appdynamics.extensions.filewatcher;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Created by abhi.pandey on 8/19/14.
 */
public class FileWatcherMonitor extends AManagedMonitor {

    protected final Logger logger = Logger.getLogger(FileWatcherMonitor.class.getName());
    private String metricPrefix;
    private boolean isFileCountRequired;
    private boolean isDirectoryDetailsRequired;
    private boolean ignoreHiddenFiles;
    private boolean isOldestFileAgeMetricRequired;
    private static final String CONFIG_ARG = "config-file";
    private static final String LOG_PREFIX = "log-prefix";
    private static String logPrefix;
    private static final String METRIC_SEPARATOR = "|";
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

                isFileCountRequired = config.getIsFileCountRequired();
                isDirectoryDetailsRequired = config.getIsDirectoryDetailsRequired();
                ignoreHiddenFiles = config.getIgnoreHiddenFiles();
                isOldestFileAgeMetricRequired = config.getIsOldestFileAgeMetricRequired();
                logger.debug("Dumping the configurations: ");
                logger.debug("Total files to process = " + config.getFileToProcess().size());
                logger.debug("Options set in config file: isFileCountRequired = " + isFileCountRequired + " ,isDirectoryDetailsRequired = " + isDirectoryDetailsRequired +
                        " ,ignoreHiddenFiles = " + ignoreHiddenFiles + " ,isOldestFileAgeMetricRequired = " + isOldestFileAgeMetricRequired);
                logger.debug("Metric prefix = " + metricPrefix);
                processMetricPrefix(config.getMetricPrefix());

                status = getStatus(config, status);
                logger.info("Status = " + status);
            } catch (Exception e) {
                logger.error("Exception", e);
            }

            return new TaskOutput(status);
        }
        throw new TaskExecutionException(getLogPrefix() + "FileWatcher monitoring task completed with failures.");

    }

    private String getStatus(Configuration config, String status) {
        try {
            Map<String, FileMetric> mapOfFilesToMonitor = Maps.newHashMap();
            List<FileToProcess> files = config.getFileToProcess();
            FileProcessor fp = new FileProcessor();
            fp.setMetricSeparator(METRIC_SEPARATOR);
            fp.createListOfPaths(files);
            filesToProcessMap = fp.processDisplayName(files, isDirectoryDetailsRequired);

            for (String key : filesToProcessMap.keySet()) {
                FileMetric fileMetric = fp.getFileMetric(key, ignoreHiddenFiles);
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


    /**
     * Returns a config file name,
     *
     * @param filename
     * @return String
     */

    private String getConfigFilename(String filename) {
        logger.debug("Getting config file name for " + filename);
        if (filename == null) {
            return "";
        }
        //for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        //for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
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
