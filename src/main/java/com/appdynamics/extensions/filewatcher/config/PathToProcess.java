/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.config;

/*
 * @author Aditya Jagtiani
 */

public class PathToProcess {
    private String displayName;
    private String path;
    private boolean ignoreHiddenFiles;
    private boolean enableRecursiveFileCounts;
    private boolean enableRecursiveFileSizes;
    private boolean excludeSubdirectoryCount;

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

    public boolean getIgnoreHiddenFiles() {
        return ignoreHiddenFiles;
    }
    public void setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
        this.ignoreHiddenFiles = ignoreHiddenFiles;
    }

    public boolean getEnableRecursiveFileCounts() {
        return enableRecursiveFileCounts;
    }
    public void setEnableRecursiveFileCounts(boolean enableRecursiveFileCounts) {
        this.enableRecursiveFileCounts = enableRecursiveFileCounts;
    }

    public boolean getExcludeSubdirectoryCount() {
        return excludeSubdirectoryCount;
    }
    public void setExcludeSubdirectoryCount(boolean excludeSubdirectoryCount) {
        this.excludeSubdirectoryCount = excludeSubdirectoryCount;
    }

    public boolean getEnableRecursiveFileSizes() {
        return enableRecursiveFileSizes;
    }

    public void setEnableRecursiveFileSizes(boolean enableRecursiveFileSizes) {
        this.enableRecursiveFileSizes = enableRecursiveFileSizes;
    }
}