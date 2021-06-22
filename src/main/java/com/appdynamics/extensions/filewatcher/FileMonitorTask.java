/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.processors.FileMetricsProcessor;
import com.appdynamics.extensions.filewatcher.processors.FilePathProcessor;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.isDirectoryAccessible;
import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.walk;

/*
 * @author Aditya Jagtiani
 */

public class FileMonitorTask implements AMonitorTaskRunnable {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileMonitorTask.class);
    private PathToProcess pathToProcess;
    private FileMetricsProcessor fileMetricsProcessor;
    private MonitorExecutorService executorService;
    private Map<String, FileMetric> fileMetrics;

    FileMonitorTask(MonitorContextConfiguration monitorContextConfiguration,
                    MetricWriteHelper metricWriteHelper, PathToProcess pathToProcess) {
        this.pathToProcess = pathToProcess;
        this.fileMetricsProcessor = new FileMetricsProcessor(monitorContextConfiguration.getMetricPrefix(),
                (Map) monitorContextConfiguration.getConfigYml().get("metrics"), metricWriteHelper);
        this.executorService = monitorContextConfiguration.getContext().getExecutorService();
    }

    @Override
    public void run() {
        try {
            List<String> baseDirectories = new FilePathProcessor().getBaseDirectories(pathToProcess);
            for (String baseDirectory : baseDirectories) {
                fileMetrics = new HashMap<>();
                if (isDirectoryAccessible(Paths.get(baseDirectory))) {
                    LOGGER.info("Configured Path {} accessible, starting to walk directory and collecting metrics.",baseDirectory);
                    if(pathToProcess.getPath().endsWith("\\**") || pathToProcess.getPath().endsWith("/**")){
                        LOGGER.info("Path is configured as fully recursive with ** wildcard. Calculating recursive directory count");
                        long recursiveDirectoryCount = FileWatcherUtil.calculateRecursiveDirectoryCount(Paths.get(baseDirectory),pathToProcess.getIgnoreHiddenFiles());
                        LOGGER.info("Recursive directory count is "+ recursiveDirectoryCount);
                        if(recursiveDirectoryCount > 50){
                            LOGGER.warn("Recursive directory count is more than 50. Printing base directory metrics only for path "+pathToProcess.getPath());
                            String currentPath = pathToProcess.getPath();
                            pathToProcess.setPath(currentPath.substring(0,currentPath.length()-1));
                        } else {
                            LOGGER.info("Collecting all metrics for path "+pathToProcess.getPath()+" since recursive directory count is less than 50");
                        }
                    }
                    LOGGER.trace("FileMonitorTask :: run - Starting to walk...");
                    walk(baseDirectory, pathToProcess, fileMetrics);
                    LOGGER.trace("FileMonitorTask :: run - Completed walk!!!");
                    fileMetricsProcessor.printMetrics(fileMetrics);
                } else {
                    LOGGER.error("Cannot monitor configured path {} as its base directory {} either does not exist or " +
                            "has insufficient permissions. Assign read & execute permissions to the base directory for " +
                            "the current machine agent user in order to monitor this path.", pathToProcess.getPath(), baseDirectory);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Task failed for name {}", pathToProcess.getDisplayName(), ex);
        }
    }

    @Override
    public void onTaskComplete() {
        LOGGER.info("Completed task for name "+pathToProcess.getDisplayName());
    }
}