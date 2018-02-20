/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.filewatcher.config;

/**
 * Created by abhi.pandey on 8/27/14.
 */
public class FileToProcess {

    private String displayName;
    private String path;
    private Boolean includeDirectoryContents;
    private Boolean ignoreHiddenFiles;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

	public Boolean getIncludeDirectoryContents() {
		return includeDirectoryContents;
	}

	public void setIncludeDirectoryContents(Boolean includeDirectoryContents) {
		this.includeDirectoryContents = includeDirectoryContents;
	}

	public Boolean getIgnoreHiddenFiles() {
		return ignoreHiddenFiles;
	}

	public void setIgnoreHiddenFiles(Boolean ignoreHiddenFiles) {
		this.ignoreHiddenFiles = ignoreHiddenFiles;

	}
}
