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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.walk;

public class FileWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);

    private WatchService watchService;
    private Map<WatchKey, Path> watchKeys;
    private String baseDirectory;
    private Map<String, FileMetric> fileMetrics;
    private PathToProcess pathToProcess;
    private FileMetricsProcessor fileMetricsProcessor;
    private boolean isEventDetected;

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
        LOGGER.info("Polling the Watch Service for Events..");
        WatchKey watchKey = watchService.poll(60, TimeUnit.SECONDS);
        if (watchKey != null) {
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                if ((kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_DELETE ||
                        kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.OVERFLOW)) {
                    Path eventPath = (Path) watchEvent.context();
                    Path directory = watchKeys.get(watchKey);
                    File child = directory.resolve(eventPath).toFile();
                    LOGGER.info("Event {} detected for path {}. Processing..", kind, child);
                    isEventDetected = true;
                    if (kind == StandardWatchEventKinds.ENTRY_DELETE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        handleFileDeletion(child);
                    }
                    walk(baseDirectory, pathToProcess, fileMetrics, watchKeys, watchService);
                }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                watchKeys.remove(watchKey);
            }
        }

        if(!isEventDetected) {
            updateOldestFileAge(fileMetrics);
        }
        fileMetricsProcessor.printMetrics(fileMetrics);
    }

    private void handleFileDeletion(File childPath) {
        for (Map.Entry<String, FileMetric> entry : fileMetrics.entrySet()) {
            if (!childPath.exists() && entry.getKey().contains(childPath.getName())) {
                FileMetric fileMetric = entry.getValue();
                fileMetric.setAvailable(false);
                fileMetric.setFileSize("0");
                fileMetric.setModified(true);
                if(fileMetric.getNumberOfLines() != -1) {
                    fileMetric.setNumberOfLines(0);
                }
                else {
                    fileMetric.setNumberOfFiles(0);
                    fileMetric.setRecursiveNumberOfFiles(0);
                }
            }
        }
    }

    private void updateOldestFileAge(Map<String, FileMetric> fileMetrics) {
        for(Map.Entry<String, FileMetric> entry : fileMetrics.entrySet()) {
            if(entry.getValue().getOldestFileAge() != -1) {
                FileMetric fileMetric = entry.getValue();
                long currentOldestFileAge = fileMetric.getOldestFileAge();
                long offset = (System.currentTimeMillis() - currentOldestFileAge)/1000;
                fileMetric.setOldestFileAge(currentOldestFileAge + offset);
            }
        }
    }
}