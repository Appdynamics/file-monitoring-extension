
/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.FileWatcher;

import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.visitors.FilePathVisitor;
import org.junit.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by aditya.jagtiani on 10/16/17.
 */

@Ignore
public class FilePathVisitorTest {
    @Test
    public void whenDirectoryHasAWildCardTest() throws IOException {
        FileToProcess fileToProcess = new FileToProcess();
        fileToProcess.setDisplayName("test1");
        fileToProcess.setIncludeDirectoryContents(true);
        fileToProcess.setIgnoreHiddenFiles(true);
        fileToProcess.setPath("src/test/resources/TestFiles/new*/*.*");
        List<String> baseDirs = FilePathVisitor.getBaseDirectories(fileToProcess);
        Assert.assertTrue(baseDirs.contains("/Users/aditya.jagtiani/repos/appdynamics/extensions/AppDynamics-File-Watcher-Extension/src/test/resources/TestFiles/new1"));
        Assert.assertTrue(baseDirs.contains("/Users/aditya.jagtiani/repos/appdynamics/extensions/AppDynamics-File-Watcher-Extension/src/test/resources/TestFiles/new2"));
        Assert.assertTrue(!baseDirs.contains("/Users/aditya.jagtiani/repos/appdynamics/extensions/AppDynamics-File-Watcher-Extension/src/test/resources/TestFiles/newFile.txt"));
    }
}

