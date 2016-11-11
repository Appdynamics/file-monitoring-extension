package com.appdynamics.extensions.filewatcher.pathmatcher.factory;

import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.AppPathMatcher;
import com.appdynamics.extensions.filewatcher.pathmatcher.GlobPathMatcher;

public class PathMatcherFactory {
	public enum PathMatcherTypes{ GLOB }

	public static AppPathMatcher getPathMatcher(PathMatcherTypes type,FileToProcess fileToProcess,Configuration conf){
		switch(type){
		case GLOB : {
			AppPathMatcher matcher = new GlobPathMatcher();
			matcher.setMatcher(fileToProcess, conf);
			return matcher;
		}
		default : return null;
		}
	}

}
