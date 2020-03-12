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
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileWatcherUtil {

    public static List<PathToProcess> getPathsToProcess(List<Map<String, ?>> configuredPaths) {
        List<PathToProcess> pathsToProcess = Lists.newArrayList();
        for (Map<String, ?> path : configuredPaths) {
            pathsToProcess.add(new PathToProcess() {{
                setDisplayName((String) path.get("displayName"));
                setPath((String) path.get("path"));
                setIgnoreHiddenFiles(Boolean.valueOf(path.get("ignoreHiddenFiles").toString()));
                setEnableRecursiveFileCounts(Boolean.valueOf(path.get("recursiveFileCounts").toString()));
                setExcludeSubdirectoryCount(Boolean.valueOf(path.get("excludeSubdirectoriesFromFileCount").toString()));
                setEnableRecursiveFileSizes(Boolean.valueOf(path.get("recursiveFileSizes").toString()));
            }});
        }
        return pathsToProcess;
    }

    public static String getFormattedDisplayName(String fileDisplayName, Path path, String baseDir) {
        if (!baseDir.endsWith("/") || !baseDir.endsWith("\\")) {
            if (baseDir.contains("/")) {
                baseDir += "/";
            } else {
                baseDir += "\\";
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(fileDisplayName);
        String suffix = path.toString().replace(baseDir.substring(0, baseDir.length() - 1), "")
                .replace(File.separator, "|");
        if (!suffix.startsWith("|")) {
            builder.append('|');
            builder.append(suffix);
        } else {
            builder.append(suffix);
        }
        return builder.toString();
    }

    public static int getNumberOfLinesFromFile(Path file) throws IOException {
        if (file.toFile().exists()) {
            try (Stream<String> fileStream = Files.lines(file)) {
                return (int) fileStream.count();
            }
        }
        return 0;
    }

    public static long calculateRecursiveFileCount(Path path, boolean ignoreHiddenFiles,
                                                   boolean excludeSubdirectoriesFromFileCounts) throws IOException {
        if (ignoreHiddenFiles) {
            if (!excludeSubdirectoriesFromFileCounts) {
                return Files.walk(path)
                        .parallel()
                        .filter(p -> (p.toFile().isFile()
                                || p.toFile().isDirectory())
                                && !p.toFile().isHidden())
                        .count() - 1;
            }
            return Files.walk(path)
                    .parallel()
                    .filter(p -> !p.toFile().isDirectory()
                            && !p.toFile().isHidden())
                    .count();
        } else {
            if (!excludeSubdirectoriesFromFileCounts) {
                return Files.walk(path)
                        .parallel()
                        .filter(p -> (p.toFile().isFile()
                                || p.toFile().isDirectory()))
                        .count() - 1;
            }
            return Files.walk(path)
                    .parallel()
                    .filter(p -> !p.toFile().isDirectory())
                    .count();
        }
    }

    public static AppPathMatcher getPathMatcher(PathToProcess fileToProcess) {
        AppPathMatcher matcher = new GlobPathMatcher();
        matcher.setMatcher(fileToProcess);
        return matcher;
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.startsWith("Windows");
    }

    public static boolean isWindowsNetworkPath(String path) {
        if (!isWindows()) {
            return false;
        }

        File file = new File(path);

        if (!file.exists()) {
            return false;
        }

        path = file.getAbsolutePath();

        if (path.startsWith("//")
                || path.startsWith("\\\\")) {
            return true;
        }

        String driveLetter = path.substring(0, 1);
        String colon = path.substring(1, 2);
        if (!":".equals(colon)) {
            throw new IllegalArgumentException("Expected 'X:': " + path);
        }
        return isNetworkPath(driveLetter);
    }

    private static boolean isNetworkPath(String driveLetter) {
        List<String> cmd = Arrays.asList("cmd", "/c", "net", "use", driveLetter + ":");
        try {
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            p.getOutputStream().close();
            int rc = p.waitFor();
            return rc == 0;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to run 'net use' on " + driveLetter, e);
        }
    }

    public static boolean isNetworkPathAccessible(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory() && file.canRead() && file.canWrite();
    }
}