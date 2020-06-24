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
import com.appdynamics.extensions.filewatcher.processors.CustomFileWalker;
import com.appdynamics.extensions.filewatcher.processors.FileMetricsProcessor;
import com.appdynamics.extensions.filewatcher.processors.FileWatcher;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

public class FileWatcherTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherTest.class);
    private final String testFilePath = "src/test/resources/TestFiles/TF2.txt";
    private Map<String, FileMetric> fileMetrics;
    private FileWatcher fileWatcher;

    @Before
    public void setup() throws IOException {
        FileMetricsProcessor fileMetricsProcessor = Mockito.mock(FileMetricsProcessor.class);

        WatchService watchService = FileSystems.getDefault().newWatchService();
        String baseDirectory = "src/test/resources/TestFiles/";
        Path path = Paths.get(baseDirectory);
        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.OVERFLOW);
        Map<WatchKey, Path> watchKeys = Maps.newHashMap();
        watchKeys.put(key, path);

        fileMetrics = Maps.newHashMap();

        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("src/test/resources/TestFiles/*");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);
        fileWatcher = new FileWatcher(watchService, watchKeys,
                baseDirectory, fileMetrics, pathToProcess, fileMetricsProcessor);
    }

    @After
    public void tearDown() {
        File file = new File(testFilePath);
        file.delete();
    }

    @Test
    public void processWatchEventsCreationTest() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                fileWatcher.processWatchEvents();
            } catch (Exception e) {
                LOGGER.error("Error encountered while testing watch events");
            }
        });
        t1.start();
        createNewFileInWatchedDirectory();
        Thread.sleep(30000);
        Assert.assertTrue(fileMetrics.containsKey("Testing|TF2.txt"));
        t1.interrupt();
    }

    @Test
    public void processWatchEventsModifyTest() throws InterruptedException {
        createNewFileInWatchedDirectory();
        Thread t1 = new Thread(() -> {
            try {
                fileWatcher.processWatchEvents();
            } catch (Exception e) {
                LOGGER.error("Error encountered while testing watch events");
            }
        });
        t1.start();
        modifyFileInWatchedDirectory();
        Thread.sleep(30000);
        String expectedMetricSuffix = "Testing|TF2.txt";
        Assert.assertTrue(fileMetrics.containsKey(expectedMetricSuffix));
        Assert.assertEquals(2, fileMetrics.get(expectedMetricSuffix).getNumberOfLines());
        t1.interrupt();
    }

    @Test
    public void processWatchEventsDeleteTest() throws InterruptedException {
        createNewFileInWatchedDirectory();
        Thread t1 = new Thread(() -> {
            try {
                fileWatcher.processWatchEvents();
            } catch (Exception e) {
                LOGGER.error("Error encountered while testing watch events");
            }
        });
        t1.start();
        deleteFileInWatchedDirectory();
        Thread.sleep(30000);
        Assert.assertTrue(!fileMetrics.containsKey("Testing|TF2.txt"));
        t1.interrupt();
    }

    private void createNewFileInWatchedDirectory() {
        try {
            File file = new File(testFilePath);
            if (!file.exists())
                file.createNewFile();
        } catch (IOException ex) {
            LOGGER.error("Error encountered while creating test file", ex);
        }
    }

    private void modifyFileInWatchedDirectory() {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(testFilePath);
            fileWriter.write("Line 1 \t \t This is line number 1 \n");
            fileWriter.write("Line 2 \t \t This is line number 2");
        } catch (IOException ex) {
            LOGGER.error("Error encountered while writing to test file", ex);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException ex) {
                LOGGER.error("Couldn't close test file writer", ex);
            }
        }
    }

    private void deleteFileInWatchedDirectory() {
        File file = new File(testFilePath);
        file.delete();
    }
}
