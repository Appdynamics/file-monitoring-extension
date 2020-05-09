/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MetricCheckIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricCheckIT.class);

    private MetricAPIService metricAPIService;
    private CustomDashboardAPIService customDashboardAPIService;

    @Before
    public void setup() {
        metricAPIService = ITUtils.initializeMetricAPIService();
        customDashboardAPIService = ITUtils.initializeCustomDashboardAPIService();
    }

    @Test
    public void testMetricUpload() {
        if (metricAPIService != null) {
            JsonNode jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infrastructure%" +
                    "20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics" +
                    "%7CFile%20Watcher%7CTestFiles%7CAvailable&time-range-" +
                    "type=BEFORE_NOW&duration-in-mins=5");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
                int availability = (valueNode == null) ? 0 : valueNode.get(0).asInt();
                Assert.assertEquals(1, availability);
            }
        } else {
            Assert.fail("Failed to connect to the Controller API");
        }
    }

    @Test
    public void testWatchServiceUpdates() throws InterruptedException {
        if (metricAPIService != null) {
            int oldFileCount = 0;
            int newFileCount;
            JsonNode jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infrastructure%" +
                    "20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics" +
                    "%7CFile%20Watcher%7CTestFiles%7CFile%20Count&time-range-" +
                    "type=BEFORE_NOW&duration-in-mins=5");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
                oldFileCount = (valueNode == null) ? 0 : valueNode.get(0).asInt();
            }

            createNewFileInWatchedDirectory("/opt/appdynamics/machine-agent/conf/test.txt");

            Thread.sleep(180000);
            jsonNode = metricAPIService.getMetricData("", "Server%20&%20Infrastructure%" +
                    "20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics" +
                    "%7CFile%20Watcher%7CTestFiles%7CFile%20Count&time-range-" +
                    "type=BEFORE_NOW&duration-in-mins=5");
            if (jsonNode != null) {
                JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricValues", "*", "value");
                newFileCount = (valueNode == null) ? 0 : valueNode.get(0).asInt();
                Assert.assertEquals(oldFileCount + 1, newFileCount);
            } else {
                Assert.fail("Failed to connect to the Controller API");
            }
        }
    }

    private void createNewFileInWatchedDirectory(String testFilePath) {
        try {
            File file = new File(testFilePath);
            if (!file.exists())
                file.createNewFile();
        } catch (IOException ex) {
            LOGGER.error("Error encountered while creating test file", ex);
        }
    }

    @Test
    public void checkDashboardsUploaded() {
        boolean isDashboardPresent = false;
        if (customDashboardAPIService != null) {
            JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
            isDashboardPresent = ITUtils.isDashboardPresent("File Watcher Dashboard", allDashboardsNode);
            Assert.assertTrue(isDashboardPresent);
        } else {
            Assert.assertFalse(isDashboardPresent);
        }
    }
}