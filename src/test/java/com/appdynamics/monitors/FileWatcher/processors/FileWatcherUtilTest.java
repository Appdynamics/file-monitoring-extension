/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.FileWatcher.processors;
/*
 * @author Aditya Jagtiani
 */


import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.processors.FilePathProcessor;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileWatcherUtilTest {

    @Test
    public void getFormattedDisplayNameLinuxTest() {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing Linux");
        pathToProcess.setPath("/A/B/C/*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/C/D.txt");

        List<String> baseDirectories = new FilePathProcessor().getBaseDirectories(pathToProcess);

        for (String baseDirectory : baseDirectories) {
            String formattedDisplayName = FileWatcherUtil.getFormattedDisplayName(pathToProcess.getDisplayName(), a, baseDirectory);
            Assert.assertEquals("Testing Linux|D.txt", formattedDisplayName);
        }
    }

    @Test
    public void getFormattedDisplayNameWindowsTest() {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing Windows");
        pathToProcess.setPath("A\\\\B\\\\C\\\\*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\B\\C\\D.txt");

        List<String> baseDirectories = new FilePathProcessor().getBaseDirectories(pathToProcess);

        for (String baseDirectory : baseDirectories) {
            String formattedDisplayName = FileWatcherUtil.getFormattedDisplayName(pathToProcess.getDisplayName(), a, baseDirectory);
            Assert.assertEquals("Testing Windows|D.txt", formattedDisplayName);
        }
    }

    @Test
    public void calculateRecursiveFileCountsTest() throws Exception {
        Path path = Paths.get("src/test/resources/TestFiles/dir2");
        Assert.assertEquals(5, FileWatcherUtil.calculateRecursiveFileCount(path,
                true, false));
        Assert.assertEquals(4, FileWatcherUtil.calculateRecursiveFileCount(path, true,
                true));
    }

    @Test
    public void getNumberOfLinesFromFileTestFileExists() throws IOException {
        Path path = Paths.get("src/test/resources/TestFiles/TF1.txt");
        Assert.assertEquals(259, FileWatcherUtil.getNumberOfLinesFromFile(path));
    }

    @Test
    public void getNumberOfLinesFromFileTestFileDoesNotExist() throws IOException {
        Path path = Paths.get("src/test/resources/TestFiles/Nonsense.txt");
        Assert.assertEquals(0, FileWatcherUtil.getNumberOfLinesFromFile(path));
    }

}