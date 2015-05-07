package com.appdynamics.extensions.filewatcher.config;

import java.util.List;

/**
 * Created by abhi.pandey on 8/26/14.
 */
public class Configuration {

    private List<FileToProcess> fileToProcess;
    private Boolean isDirectoryDetailsRequired;
    private Boolean isFileCountRequired;
    private Boolean ignoreHiddenFiles;
    private Boolean isOldestFileAgeMetricRequired;
    private String metricPrefix;

    public List<FileToProcess> getFileToProcess() {
        return fileToProcess;
    }

    public void setFileToProcess(List<FileToProcess> fileToProcess) {
        this.fileToProcess = fileToProcess;
    }

    public Boolean getIsDirectoryDetailsRequired() {
        return isDirectoryDetailsRequired;
    }

    public void setIsDirectoryDetailsRequired(Boolean isDirectoryDetailsRequired) {
        this.isDirectoryDetailsRequired = isDirectoryDetailsRequired;
    }

    public Boolean getIsFileCountRequired() {
        return isFileCountRequired;
    }

    public void setIsFileCountRequired(Boolean isFileCountRequired) {
        this.isFileCountRequired = isFileCountRequired;
    }

    public Boolean getIgnoreHiddenFiles() {
        return ignoreHiddenFiles;
    }

    public void setIgnoreHiddenFiles(Boolean ignoreHiddenFiles) {
        this.ignoreHiddenFiles = ignoreHiddenFiles;
    }

    public Boolean getIsOldestFileAgeMetricRequired() {
        return isOldestFileAgeMetricRequired;
    }

    public void setIsOldestFileAgeMetricRequired(Boolean isOldestFileAgeMetricRequired) {
        this.isOldestFileAgeMetricRequired = isOldestFileAgeMetricRequired;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }
}
