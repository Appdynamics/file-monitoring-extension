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
import com.appdynamics.extensions.filewatcher.util.Constants;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.PerMinValueCalculator;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.*;

public class CustomFileWalker extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(CustomFileWalker.class);

    private GlobPathMatcher globPathMatcher;
    private PathToProcess pathToProcess;
    private Map<String, FileMetric> fileMetrics;
    private String baseDirectory;

    private static PerMinValueCalculator perMinValueCalculator = new PerMinValueCalculator();

    public CustomFileWalker(String baseDirectory, GlobPathMatcher globPathMatcher, PathToProcess pathToProcess,
                            Map<String, FileMetric> fileMetrics) {
        this.baseDirectory = baseDirectory;
        this.globPathMatcher = globPathMatcher;
        this.pathToProcess = pathToProcess;
        this.fileMetrics = fileMetrics;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) {
        LOGGER.trace("CustomFileWalker - preVisitDirectory :: previsit directory path "+path);
        if (pathToProcess.getIgnoreHiddenFiles() && path.toFile().isHidden()) {
            LOGGER.debug("Skipping directory {}. Ignore hidden files = true & the path to this directory is hidden.",
                    path.getFileName());
            return FileVisitResult.CONTINUE;
        }
        if (globPathMatcher.getMatcher().matches(path)) {
            LOGGER.info("Match found for entered path {}. Checking access to directory..", path.getFileName());
            if (isDirectoryAccessible(path)) {
                LOGGER.info("Path {} accessible. Visiting directory..", path.getFileName());
                String metricSuffix = getFormattedDisplayName(pathToProcess.getDisplayName(), path, baseDirectory);
                fileMetrics.put(metricSuffix,
                        generateDirectoryMetrics(path, basicFileAttributes, metricSuffix));
                LOGGER.info("Directory metrics collected for {}.", path);

            } else {
                LOGGER.error("Directory {} is inaccessible. Assign read & execute permissions to directory to proceed.",
                        path);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    private FileMetric generateDirectoryMetrics(Path path, BasicFileAttributes basicFileAttributes,
                                                String metricSuffix) {
        LOGGER.info("Generating directory metrics for {}", path);
        FileMetric fileMetric = new FileMetric();
        setBasicAttributes(path, basicFileAttributes, fileMetric);

        BigDecimal prevTs = perMinValueCalculator.getPerMinuteValue(metricSuffix+ Constants.METRIC_SEPARATOR + Constants.MODIFIED,new BigDecimal(fileMetric.getLastModifiedTime()));
        if(prevTs != null && (prevTs.compareTo(BigDecimal.ZERO) > 0)){
            fileMetric.setModified(true);
        }

        setOtherDirectoryAttributes(path, fileMetric);
        LOGGER.info("For directory {}, Size = {}, File Count = {} & Oldest File Age = {} ms", path.getFileName(),
                fileMetric.getFileSize(), fileMetric.getNumberOfFiles(), fileMetric.getOldestFileAge());
        return fileMetric;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        LOGGER.trace("CustomFileWalker - visitFile :: visitFile path "+path);
        if (pathToProcess.getIgnoreHiddenFiles() && path.toFile().isHidden()) {
            LOGGER.debug("Skipping file {} as it is hidden ", path.getFileName());
            return FileVisitResult.CONTINUE;
        }

        if (globPathMatcher.getMatcher().matches(path)) {
            LOGGER.info("Match found for entered path {}. Checking access to file..", path.getFileName());
            if (isFileAccessible(path)) {
                LOGGER.info("Path {} accessible. Visiting file..", path.getFileName());
                String metricSuffix = getFormattedDisplayName(pathToProcess.getDisplayName(), path, baseDirectory);
                fileMetrics.put(metricSuffix, generateFileMetrics(path, basicFileAttributes, metricSuffix));
                LOGGER.info("File metrics collected for {}.", path);
            } else {
                LOGGER.error("File {} is inaccessible. Assign read permissions to file for the machine agent user to " +
                        "proceed.", path);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @NotNull
    private FileMetric generateFileMetrics(Path path, BasicFileAttributes basicFileAttributes, String metricSuffix)
            throws IOException {
        LOGGER.debug("Generating File Metrics for {}", path.getFileName());
        FileMetric fileMetric = new FileMetric();
        setBasicAttributes(path, basicFileAttributes, fileMetric);

        BigDecimal prevTs = perMinValueCalculator.getPerMinuteValue(metricSuffix+ Constants.METRIC_SEPARATOR + Constants.MODIFIED,new BigDecimal(fileMetric.getLastModifiedTime()));
        if(prevTs != null && (prevTs.compareTo(BigDecimal.ZERO) > 0)){
            fileMetric.setModified(true);
        }

        fileMetric.setNumberOfFiles(-1);
        fileMetric.setOldestFileAge(-1);
        fileMetric.setRecursiveNumberOfFiles(-1);
        fileMetric.setRecursiveFileSize("-1");
        fileMetric.setAvailable(true);
        fileMetric.setNumberOfLines(getNumberOfLinesFromFile(path));
        LOGGER.info("For file {}, File Size = {} & Last Modified Time = {} ms, Number of Lines " +
                        "= {}", path.getFileName(), fileMetric.getFileSize(), fileMetric.getLastModifiedTime(),
                fileMetric.getNumberOfLines());
        return fileMetric;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc){
        LOGGER.trace("CustomFileWalker - visitFileFailed :: path "+path+" is failed to visit,Exception is ",exc);
        if(Files.isDirectory(path)){
            LOGGER.error("Error occurred while visiting directory at path "+path,exc);
        } else {
            LOGGER.warn("Ignoring file at path {} because it does not exist in the file system anymore.",path);
        }
        return FileVisitResult.CONTINUE;
    }

    private void setBasicAttributes(Path path, BasicFileAttributes basicFileAttributes, FileMetric fileMetric) {
        if (basicFileAttributes != null) {
            LOGGER.debug("Setting Basic Directory Attributes for {}", path.getFileName());
            fileMetric.setLastModifiedTime(basicFileAttributes.lastModifiedTime().toMillis());
            fileMetric.setFileSize(String.valueOf(basicFileAttributes.size()));
        } else {
            LOGGER.debug("Couldn't find basic file attributes for {}", path.getFileName());
        }
    }

    private void setOtherDirectoryAttributes(@NotNull Path path, FileMetric fileMetric) {
        LOGGER.debug("Setting other directory attributes for {}", path.getFileName());
        int fileCount = 0;
        long oldestFile = 0L;

        File[] filesInDir = path.toFile().listFiles();

        if (filesInDir != null && filesInDir.length > 0) {
            oldestFile = filesInDir[0].lastModified();
            for (File f : filesInDir) {
                if (pathToProcess.getIgnoreHiddenFiles()) {
                    if (!f.isHidden()) {
                        if (f.isFile() || !pathToProcess.getExcludeSubdirectoryCount()) {
                            fileCount++;
                        }
                        if (f.lastModified() < oldestFile) {
                            oldestFile = f.lastModified();
                        }
                    } else {
                        LOGGER.info("Skipping directory {} as it is hidden", f);
                    }
                } else {
                    fileCount++;
                    if (f.lastModified() < oldestFile) {
                        oldestFile = f.lastModified();
                    }
                }
            }
        }
        fileMetric.setNumberOfFiles(fileCount);
        long currentTimeInMillis = System.currentTimeMillis();
        long oldestFileAge = -1;
        if (oldestFile > 0 && oldestFile < currentTimeInMillis) {
            oldestFileAge = (currentTimeInMillis - oldestFile) / 1000;
        }
        fileMetric.setOldestFileAge(oldestFileAge);
        fileMetric.setAvailable(true);
        fileMetric.setNumberOfLines(-1);
        fileMetric.setRecursiveNumberOfFiles(pathToProcess.getEnableRecursiveFileCounts() ?
                evaluateRecursiveFileCounts(path) : -1);
//        fileMetric.setRecursiveFileSize(pathToProcess.getEnableRecursiveFileSizes() ?
//                String.valueOf(FileUtils.sizeOfDirectoryAsBigInteger(path.toFile())) : "-1");
        fileMetric.setRecursiveFileSize(evaluateRecursiveFileSize(path));
    }

    private long evaluateRecursiveFileCounts(Path path) {
        try {
            LOGGER.debug("Calculating recursive file count for {}", path);
            return calculateRecursiveFileCount(path, pathToProcess.getIgnoreHiddenFiles(),
                    pathToProcess.getExcludeSubdirectoryCount());
        } catch (Exception ex) {
            LOGGER.error("Error encountered while calculating recursive file count for directory {}",
                    path.getFileName(), ex);
        }
        return -1;
    }

    private String evaluateRecursiveFileSize(Path path){
        try{
            LOGGER.debug("Calculating recursive file size for {}", path);
            if(pathToProcess.getEnableRecursiveFileSizes()){
                return String.valueOf(FileUtils.sizeOfDirectoryAsBigInteger(path.toFile()));
            }
        }catch (Exception ex){
            LOGGER.error("Error encountered while calculating recursive File Size for directory {}",
                    path.getFileName(), ex);
        }
        return "-1";
    }
}