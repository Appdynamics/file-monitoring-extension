/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.util;
/*
 * @author Aditya Jagtiani
 */


import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.AppPathMatcher;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class FileWatcherUtil {
    //TODO check if the map needs to be cleared at the end of each job/task
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileWatcherUtil.class);

    public static List<PathToProcess> getPathsToProcess(List<Map<String, ?>> configuredPaths) {
        List<PathToProcess> pathsToProcess = Lists.newArrayList();
        for(Map<String, ?> path: configuredPaths) {
            pathsToProcess.add(new PathToProcess() {{
                setDisplayName((String) path.get("displayName"));
                setPath((String) path.get("path"));
                setIgnoreHiddenFiles((Boolean) path.get("ignoreHiddenFiles"));
                setEnableRecursiveFileCounts((Boolean) path.get("recursiveFileCounts"));
                setEnableRecursiveFileSizes((Boolean) path.get("recursiveFileSizes"));
                setExcludeSubdirectoryCount((Boolean) path.get("excludeSubdirectoryCount"));
            }});
        }
        return pathsToProcess;
    }

    public static String getFormattedDisplayName(String fileDisplayName, Path path, String baseDir){
        StringBuilder builder = new StringBuilder();
        builder.append(fileDisplayName);
        String suffix = path.toString().replace(baseDir.substring(0, baseDir.length()-1), "")
                .replace(File.separator, "|");
        if(!suffix.startsWith("|")){
            builder.append('|');
            builder.append(suffix);
        }
        else{
            builder.append(suffix);
        }
        return builder.toString();
    }

    public static AppPathMatcher getPathMatcher(PathToProcess fileToProcess) {
        AppPathMatcher matcher = new GlobPathMatcher();
        matcher.setMatcher(fileToProcess);
        return matcher;
    }
}
