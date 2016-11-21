package com.appdynamics.extensions.filewatcher;

import java.util.Map;

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
			 FilePathVisitor.walkFilesByGlobMatcher(file, globPathMatcher,fileMetricsMap);
		} catch (Exception e) {
			logger.error("Error in walking file to process path " + e);
		}	
	
	}


}
