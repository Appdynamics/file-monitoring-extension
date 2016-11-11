package com.appdynamics.extensions.filewatcher.pathmatcher;

import java.nio.file.PathMatcher;

import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;

public abstract class AppPathMatcher {
	
	protected PathMatcher matcher;

	protected FileToProcess file;
	
	public PathMatcher getMatcher() {
		return matcher;
	}

	public abstract void setMatcher(FileToProcess fileToProcess,Configuration conf);

}
