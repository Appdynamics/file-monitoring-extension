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

import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FileWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);

    private WatchService watchService;
    private Map<WatchKey, Path> watchKeys;
    private String baseDirectory;
    private Map<String, FileMetric> fileMetrics;
    private PathToProcess pathToProcess;
    private FileMetricsProcessor fileMetricsProcessor;

    public FileWatcher(WatchService watchService, Map<WatchKey, Path> watchKeys,
                       String baseDirectory, Map<String, FileMetric> fileMetrics,
                       PathToProcess pathToProcess, FileMetricsProcessor fileMetricsProcessor) {
        this.watchService = watchService;
        this.watchKeys = watchKeys;
        this.baseDirectory = baseDirectory;
        this.fileMetrics = fileMetrics;
        this.pathToProcess = pathToProcess;
        this.fileMetricsProcessor = fileMetricsProcessor;
    }

    public void processWatchEvents() throws InterruptedException, IOException {
        WatchKey watchKey = watchService.poll(60, TimeUnit.SECONDS);
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
            }
        }
        fileMetricsProcessor.printMetrics(fileMetrics);
    }

    private void walk(String baseDirectory) throws IOException {
        GlobPathMatcher globPathMatcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Files.walkFileTree(Paths.get(baseDirectory), new CustomFileWalker(baseDirectory, globPathMatcher, pathToProcess, fileMetrics));
    }

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