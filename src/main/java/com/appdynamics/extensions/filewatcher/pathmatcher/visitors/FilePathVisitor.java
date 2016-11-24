package com.appdynamics.extensions.filewatcher.pathmatcher.visitors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.FileMetric;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.GlobPathMatcher;

public class FilePathVisitor {
	protected static final Logger logger = Logger.getLogger(FilePathVisitor.class.getName());

	public static void  walkFilesByGlobMatcher(final FileToProcess file,final GlobPathMatcher matcher,Map<String,FileMetric> fileMetricsMap) throws IOException{
		String baseDir = null;
		String filePath = file.getPath().replace("\\\\", "\\"); //For Windows regexes
		if(filePath.isEmpty()){
			logger.error("File path is empty, returning");
			return;
		}
		if(filePath.contains("*")){
			String tempPath = filePath.substring(0, filePath.indexOf("*"));
			baseDir = tempPath.substring(0, FilenameUtils.indexOfLastSeparator(tempPath) + 1);
		}
		else{
			baseDir = filePath.substring(0, FilenameUtils.indexOfLastSeparator(filePath) + 1);
		}
		if(baseDir.isEmpty()){
			baseDir = File.separator;
		}
		logger.debug("Base dir initialised for " + file.getPath().toString() + " to " + baseDir);
		Files.walkFileTree(Paths.get(baseDir),new CustomGlobFileVisitor(file, matcher,fileMetricsMap,baseDir));


	}
}
