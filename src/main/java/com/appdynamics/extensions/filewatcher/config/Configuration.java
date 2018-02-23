/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.config;

import java.util.List;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;

/**
 * Created by abhi.pandey on 8/26/14.
 */
public class Configuration extends MonitorConfiguration{

    public Configuration(String defaultMetricPrefix, Runnable taskRunner, MetricWriteHelper metricWriter) {
		super(defaultMetricPrefix, taskRunner, metricWriter);

	}

	private List<FileToProcess> fileToProcess;
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
