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
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FilePathProcessorTest {

    @Test
    public void getBaseDirectoryWithoutWildcard() {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setPath("src/test/resources/TestFiles");

        FilePathProcessor filePathProcessor = new FilePathProcessor();
        List<String> baseDirectories = filePathProcessor.getBaseDirectories(pathToProcess);

        Assert.assertEquals(1, baseDirectories.size());
        Assert.assertTrue(baseDirectories.contains("src/test/resources/"));
    }

    @Test
    public void getBaseDirectoryWithWildcards() {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setPath("src/test/resources/Test*/**");
        FilePathProcessor filePathProcessor = new FilePathProcessor();
        List<String> baseDirectories = filePathProcessor.getBaseDirectories(pathToProcess);
        Assert.assertEquals(1, baseDirectories.size());
    }

    @Test
    public void getMultipleBaseDirectoriesWithWildcards() {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setPath("src/test/resources/TestFiles/dir*/*.*");
        FilePathProcessor filePathProcessor = new FilePathProcessor();
        List<String> baseDirectories = filePathProcessor.getBaseDirectories(pathToProcess);
        Assert.assertEquals(2, baseDirectories.size());
    }
}