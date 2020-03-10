package com.appdynamics.monitors.FileWatcher.processors;


import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.processors.CustomFileVisitor;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.getFormattedDisplayName;


/*
 * @author Aditya Jagtiani
 */

public class WindowsCustomFileVisitorTest {

    private CustomFileVisitor classUnderTest;


    @Test
    public void visitDirectoryNonRecursively() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("A\\\\B\\\\C");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\B\\C\\D.TXT");
        Path b = Paths.get("A\\B\\C");
        List<Path> paths = Arrays.asList(a, b);

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileVisitor("A\\B\\", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("A\\B\\C")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }

        Assert.assertEquals(1, fileMetrics.size());
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "A\\B\\")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "A\\B\\")));
    }

    @Test
    public void visitDirectoryAndContentsNonRecursively() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("..\\\\A\\\\B\\\\C\\\\*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("..\\A\\B\\C\\D.TXT");
        Path b = Paths.get("..\\A\\B\\C\\E");
        Path c = Paths.get("..\\A\\B\\C\\E\\F.txt");
        List<Path> paths = Arrays.asList(a, b, c);

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileVisitor("..\\A\\B\\C\\", matcher, pathToProcess, fileMetrics);

        for(Path p: paths) {
            if(p.toString().equals("..\\A\\B\\C\\E")){
                classUnderTest.preVisitDirectory(p, null);
            }
            else{
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "..\\A\\B\\C\\")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "..\\A\\B\\C\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c, "..\\A\\B\\C\\")));
    }

}
