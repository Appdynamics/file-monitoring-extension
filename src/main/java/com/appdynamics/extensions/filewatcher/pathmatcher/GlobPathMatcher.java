/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.filewatcher.pathmatcher;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;

import com.appdynamics.extensions.filewatcher.config.Configuration;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;

public class GlobPathMatcher extends AppPathMatcher{

	@Override
	public void setMatcher(FileToProcess fileToProcess,Configuration conf) {
		this.file = fileToProcess;
		if(this.file.getIgnoreHiddenFiles()==null){
			this.file.setIgnoreHiddenFiles(conf.getIgnoreHiddenFiles());
		}
		if(this.file.getIncludeDirectoryContents()==null){
			this.file.setIncludeDirectoryContents(true);
		}
		this.matcher=FileSystems.getDefault().getPathMatcher("glob:"+this.file.getPath());
	}


}
