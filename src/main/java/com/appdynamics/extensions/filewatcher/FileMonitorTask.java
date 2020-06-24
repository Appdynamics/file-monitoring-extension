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
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.processors.FileManager;
import com.appdynamics.extensions.filewatcher.processors.FileMetricsProcessor;
import com.appdynamics.extensions.filewatcher.processors.FilePathProcessor;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.isDirectoryAccessible;

/*
 * @author Aditya Jagtiani
 */

public class FileMonitorTask implements AMonitorTaskRunnable {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileMonitorTask.class);
    private MetricWriteHelper metricWriteHelper;
    private PathToProcess pathToProcess;
    private Map<WatchKey, Path> keys;
    private FileMetricsProcessor fileMetricsProcessor;
    private MonitorExecutorService executorService;
    private CountDownLatch countDownLatch;
    private WatchService watchService;

    FileMonitorTask(MonitorContextConfiguration monitorContextConfiguration,
                    MetricWriteHelper metricWriteHelper, PathToProcess pathToProcess) {
        this.metricWriteHelper = metricWriteHelper;
        this.pathToProcess = pathToProcess;
        this.keys = new ConcurrentHashMap<>();
        this.fileMetricsProcessor = new FileMetricsProcessor(monitorContextConfiguration.getMetricPrefix(),
                (Map) monitorContextConfiguration.getConfigYml().get("metrics"), metricWriteHelper);
        this.executorService = monitorContextConfiguration.getContext().getExecutorService();
    }

    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            List<String> baseDirectories = new FilePathProcessor().getBaseDirectories(pathToProcess);
            countDownLatch = new CountDownLatch(baseDirectories.size());
            for (String baseDirectory : baseDirectories) {
                if (isDirectoryAccessible(Paths.get(baseDirectory))) {
                    LOGGER.info("Configured Path {} accessible, starting File Manager");
                    executorService.execute("File Manager", new FileManager(watchService, keys, baseDirectory,
                            pathToProcess, fileMetricsProcessor, countDownLatch));
                } else {
                    LOGGER.error("Cannot monitor configured path {} as its base directory {} either does not exist or " +
                            "has insufficient permissions. Assign read & execute permissions to the base directory for " +
                            "the current machine agent user in order to monitor this path.", pathToProcess.getPath(), baseDirectory);
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Task failed for path {}", pathToProcess.getPath(), ex);
        }
    }

    @Override
    public void onTaskComplete() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.error("An unexpected error occurred while completing task", e);
        } finally {
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.error("Error encountered while closing WatchService", e);
            }
        }
    }
}