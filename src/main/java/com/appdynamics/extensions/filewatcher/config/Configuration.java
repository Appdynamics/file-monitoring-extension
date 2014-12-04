package com.appdynamics.extensions.fileWatcher.config;

import java.util.List;

/**
 * Created by abhi.pandey on 8/26/14.
 */
public class Configuration {

    private List<FileToProcess> fileToProcess;

    private Boolean isFileCountRequired;
    private String metricPrefix;

    public Boolean getIsFileCountRequired() {
        return isFileCountRequired;
    }

    public void setIsFileCountRequired(Boolean isFileCountRequired) {
        this.isFileCountRequired = isFileCountRequired;
    }

    public List<FileToProcess> getFileToProcess() {
        return fileToProcess;
    }

    public void setFileToProcess(List<FileToProcess> fileToProcess) {
        this.fileToProcess = fileToProcess;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }
}
