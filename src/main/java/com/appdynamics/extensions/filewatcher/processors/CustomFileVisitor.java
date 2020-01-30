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


import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

public class CustomFileVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(CustomFileVisitor.class);
    private WatchService watchService;
    private Map<WatchKey, Path> keys;
    private GlobPathMatcher globPathMatcher;
    private PathToProcess pathToProcess;

    public CustomFileVisitor(WatchService watchService, Map<WatchKey, Path> keys, GlobPathMatcher globPathMatcher,
                             PathToProcess pathToProcess) {
        this.watchService = watchService;
        this.keys = keys;
        this.globPathMatcher = globPathMatcher;
        this.pathToProcess = pathToProcess;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes basicFileAttributes) throws IOException {
        if(pathToProcess.isIgnoreHiddenFiles() && dir.toFile().isHidden()) {
            LOGGER.info("Skipping file {}. Ignore hidden files = true & the path to this file is hidden.", dir.getFileName());
            return FileVisitResult.CONTINUE;
        }
        WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        keys.put(key, dir);
        if(globPathMatcher.getMatcher().matches(dir)) {
            LOGGER.info("Match found for entered path {}", dir);

        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
        return FileVisitResult.CONTINUE;
    }
}
