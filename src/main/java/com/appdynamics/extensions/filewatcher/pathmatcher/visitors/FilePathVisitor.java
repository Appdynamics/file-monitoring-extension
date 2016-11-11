package com.appdynamics.extensions.filewatcher.pathmatcher.visitors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.GlobPathMatcher;

public class FilePathVisitor {
	protected static final Logger logger = Logger.getLogger(FilePathVisitor.class.getName());

	public static Map<String,String> walkFilesByGlobMatcher(final FileToProcess file,final GlobPathMatcher matcher) throws IOException{
		Map<String,String> filesToProcessMap = new HashMap<String, String>();
		String baseDir = null;
		if(file.getPath().isEmpty()){
			logger.error("File path is empty, returning");
			return filesToProcessMap;
		}
		if(file.getPath().contains("*")){
			baseDir = file.getPath().substring(0, file.getPath().indexOf('*'));
		}
		else {
			baseDir = file.getPath().substring(0,file.getPath().lastIndexOf('/')+1);
		}
		if(baseDir.isEmpty()){
			baseDir = "/";
		}
		Files.walkFileTree(Paths.get(baseDir),new CustomGlobFileVisitor(file, matcher, filesToProcessMap, baseDir));
		return filesToProcessMap;

	}
}
