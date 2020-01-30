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
    private boolean isChanged;
    private String fileSize;
    private String timeStamp;
    private int numberOfFiles;
    private long oldestFileAge;

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    private boolean isAvailable;

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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
}