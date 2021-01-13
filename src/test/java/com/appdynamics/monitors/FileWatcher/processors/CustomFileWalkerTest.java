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

import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.processors.CustomFileWalker;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.getFormattedDisplayName;


@PrepareForTest({FileWatcherUtil.class, CustomFileWalker.class})
@RunWith(PowerMockRunner.class)
public class CustomFileWalkerTest {

    private CustomFileWalker classUnderTest;

    @Before
    public void setup() {
        PowerMockito.spy(FileWatcherUtil.class);
    }

    @Test
    public void visitDirectoryNonRecursively() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("/A/B/C");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/C/D.TXT");
        Path b = Paths.get("/A/B/C");

        List<Path> paths = Arrays.asList(a, b);

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        classUnderTest = new CustomFileWalker("/A/B/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/C")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }

        Assert.assertEquals(1, fileMetrics.size());
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/A/B/")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/A/B/")));
    }

    @Test
    public void visitDirectoryAndContentsNonRecursively() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("/A/B/C/*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/C/D.TXT");
        Path b = Paths.get("/A/B/C/E");
        Path c = Paths.get("/A/B/C/E/F.txt");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("/A/B/C/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/C/E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/A/B/C")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/A/B/C")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "/A/B/C")));
    }

    @Test
    public void visitDirectoryAndContentsRecursively() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("/A/B/C/**");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/C/D.TXT");
        Path b = Paths.get("/A/B/C/E");
        Path c = Paths.get("/A/B/C/E/F.txt");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("/A/B/C/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/C/E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(3, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/A/B/C/")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/A/B/C/")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "/A/B/C/")));
    }

    @Test
    public void visitFileWithinADirectory() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("/A/B/C/D.txt");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/C/D.txt");
        Path b = Paths.get("/A/B/C/E");
        Path c = Paths.get("/A/B/C/F.txt");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("/A/B/C/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/C/E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(1, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/A/B/C/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/A/B/C/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "/A/B/C/")));
    }

    @Test
    public void visitFilesWithRegexesWithinADirectory() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("/A/B/C/*.*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/C/D.txt");
        Path b = Paths.get("/A/B/C/E");
        Path c = Paths.get("/A/B/C/F.txt");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("/A/B/C/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/C/E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/A/B/C/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/A/B/C/")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "/A/B/C/")));
    }


    @Test
    public void visitFilesWithRegexesWithinADirectoryWithIntermediateRegexes() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("/A/B/C*/*.*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/Cat/D.txt");
        Path b = Paths.get("/A/B/Cat/E");
        Path c = Paths.get("/A/B/Cat/E/F.txt");
        Path d = Paths.get("/A/B/Cat/G.txt");
        List<Path> paths = Arrays.asList(a, b, c, d);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("/A/B/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/Cat/E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/A/B/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/A/B/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "/A/B/")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), d, "/A/B/")));
    }

    @Test
    public void visitFilesWithRegexesWithinADirectoryWithStartingRegex() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("*/A/B/C/*.*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/C/D.txt");
        Path b = Paths.get("/A/B/C/E");
        Path c = Paths.get("/A/B/C/E/F.txt");
        Path d = Paths.get("/A/B/C/G.txt");
        List<Path> paths = Arrays.asList(a, b, c, d);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/C/E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "/")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), d, "/")));
    }

    @Test
    public void visitFilesWithRegexesWithinADirectoryWithIntermediateAndStartingRegexes() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("*/A/B/C*/*.*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("/A/B/Cat/D.txt");
        Path b = Paths.get("/A/B/Cat/E");
        Path c = Paths.get("/A/B/Cat/E/F.txt");
        Path d = Paths.get("/A/B/Cat/G.txt");
        List<Path> paths = Arrays.asList(a, b, c, d);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("/", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("/A/B/Cat/E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "/")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "/")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), d, "/")));
    }
}