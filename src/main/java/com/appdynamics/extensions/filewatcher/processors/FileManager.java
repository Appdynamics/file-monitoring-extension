/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.processors;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.appdynamics.extensions.metrics.Metric;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FileManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    private WatchService watchService;
    private Map<WatchKey, Path> watchKeys;
    private String baseDirectory;
    private PathToProcess pathToProcess;
    private FileMetricsProcessor fileMetricsProcessor;
    private MetricWriteHelper metricWriteHelper;
    private Map<String, FileMetric> fileMetrics;

    public FileManager(WatchService watchService, Map<WatchKey, Path> watchKeys, String baseDirectory,
                       PathToProcess pathToProcess,
                       MetricWriteHelper metricWriteHelper, FileMetricsProcessor fileMetricsProcessor) {
        this.watchService = watchService;
        this.watchKeys = watchKeys;
        this.baseDirectory = baseDirectory;
        this.pathToProcess = pathToProcess;
        this.fileMetricsProcessor = fileMetricsProcessor;
        this.metricWriteHelper = metricWriteHelper;
        this.fileMetrics = new HashMap<>();
    }

    public void run() {
        LOGGER.info("Attempting to walk directory {}", baseDirectory);
        try {
            walk(baseDirectory);
            printMetrics();
            watch();
        } catch (InterruptedException | IOException ex) {
            LOGGER.error("Error encountered while walking File {}", baseDirectory, ex);
        }
    }

    private void walk(String baseDirectory) throws IOException {
        GlobPathMatcher globPathMatcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Files.walkFileTree(Paths.get(baseDirectory), new CustomFileVisitor(baseDirectory, globPathMatcher, pathToProcess, fileMetrics));
    }

    private void printMetrics() {
        List<Metric> metrics = fileMetricsProcessor.getMetricList(fileMetrics);
        metricWriteHelper.transformAndPrintMetrics(metrics);
    }

    private void watch() throws InterruptedException, IOException {
        LOGGER.info("Watching directory {} for events", baseDirectory);
        registerPath(Paths.get(baseDirectory));
        WatchKey watchKey;
        while (true) {
            watchKey = watchService.poll(600, TimeUnit.SECONDS);
            if (watchKey != null) {
                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = watchEvent.kind();
                    if ((kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_DELETE ||
                            kind == StandardWatchEventKinds.ENTRY_MODIFY)) {
                        Path eventPath = (Path) watchEvent.context();
                        Path directory = watchKeys.get(watchKey);
                        File child = directory.resolve(eventPath).toFile();
                        LOGGER.info("Event {} detected for path {}", kind, child);
                        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            handleFileDeletion(child);
                        }
                        walk(baseDirectory);
                    }
                }

                boolean valid = watchKey.reset();
                if (!valid) {
                    watchKeys.remove(watchKey);
                    if (watchKeys.isEmpty()) {
                        break;
                    }
                }
            }
            printMetrics();
        }
    }

    private void resetCurrentFileMetrics(Map<String, FileMetric> fileMetrics) {
        fileMetrics.replaceAll((key, value) -> new FileMetric());
    }

    private void registerPath(Path path) throws IOException {
        if (!watchKeys.containsValue(path)) {
            LOGGER.debug("Now registering path {} with the Watch Service", path.getFileName());
            WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            watchKeys.put(key, path);
        }
    }

    // This is done here because the globpatternmatcher can never process a file that does not exist anymore.
    private void handleFileDeletion(File childPath) {
        for (Map.Entry<String, FileMetric> entry : fileMetrics.entrySet()) {
            if (entry.getKey().contains(childPath.getName())) {
                FileMetric fileMetric = entry.getValue();
                fileMetric.setAvailable(false);
                fileMetric.setFileSize("0");
                fileMetric.setChanged(true);
                if (childPath.isFile()) {
                    fileMetric.setNumberOfLines(0);
                } else if (childPath.isDirectory()) {
                    fileMetric.setNumberOfFiles(0);
                    fileMetric.setRecursiveNumberOfFiles(0);
                }
                fileMetrics.put(entry.getKey(), fileMetric);
            }
        }
    }
}