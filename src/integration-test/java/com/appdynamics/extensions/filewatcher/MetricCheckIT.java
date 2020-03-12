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

import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.appdynamics.extensions.filewatcher.IntegrationTestUtils.initializeMetricAPIService;

public class MetricCheckIT {

    private MetricAPIService metricAPIService;

    @Before
    public void setup() {
        metricAPIService = initializeMetricAPIService();
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
}