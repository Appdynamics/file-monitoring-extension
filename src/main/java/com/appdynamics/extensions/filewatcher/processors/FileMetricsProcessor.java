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
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.Constants.*;

public class FileMetricsProcessor {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileMetricsProcessor.class);
    private String metricPrefix;
    private Map<String, Map<String, ?>> metricsFromConfig;
    private List<Metric> metrics;

    public FileMetricsProcessor(String metricPrefix, Map<String, Map<String, ?>> metricsFromConfig) {
        this.metricPrefix = metricPrefix.trim();
        this.metricsFromConfig = metricsFromConfig;
        metrics = Lists.newCopyOnWriteArrayList();
    }

    List<Metric> getMetricList(Map<String, FileMetric> fileMetrics) {
        for(Map.Entry<String, FileMetric> entry : fileMetrics.entrySet()) {
            if(metricsFromConfig.containsKey(FILE_COUNT)) {
                addToMetricList(FILE_COUNT, entry.getValue().getNumberOfFiles(), entry.getKey());
            }
            if(metricsFromConfig.containsKey(FILE_SIZE)) {
                addToMetricList(FILE_SIZE, entry.getValue().getFileSize(), entry.getKey());
            }
            if(metricsFromConfig.containsKey(OLDEST_FILE_AGE)) {
                addToMetricList(OLDEST_FILE_AGE, entry.getValue().getOldestFileAge(), entry.getKey());
            }
            if(metricsFromConfig.containsKey(NUMBER_OF_LINES)) {
                addToMetricList(NUMBER_OF_LINES, entry.getValue().getNumberOfLines(), entry.getKey());
            }
            if(metricsFromConfig.containsKey(AVAILABLE)) {
                addToMetricList(AVAILABLE, entry.getValue().getAvailable(), entry.getKey());
            }
            if(metricsFromConfig.containsKey(CHANGED)) {
                addToMetricList(CHANGED, entry.getValue().getChanged(), entry.getKey());
            }
            if(metricsFromConfig.containsKey(LAST_MODIFIED_TIME)) {
                addToMetricList(LAST_MODIFIED_TIME, entry.getValue().getLastModifiedTime(), entry.getKey());
            }
        }
        return metrics;
    }

    private void addToMetricList(String name, Object value, String path) {
        Map<String, ?> metricProps = metricsFromConfig.get(name);
        if (!value.equals(-1)) {
            Metric metric = new Metric(name, String.valueOf(value), metricPrefix + METRIC_SEPARATOR +
                    path + METRIC_SEPARATOR + metricProps.get("alias"), metricProps);
            metrics.add(metric);
            LOGGER.info("Added metric {} to the queue for publishing", metric.getMetricPath());
        }
    }
}
