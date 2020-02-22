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
import com.appdynamics.extensions.executorservice.MonitorExecutorService;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.*;
import java.util.Map;

public class FileWatcher { //implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);
    private String baseDirectory;
    private Map<WatchKey, Path> watchKeys;
    private MonitorExecutorService executorService;
    private PathToProcess pathToProcess;
    private FileMetricsProcessor fileMetricsProcessor;
    private MetricWriteHelper metricWriteHelper;
    private WatchService watchService;

    public FileWatcher(String baseDirectory, Map<WatchKey, Path> watchKeys, MonitorExecutorService executorService,
                       PathToProcess pathToProcess, MetricWriteHelper metricWriteHelper,
                       FileMetricsProcessor fileMetricsProcessor, WatchService watchService) {
        this.baseDirectory = baseDirectory;
        this.watchKeys = watchKeys;
        this.pathToProcess = pathToProcess;
        this.executorService = executorService;
        this.metricWriteHelper = metricWriteHelper;
        this.fileMetricsProcessor = fileMetricsProcessor;
        this.watchService = watchService;
    }

    public void run() {
        try {
            watchDirectoryForEvents();
        }
        catch (Exception ex) {
            LOGGER.error("Error encountered while watching directory {}", baseDirectory, ex);
        }
    }

    private void watchDirectoryForEvents() throws InterruptedException {
        WatchKey watchKey;

        LOGGER.info("Watching directory {} for events", baseDirectory);
        while (true) {
            watchKey = watchService.take();
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                if ((kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_DELETE ||
                        kind == StandardWatchEventKinds.ENTRY_MODIFY)) {

                    Path eventPath = (Path) watchEvent.context();
                    Path directory = watchKeys.get(watchKey);
                    String childPath = directory.resolve(eventPath).toFile().getAbsolutePath();
                    LOGGER.info("Event {} detected for path {}", kind, childPath);

                    Path startingDirectoryForWatchEvent = Paths.get(childPath.substring(0,
                            FilenameUtils.indexOfLastSeparator(childPath) + 1));

                    executorService.execute("WatchService File Walker", new FileWalker(startingDirectoryForWatchEvent,
                            watchService, baseDirectory, pathToProcess, watchKeys, metricWriteHelper, fileMetricsProcessor));
                }
            }
            //  System.out.printf("%s:%s\n", child, kind);

            boolean valid = watchKey.reset();
            if (!valid) {
                watchKeys.remove(watchKey);
                if (watchKeys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
