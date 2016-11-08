package com.appdynamics.extensions.filewatcher.config;

/**
 * Created by abhi.pandey on 8/27/14.
 */
public class FileToProcess {

    private String displayName;
    private String path;
    private Boolean isDirectoryDetailsRequired;

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

	public Boolean getIsDirectoryDetailsRequired() {
		return isDirectoryDetailsRequired;
	}

	public void setIsDirectoryDetailsRequired(Boolean isDirectoryDetailsRequired) {
		this.isDirectoryDetailsRequired = isDirectoryDetailsRequired;
	}



}
