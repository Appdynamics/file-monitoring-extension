package com.appdynamics.extensions.filewatcher.pathmatcher.visitors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.GlobPathMatcher;

public class CustomGlobFileVisitor extends SimpleFileVisitor<Path>{
	
	protected static final Logger logger = Logger.getLogger(CustomGlobFileVisitor.class.getName());
	
	private FileToProcess file;
	
	private GlobPathMatcher matcher;
	
	private Map<String,String> filesToProcessMap;
	
	private String baseDir;
	
	public CustomGlobFileVisitor(FileToProcess file,GlobPathMatcher matcher,Map<String,String> filesToProcessMap,String baseDir){
		
		this.baseDir=baseDir;
		this.file=file;
		this.matcher=matcher;
		this.filesToProcessMap=filesToProcessMap;
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {

		if(file.getIgnoreHiddenFiles()  && path.toFile().isHidden()){
			return FileVisitResult.CONTINUE;
		}
		
		if(!file.getIsDirectoryDetailsRequired()){
			return FileVisitResult.CONTINUE;
		}

		if (matcher.getMatcher().matches(path)) {
			logger.debug("Found match for entered path " + path);
			if(!getFilesToProcessMap().containsKey(path.toString())){
				StringBuilder builder = new StringBuilder();
				builder.append(file.getDisplayName());
				builder.append(path.toString().replaceAll(baseDir.substring(0, baseDir.length()-1), "").replaceAll("/", "|"));
				getFilesToProcessMap().put(path.toString(), builder.toString());
			}
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException
	{
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
			throws IOException
	{
			if(file.getIgnoreHiddenFiles() && path.toFile().isHidden()){
				return FileVisitResult.CONTINUE;
			}
			if (matcher.getMatcher().matches(path)) {
				logger.debug("Found match for entered path " + path);
				if(!getFilesToProcessMap().containsKey(path.toString())){
					StringBuilder builder = new StringBuilder();
					builder.append(file.getDisplayName());
					builder.append(path.toString().replaceAll(baseDir.substring(0, baseDir.length()-1), "").replaceAll("/", "|"));
					getFilesToProcessMap().put(path.toString(), builder.toString());
				}
			}
		return FileVisitResult.CONTINUE;
	}

	public Map<String,String> getFilesToProcessMap() {
		return filesToProcessMap;
	}
}
