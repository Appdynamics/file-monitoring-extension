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
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;

import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.Constants.*;
import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.getPathsToProcess;
import static com.appdynamics.extensions.util.AssertUtils.assertNotNull;

public class FileWatcher extends ABaseMonitor {

	private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileWatcher.class);
    private List<PathToProcess> pathsToProcess;
    private WatchService watcher;

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
		for(PathToProcess pathToProcess : pathsToProcess) {
		    FileWatcherTask task = new FileWatcherTask(getContextConfiguration(),
                    tasksExecutionServiceProvider.getMetricWriteHelper(), pathToProcess);
		    tasksExecutionServiceProvider.submit(pathToProcess.getDisplayName(), task);
        }
	}

	@Override
	protected List<Map<String, ?>> getServers() {
		return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(CONFIGURED_PATHS);
	}

	@Override
	protected void initializeMoreStuff(Map<String, String> args) {
		initMonitor();
	}

	@Override
	protected void onConfigReload(File file) {
		initMonitor();
	}

	/**
	 * Initialize and validate the CatEndpoints once after a machine agent restart or config reload and reuse for
	 * every run
	 */
	private void initMonitor() {
	    try {
            pathsToProcess = getPathsToProcess((List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(CONFIGURED_PATHS));
            if(watcher == null) {
                watcher = FileSystems.getDefault().newWatchService();
            }
        }
        catch (Exception ex) {
	        LOGGER.error("Error encountered while registering directories with the WatchService", ex);
        }
	}

	public static void main(String[] args) throws TaskExecutionException {
		ConsoleAppender ca = new ConsoleAppender();
		ca.setWriter(new OutputStreamWriter(System.out));
		ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
		ca.setThreshold(Level.DEBUG);
		org.apache.log4j.Logger.getRootLogger().addAppender(ca);

		FileWatcher fileWatcher = new FileWatcher();
		Map<String, String> argsMap = new HashMap<String, String>();
		argsMap.put("config-file", "/Users/aj89/repos/appdynamics/extensions/AppDynamics-File-Watcher-Extension/src/main/resources/conf/config.yml");

		fileWatcher.execute(argsMap, null);
	}
}