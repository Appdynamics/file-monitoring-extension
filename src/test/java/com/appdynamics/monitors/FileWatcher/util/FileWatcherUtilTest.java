/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.FileWatcher.util;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileWatcherUtilTest {

    private Path path;

    @Before
    public void before() {
        path = Paths.get("src/test/resources/TestFiles/dir2");
    }

    @Test
    public void calculateRecursiveFileCountWithoutHiddenFilesIncludeSubDirectories() throws Exception {
        Assert.assertEquals(5, FileWatcherUtil.calculateRecursiveFileCount(path,
                true, false));
    }

    @Test
    public void calculateRecursiveFileCountWithoutHiddenFilesExcludeSubdirectories() throws Exception {
        Assert.assertEquals(4, FileWatcherUtil.calculateRecursiveFileCount(path, true,
                true));
    }

    @Test
    public void calculateRecursiveFileCountWithHiddenFilesIncludeSubdirectories() throws Exception {
        Assert.assertEquals(7, FileWatcherUtil.calculateRecursiveFileCount(path,
                false, false));
    }

    @Test
    public void calculateRecursiveFileCountWithHiddenFilesExcludeSubdirectories() throws Exception {
        Assert.assertEquals(6, FileWatcherUtil.calculateRecursiveFileCount(path,
                false, true));
    }
}
