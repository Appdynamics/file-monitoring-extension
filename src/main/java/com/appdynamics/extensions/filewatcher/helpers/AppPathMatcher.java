/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.helpers;

/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.extensions.filewatcher.config.PathToProcess;

import java.nio.file.PathMatcher;

public abstract class AppPathMatcher {
	PathMatcher matcher;
	protected PathToProcess file;
	
	public PathMatcher getMatcher() {
		return matcher;
	}
	public abstract void setMatcher(PathToProcess fileToProcess);
}