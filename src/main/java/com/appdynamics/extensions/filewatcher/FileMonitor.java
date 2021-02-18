/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;

import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.Constants.*;
import static com.appdynamics.extensions.util.AssertUtils.assertNotNull;

/*
 * @author Aditya Jagtiani
 */

public class FileMonitor extends ABaseMonitor {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileMonitor.class);
    private List<PathToProcess> pathsToProcess;

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        initMonitor();
        assertNotNull(pathsToProcess, "Please configure the paths to be processed in your config.yml");
        for (PathToProcess pathToProcess : pathsToProcess) {
            FileMonitorTask task = new FileMonitorTask(getContextConfiguration(),
                    tasksExecutionServiceProvider.getMetricWriteHelper(), pathToProcess);
            tasksExecutionServiceProvider.submit(pathToProcess.getDisplayName(), task);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(CONFIGURED_PATHS);
    }

    @Override
    protected void onConfigReload(File file) {
        initMonitor();
    }

    private void initMonitor() {
        try {
            pathsToProcess = getPathsToProcess(getServers());
        } catch (Exception ex) {
            LOGGER.error("Error encountered while getting paths to process from config", ex);
        }
    }

    private List<PathToProcess> getPathsToProcess(List<Map<String, ?>> configuredPaths) {
        List<PathToProcess> pathsToProcess = Lists.newArrayList();
        for (Map<String, ?> path : configuredPaths) {
            pathsToProcess.add(new PathToProcess() {{
                setDisplayName((String) path.get("displayName"));
                setPath((String) path.get("path"));
                setIgnoreHiddenFiles(Boolean.valueOf(path.get("ignoreHiddenFiles").toString()));
                setEnableRecursiveFileCounts(Boolean.valueOf(path.get("recursiveFileCounts").toString()));
                setExcludeSubdirectoryCount(Boolean.valueOf(path.get("excludeSubdirectoriesFromFileCount").toString()));
                setEnableRecursiveFileSizes(Boolean.valueOf(path.get("recursiveFileSizes").toString()));
            }});
        }
        return pathsToProcess;
    }

    @Override
    public void onComplete() {
        LOGGER.info("File Monitoring Jobs Completed");
    }
}