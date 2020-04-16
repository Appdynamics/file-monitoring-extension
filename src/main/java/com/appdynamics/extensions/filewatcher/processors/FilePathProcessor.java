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
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class FilePathProcessor {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FilePathProcessor.class);
    private List<String> baseDirectories = Lists.newArrayList();

    public List<String> getBaseDirectories(PathToProcess pathToProcess) {
        try {
            return processFilePath(pathToProcess);
        } catch (Exception ex) {
            LOGGER.error("Error encountered while evaluating base directories for path {}", pathToProcess.getDisplayName(), ex);
        }
        return Lists.newArrayList();
    }

    private List<String> processFilePath(final PathToProcess file) {
        String filePath = file.getPath().replace("\\\\", "\\"); //For Windows regexes
        if (filePath.isEmpty()) {
            LOGGER.error("File path is empty for {}, returning", file.getDisplayName());
            return baseDirectories;
        }
        if (filePath.contains("*")) {
            String tempPath = filePath.substring(0, filePath.indexOf("*"));
            String currentBaseDir = tempPath.substring(0, FilenameUtils.indexOfLastSeparator(tempPath) + 1);
            if (filePath.substring(filePath.indexOf("*")).contains("\\")
                    || filePath.substring(filePath.indexOf("*")).contains("/")) {
                String dirPattern = obtainDirWildCard(filePath, FilenameUtils.indexOfLastSeparator(tempPath) + 1);
                baseDirectories.addAll(getRequiredDirectories(currentBaseDir, dirPattern));
            } else {
                baseDirectories.add(tempPath.substring(0, FilenameUtils.indexOfLastSeparator(tempPath) + 1));
            }
        } else {
            baseDirectories.add(filePath.substring(0, FilenameUtils.indexOfLastSeparator(filePath) + 1));
        }
        return baseDirectories;
    }

    private static List<String> getRequiredDirectories(String baseDir, String wildcard) {
        List<String> baseDirectories = Lists.newArrayList();
        File directory = new File(baseDir);
        if (directory.isDirectory()) {
            FileFilter fileFilter = new WildcardFileFilter(wildcard);
            File[] files = directory.listFiles(fileFilter);
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        baseDirectories.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return baseDirectories;
    }

    private static String obtainDirWildCard(String filePath, int start) {
        return filePath.substring(start, filePath.indexOf("/", start));
    }
}