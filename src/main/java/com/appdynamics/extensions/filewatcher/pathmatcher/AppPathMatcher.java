/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

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
