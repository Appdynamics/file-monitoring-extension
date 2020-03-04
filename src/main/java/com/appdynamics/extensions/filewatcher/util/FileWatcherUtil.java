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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileWatcherUtil {
    //TODO check if the map needs to be cleared at the end of each job/task
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileWatcherUtil.class);

    public static List<PathToProcess> getPathsToProcess(List<Map<String, ?>> configuredPaths) {
        List<PathToProcess> pathsToProcess = Lists.newArrayList();
        for(Map<String, ?> path: configuredPaths) {
            pathsToProcess.add(new PathToProcess() {{
                setDisplayName((String) path.get("displayName"));
                setPath((String) path.get("path"));
                setIgnoreHiddenFiles(Boolean.valueOf(path.get("ignoreHiddenFiles").toString()));
                setEnableRecursiveFileCounts(Boolean.valueOf(path.get("recursiveFileCounts").toString()));
                setEnableRecursiveFileSizes(Boolean.valueOf(path.get("recursiveFileSizes").toString()));
                setExcludeSubdirectoryCount(Boolean.valueOf(path.get("excludeSubdirectoriesFromFileCount").toString()));
            }});
        }
        return pathsToProcess;
    }

    public static String getFormattedDisplayName(String fileDisplayName, Path path, String baseDir){
        if(!baseDir.endsWith("/") || !baseDir.endsWith("\\")) {
            if(baseDir.contains("/")) {
                baseDir += "/";
            }
            else {
                baseDir += "\\";
            }
        }
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

    public static int getNumberOfLinesFromFile(Path file) throws IOException {
        try (Stream<String> fileStream = Files.lines(file)) {
            return (int) fileStream.count();
        }
    }

/*    public static int calculateRecursiveFileCount(File path, boolean ignoreHiddenFiles) {
        int count = 0;
            for (File file : path.listFiles()) {
                if (file.isFile()) {
                    count++;
                }
                if (file.isDirectory()) {
                    count += calculateRecursiveFileCount(file, ignoreHiddenFiles);
                }
            }

        return count;
    }*/

/*    public static long calculateRecursiveFileCount(Path path, boolean ignoreHiddenFiles) throws Exception {
        Predicate<Path> isValidFile;
        Predicate<Path> isValidDirectory;

        if(ignoreHiddenFiles) {

        }


        return Files.walk(path)
                .parallel()
                .filter(p -> p.toFile().isFile())
                .count();
    }*/

    private static String evaluatePath(String pathFromConfig) {
        if(pathFromConfig.endsWith("/") || pathFromConfig.endsWith("'\'")) {
            LOGGER.info("Removing trailing slash so as to treat the given path as a subdirectory");
            return pathFromConfig.substring(0, pathFromConfig.length() - 1);
        }
        return pathFromConfig;
    }

    public static AppPathMatcher getPathMatcher(PathToProcess fileToProcess) {
        AppPathMatcher matcher = new GlobPathMatcher();
        matcher.setMatcher(fileToProcess);
        return matcher;
    }

    // Along the lines of org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS
    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.startsWith("Windows");
    }

    public boolean isWindowsNetworkDrive(File file) {
        if (!isWindows()) {
            return false;
        }

        // Make sure the file is absolute
        file = file.getAbsoluteFile();
        String path = file.getPath();
//        System.out.println("Checking [" + path + "]");

        // UNC paths are dangerous
        if (path.startsWith("//")
                || path.startsWith("\\\\")) {
            // We might want to check for \\localhost or \\127.0.0.1 which would be OK, too
            return true;
        }

        String driveLetter = path.substring(0, 1);
        String colon = path.substring(1, 2);
        if (!":".equals(colon)) {
            throw new IllegalArgumentException("Expected 'X:': " + path);
        }

        return isNetworkDrive(driveLetter);
    }

    /** Use the command <code>net</code> to determine what this drive is.
     * <code>net use</code> will return an error for anything which isn't a share.
     *
     *  <p>Another option would be <code>fsinfo</code> but my gut feeling is that
     *  <code>net</code> should be available and on the path on every installation
     *  of Windows.
     */
    private boolean isNetworkDrive(String driveLetter) {
        List<String> cmd = Arrays.asList("cmd", "/c", "net", "use", driveLetter + ":");

        try {
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

            p.getOutputStream().close();

            StringBuilder consoleOutput = new StringBuilder();

            String line;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while ((line = in.readLine()) != null) {
                    consoleOutput.append(line).append("\r\n");
                }
            }

            int rc = p.waitFor();
//            System.out.println(consoleOutput);
//            System.out.println("rc=" + rc);
            return rc == 0;
        } catch(Exception e) {
            throw new IllegalStateException("Unable to run 'net use' on " + driveLetter, e);
        }
    }
}
