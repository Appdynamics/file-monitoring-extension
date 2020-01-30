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

    public boolean isIgnoreHiddenFiles() {
        return ignoreHiddenFiles;
    }

    public void setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
        this.ignoreHiddenFiles = ignoreHiddenFiles;
    }

    public boolean isEnableRecursiveFileSizes() {
        return enableRecursiveFileSizes;
    }

    public void setEnableRecursiveFileSizes(boolean enableRecursiveFileSizes) {
        this.enableRecursiveFileSizes = enableRecursiveFileSizes;
    }

    public boolean isEnableRecursiveFileCounts() {
        return enableRecursiveFileCounts;
    }

    public void setEnableRecursiveFileCounts(boolean enableRecursiveFileCounts) {
        this.enableRecursiveFileCounts = enableRecursiveFileCounts;
    }

    private String displayName;
    private String path;
    private boolean ignoreHiddenFiles;
    private boolean enableRecursiveFileSizes;
    private boolean enableRecursiveFileCounts;

    public boolean isExcludeSubdirectoryCount() {
        return excludeSubdirectoryCount;
    }

    public void setExcludeSubdirectoryCount(boolean excludeSubdirectoryCount) {
        this.excludeSubdirectoryCount = excludeSubdirectoryCount;
    }

    private boolean excludeSubdirectoryCount;
}
