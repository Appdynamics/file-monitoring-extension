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
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;

import java.nio.file.WatchService;
import java.util.List;

public class FileMetricsProcessor {

    public FileMetricsProcessor(WatchService watchService, List<PathToProcess> pathsToProcess) {}

    public List<Metric> getFileMetrics() {
        List<Metric> fileMetrics = Lists.newArrayList();
        return null;
    }
}
