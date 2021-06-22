package com.appdynamics.monitors.FileWatcher.processors;

import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.processors.CustomFileWalker;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PrepareForTest({FileWatcherUtil.class, CustomFileWalker.class})
@RunWith(PowerMockRunner.class)
public class WalkMethodTest {

    @Test
    public void testWalkFunctionForFullyRecursive() throws IOException {

        Map<String, FileMetric> fileMetricMap = new HashMap<>();

        String baseDirPath = "src/test/resources/TestFiles/";

        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Fully Recursive");
        pathToProcess.setPath("src/test/resources/TestFiles/**");
        pathToProcess.setIgnoreHiddenFiles(true);
        pathToProcess.setExcludeSubdirectoryCount(true);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setEnableRecursiveFileSizes(false);

        FileWatcherUtil.walk(baseDirPath,pathToProcess,fileMetricMap);

        Assert.assertEquals(9,fileMetricMap.size());

        List<String> expectedList = expectedMetricListFullyRecursive();

        for(String expectedPath: expectedList){
            Assert.assertTrue(fileMetricMap.containsKey(expectedPath));
        }

    }

    public List<String> expectedMetricListFullyRecursive(){
        List<String> metricListFullyRecursive = Lists.newLinkedList();
        metricListFullyRecursive.add("Fully Recursive|dir1");
        metricListFullyRecursive.add("Fully Recursive|dir1|D1F1.txt");
        metricListFullyRecursive.add("Fully Recursive|dir2");
        metricListFullyRecursive.add("Fully Recursive|dir2|dir3");
        metricListFullyRecursive.add("Fully Recursive|dir2|dir3|D3F1.txt");
        metricListFullyRecursive.add("Fully Recursive|dir2|dir3|D3F2.txt");
        metricListFullyRecursive.add("Fully Recursive|dir2|D2F1.txt");
        metricListFullyRecursive.add("Fully Recursive|dir2|D2F2.txt");
        metricListFullyRecursive.add("Fully Recursive|TF1.txt");

        return metricListFullyRecursive;
    }

    @Test
    public void testWalkFunctionForSingleLevel() throws IOException {

        Map<String, FileMetric> fileMetricMap = new HashMap<>();

        String baseDirPath = "src/test/resources/TestFiles/";

        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Single Level");
        pathToProcess.setPath("src/test/resources/TestFiles/*");
        pathToProcess.setIgnoreHiddenFiles(true);
        pathToProcess.setExcludeSubdirectoryCount(true);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setEnableRecursiveFileSizes(false);

        FileWatcherUtil.walk(baseDirPath,pathToProcess,fileMetricMap);

        Assert.assertEquals(3,fileMetricMap.size());

        List<String> expectedList = expectedMetricListSingleLevel();

        for(String expectedPath: expectedList){
            Assert.assertTrue(fileMetricMap.containsKey(expectedPath));
        }

    }

    public List<String> expectedMetricListSingleLevel(){
        List<String> metricListSingleLevel = Lists.newLinkedList();
        metricListSingleLevel.add("Single Level|dir1");
        metricListSingleLevel.add("Single Level|dir2");
        metricListSingleLevel.add("Single Level|TF1.txt");

        return metricListSingleLevel;
    }

    @Test
    public void testWalkFunctionForSpecificDirectory() throws IOException {

        Map<String, FileMetric> fileMetricMap = new HashMap<>();

        String baseDirPath = "src/test/resources/TestFiles/";

        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Specific Directory");
        pathToProcess.setPath("src/test/resources/TestFiles/dir2");
        pathToProcess.setIgnoreHiddenFiles(true);
        pathToProcess.setExcludeSubdirectoryCount(true);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setEnableRecursiveFileSizes(false);

        FileWatcherUtil.walk(baseDirPath,pathToProcess,fileMetricMap);

        Assert.assertEquals(1,fileMetricMap.size());
        Assert.assertTrue(fileMetricMap.containsKey("Specific Directory|dir2"));

    }

    @Test
    public void testWalkFunctionForSpecificFile() throws IOException {

        Map<String, FileMetric> fileMetricMap = new HashMap<>();

        String baseDirPath = "src/test/resources/TestFiles/";

        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Specific File");
        pathToProcess.setPath("src/test/resources/TestFiles/TF1.txt");
        pathToProcess.setIgnoreHiddenFiles(true);
        pathToProcess.setExcludeSubdirectoryCount(true);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setEnableRecursiveFileSizes(false);

        FileWatcherUtil.walk(baseDirPath,pathToProcess,fileMetricMap);

        Assert.assertEquals(1,fileMetricMap.size());
        Assert.assertTrue(fileMetricMap.containsKey("Specific File|TF1.txt"));
        Assert.assertEquals(259,(fileMetricMap.get("Specific File|TF1.txt")).getNumberOfLines());
    }

}
