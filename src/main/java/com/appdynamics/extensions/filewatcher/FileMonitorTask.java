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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.isNetworkPathAccessible;
import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.isWindowsNetworkPath;

public class FileMonitorTask implements AMonitorTaskRunnable {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileMonitorTask.class);
    private MetricWriteHelper metricWriteHelper;
    private PathToProcess pathToProcess;
    private Map<WatchKey, Path> keys;
    private FileMetricsProcessor fileMetricsProcessor;
    private MonitorExecutorService executorService;
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
            for (String baseDirectory : baseDirectories) {
                if (isWindowsNetworkPath(baseDirectory)) {
                    if (isNetworkPathAccessible(baseDirectory)) {
                        LOGGER.info("Windows Network Path {} accessible, starting File Manager");
                        executorService.execute("File Manager", new FileManager(watchService, keys, baseDirectory,
                                pathToProcess, fileMetricsProcessor));
                    } else {
                        LOGGER.error("Windows Network Path {} inaccessible. Please check the file path and user permissions. " +
                                "Skipping", baseDirectory);
                    }
                } else {
                    executorService.execute("File Manager", new FileManager(watchService, keys, baseDirectory,
                            pathToProcess, fileMetricsProcessor));
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Task failed for path {}", pathToProcess.getPath(), ex);
        }
    }

    @Override
    public void onTaskComplete() {}
}