/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.filewatcher;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.appdynamics.extensions.filewatcher.pathmatcher.visitors.CustomGlobFileVisitor;
import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.pathmatcher.factory.PathMatcherFactory;
import com.appdynamics.extensions.filewatcher.pathmatcher.factory.PathMatcherFactory.PathMatcherTypes;
import com.appdynamics.extensions.filewatcher.pathmatcher.visitors.FilePathVisitor;

/**
 * Created by abhi.pandey on 9/17/14.
 */
public class FileProcessor {

	protected static final Logger logger = Logger.getLogger(FileProcessor.class.getName());

	public void processFilePath(Configuration conf,FileToProcess file, Map<String,FileMetric> fileMetricsMap) {

		GlobPathMatcher globPathMatcher = (GlobPathMatcher) PathMatcherFactory.getPathMatcher(PathMatcherTypes.GLOB, file, conf);
		try {
            List<String> baseDirectories = FilePathVisitor.getBaseDirectories(file);
            for(String baseDir : baseDirectories) {
                logger.debug("Currently processing base directory : " +baseDir);
                Files.walkFileTree(Paths.get(baseDir), new CustomGlobFileVisitor(file, globPathMatcher, fileMetricsMap, baseDir));
            }
		} catch (Exception e) {
			logger.error("Error in walking file to process path " + e);
		}
	}
}
