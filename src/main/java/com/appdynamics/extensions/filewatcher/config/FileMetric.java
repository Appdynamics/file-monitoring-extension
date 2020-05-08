/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.config;

/*
 * @author Aditya Jagtiani
 */

public class FileMetric {
    private boolean isModified;
    private String fileSize;
    private String recursiveFileSize;
    private int numberOfFiles;
    private long oldestFileAge;
    private long numberOfLines;
    private long lastModifiedTime;
    private boolean isAvailable;
    private long recursiveNumberOfFiles;

    public long getRecursiveNumberOfFiles() { return recursiveNumberOfFiles; }
    public void setRecursiveNumberOfFiles(long recursiveNumberOfFiles) { this.recursiveNumberOfFiles = recursiveNumberOfFiles; }

    public long getNumberOfLines() {
        return numberOfLines;
    }
    public void setNumberOfLines(long numberOfLines) {
        this.numberOfLines = numberOfLines;
    }

    public boolean getAvailable() {
        return isAvailable;
    }
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean getModified() {
        return isModified;
    }
    public void setModified(boolean isChanged) {
        this.isModified = isChanged;
    }

    public String getFileSize() {
        return fileSize;
    }
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }
    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }
    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public long getOldestFileAge() {
        return oldestFileAge;
    }
    public void setOldestFileAge(long oldestFileAge) {
        this.oldestFileAge = oldestFileAge;
    }

    public String getRecursiveFileSize() { return recursiveFileSize; }
    public void setRecursiveFileSize(String recursiveFileSize) { this.recursiveFileSize = recursiveFileSize; }
}
