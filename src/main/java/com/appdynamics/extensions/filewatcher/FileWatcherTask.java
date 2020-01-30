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
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.processors.CustomFileVisitor;
import com.appdynamics.extensions.filewatcher.processors.FilePathProcessor;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcherTask implements AMonitorTaskRunnable {

	private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileWatcherTask.class);
	private MonitorContextConfiguration monitorContextConfiguration;
	private MetricWriteHelper metricWriteHelper;
	private PathToProcess pathToProcess;
    private Map<WatchKey, Path> keys;
    private WatchService watchService;

	FileWatcherTask(MonitorContextConfiguration monitorContextConfiguration,
                           MetricWriteHelper metricWriteHelper, PathToProcess pathToProcess) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.pathToProcess = pathToProcess;
        this.keys = new HashMap<>();
    }

	@Override
	public void run() {
        String baseDirectory = new FilePathProcessor().getBaseDirectories(pathToProcess).get(0);
        try {
            executeTask(baseDirectory);
        }
        catch (Exception ex) {
            LOGGER.error("Task failed for directory {}", baseDirectory, ex);
        }
	}

	private void executeTask(String baseDirectory) throws IOException {
	    try(WatchService watchService = FileSystems.getDefault().newWatchService()) {
	        watch(watchService, Paths.get(baseDirectory));
        }
    }

	private void watch(WatchService watchService, Path start) {
	    LOGGER.info("Now watching directory: {}", start.getFileName());
	    try {
            registerTree(watchService, start);
            watchTree();

        }
        catch (Exception e) {
	        LOGGER.error("Error encountered while registering directory: {}", start, e);
        }
    }

    private void watchTree() throws InterruptedException, IOException {
	    while(true) {
	        WatchKey watchKey = watchService.take();
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                Path eventPath = (Path) watchEvent.context();

                Path directory = keys.get(watchKey);
                // Path directory = (Path) key.watchable(); //problems with renames
                Path child = directory.resolve(eventPath);

                if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(child)) {
                    registerTree(watchService, child);
                }

                System.out.printf("%s:%s\n", child, kind);
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                keys.remove(watchKey);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void registerTree(WatchService watchService, Path start) throws IOException {
	    LOGGER.info("Attempting to register directory {}", start.getFileName());
        GlobPathMatcher globPathMatcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
	    Files.walkFileTree(start, new CustomFileVisitor(watchService, keys, globPathMatcher, pathToProcess));
    }

	@Override
	public void onTaskComplete() {
		LOGGER.info("Finished collecting metrics for {}", pathToProcess.getDisplayName());
	}
}
