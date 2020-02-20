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
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileWatcherTask implements AMonitorTaskRunnable {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileWatcherTask.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private MetricWriteHelper metricWriteHelper;
    private PathToProcess pathToProcess;
    private Map<WatchKey, Path> keys;
    private WatchService watchService;
    private Map<String, FileMetric> fileMetrics;
    private String baseDirectory;
    private WatchKey watchKey;
    private FileMetricsProcessor fileMetricsProcessor;
    private MonitorExecutorService executorService;

    FileWatcherTask(MonitorContextConfiguration monitorContextConfiguration,
                    MetricWriteHelper metricWriteHelper, PathToProcess pathToProcess) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.pathToProcess = pathToProcess;
        this.keys = new HashMap<>();
        this.fileMetrics = new HashMap<>();
        this.fileMetricsProcessor = new FileMetricsProcessor(monitorContextConfiguration.getMetricPrefix(), (Map) monitorContextConfiguration.getConfigYml().get("metrics"));
        this.executorService = monitorContextConfiguration.getContext().getExecutorService();
    }

    @Override
    public void run() {
        baseDirectory = new FilePathProcessor().getBaseDirectories(pathToProcess).get(0);
        try {
            processDirectory(baseDirectory);
        } catch (Exception ex) {
            LOGGER.error("Task failed for directory {}", baseDirectory, ex);
        }
    }

    private void processDirectory(String baseDirectory) {
        Path start = Paths.get(baseDirectory);
        LOGGER.info("Now processing directory: {}", start.getFileName());
        try {
            watchService = FileSystems.getDefault().newWatchService();
            executorService.execute("Initial File Walker", new FileWalker(start, watchService, baseDirectory,
                    pathToProcess, keys, metricWriteHelper, fileMetricsProcessor));
            watchDirectoryForEvents();
        } catch (Exception e) {
            LOGGER.error("Error encountered while registering directory: {}", start, e);
        }
    }


    private void watchDirectoryForEvents() throws InterruptedException {
        while (true) {
            watchKey = watchService.take();
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                if ((kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_DELETE ||
                        kind == StandardWatchEventKinds.ENTRY_MODIFY)) {

                    Path eventPath = (Path) watchEvent.context();

                    Path directory = keys.get(watchKey);
                    // Path directory = (Path) key.watchable(); //problems with renames
                    String childPath = directory.resolve(eventPath).toFile().getAbsolutePath();
                    LOGGER.info("Event {} detected for path {}", kind, childPath);

                    Path startingDirectoryForWatchEvent = Paths.get(childPath.substring(0,
                            FilenameUtils.indexOfLastSeparator(childPath) + 1));

                    executorService.execute("WatchService File Walker", new FileWalker(startingDirectoryForWatchEvent,
                            watchService, baseDirectory, pathToProcess, keys, metricWriteHelper, fileMetricsProcessor));
                }
            }
            //  System.out.printf("%s:%s\n", child, kind);

            boolean valid = watchKey.reset();
            if (!valid) {
                keys.remove(watchKey);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
        // todo remove trailing slash
        //todo file metrics is getting re-written - wtf?
        // todo metric printing logic to stop the watch service
        //todo - execute walk + metric collection in parallel for events or execute a listener on a list that stores all the events to prevent any overflow in case of longer walking times
    }

    @Override
    public void onTaskComplete() {
        if (watchKey != null) {
            watchKey.reset();
        }
        LOGGER.info("Finished collecting metrics for {}", pathToProcess.getDisplayName());
    }
}
