/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.filewatcher.pathmatcher.visitors;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.FileMetric;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.GlobPathMatcher;

public class FilePathVisitor {
	protected static final Logger logger = Logger.getLogger(FilePathVisitor.class.getName());
    public static List<String> getBaseDirectories(final FileToProcess file) throws IOException{
        List<String> baseDirectories = Lists.newArrayList();
        String filePath = file.getPath().replace("\\\\", "\\"); //For Windows regexes
        if(filePath.isEmpty()){
            logger.error("File path is empty, returning");
            return baseDirectories;
        }
        if(filePath.contains("*")){
            String tempPath = filePath.substring(0, filePath.indexOf("*"));
            String currentBaseDir = tempPath.substring(0, FilenameUtils.indexOfLastSeparator(tempPath) + 1);
            if(filePath.substring(filePath.indexOf("*"), filePath.length()).contains("\\")
                    || filePath.substring(filePath.indexOf("*"), filePath.length()).contains("/")) {
                String dirPattern = obtainDirWildCard(filePath, FilenameUtils.indexOfLastSeparator(tempPath) + 1);
                baseDirectories.addAll(getRequiredDirectories(currentBaseDir, dirPattern));
            }
            else {
                baseDirectories.add(tempPath.substring(0, FilenameUtils.indexOfLastSeparator(tempPath) + 1));
            }
        }
        else{
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
