/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

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
