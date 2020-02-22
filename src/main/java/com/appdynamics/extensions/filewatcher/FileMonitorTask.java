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
import com.appdynamics.extensions.filewatcher.processors.FileWalker;
import com.appdynamics.extensions.filewatcher.processors.FileWatcher;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMonitorTask implements AMonitorTaskRunnable {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileMonitorTask.class);
    private MetricWriteHelper metricWriteHelper;
    private PathToProcess pathToProcess;
    private Map<WatchKey, Path> keys;
    private WatchService watchService;
    private FileMetricsProcessor fileMetricsProcessor;
    private MonitorExecutorService executorService;

    FileMonitorTask(MonitorContextConfiguration monitorContextConfiguration,
                    MetricWriteHelper metricWriteHelper, PathToProcess pathToProcess) {
        this.metricWriteHelper = metricWriteHelper;
        this.pathToProcess = pathToProcess;
        this.keys = new HashMap<>();
        this.fileMetricsProcessor = new FileMetricsProcessor(monitorContextConfiguration.getMetricPrefix(),
                (Map) monitorContextConfiguration.getConfigYml().get("metrics"));
        this.executorService = monitorContextConfiguration.getContext().getExecutorService();
    }

    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            List<String> baseDirectories = new FilePathProcessor().getBaseDirectories(pathToProcess);
            for(String baseDirectory : baseDirectories) {
                processDirectory(baseDirectory);
            }
        } catch (Exception ex) {
            LOGGER.error("Task failed for path {}", pathToProcess.getPath(), ex);
        }
    }

    private void processDirectory(String baseDirectory) {
        Path start = Paths.get(baseDirectory);
        LOGGER.info("Now processing directory: {}", start.getFileName());
        try {
            // TODO - change to callable and return metrics
            executorService.execute("Initial File Walker", new FileWalker(start, watchService, baseDirectory,
                    pathToProcess, keys, metricWriteHelper, fileMetricsProcessor));
            new FileWatcher(baseDirectory, keys, executorService, pathToProcess,
                    metricWriteHelper, fileMetricsProcessor, watchService).run();
            //executorService.execute("File Watcher", new FileWatcher(baseDirectory, keys, executorService, pathToProcess,
                  //  metricWriteHelper, fileMetricsProcessor, watchService));
        } catch (Exception e) {
            LOGGER.error("Error encountered while registering directory: {}", start, e);
        }
    }

    //todo file metrics is getting re-written - wtf? - DONE
        // todo metric printing logic to stop the watch service - DONE
        //todo - execute walk + metric collection in parallel for events or execute a listener on a list that stores all the events to prevent any overflow in case of longer walking times- DONE


    @Override
    public void onTaskComplete() {
        LOGGER.info("Finished collecting metrics for {}", pathToProcess.getDisplayName());
    }
}
