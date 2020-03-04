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

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            watch();
        } catch (InterruptedException | IOException ex) {
            LOGGER.error("Error encountered while walking File {}", baseDirectory, ex);
        }
    }

    private void walk(String baseDirectory) throws IOException {
        GlobPathMatcher globPathMatcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Files.walkFileTree(Paths.get(baseDirectory), new CustomFileVisitor(baseDirectory,
                watchService, watchKeys, globPathMatcher, pathToProcess, fileMetrics));
        printMetrics();
    }

    private void printMetrics() {
        List<Metric> metrics = fileMetricsProcessor.getMetricList(fileMetrics);
        metricWriteHelper.transformAndPrintMetrics(metrics);
        metrics.clear();
    }

    private void watch() throws InterruptedException, IOException {
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

                        /*Path startingDirectoryForWatchEvent = Paths.get(childPath.substring(0,
                                FilenameUtils.indexOfLastSeparator(childPath) + 1));*/
                    String startingDirectoryForWatchEvent = childPath.substring(0,
                            FilenameUtils.indexOfLastSeparator(childPath) + 1);
                    resetCurrentFileMetrics(fileMetrics);
                    walk(baseDirectory);
                    printMetrics();
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

    private void resetCurrentFileMetrics(Map<String, FileMetric> fileMetrics) {
        fileMetrics.replaceAll((key, value) -> new FileMetric());
    }

}
// todo for reference, create a new thread per base directory. walk, print and watch in this thread. use the same watchservice and watchkeys for each TASK (path to process). Gn tc

