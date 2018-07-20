package com.synopsys.integration.swip.common;

import java.io.File;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.test.tool.TestLogger;

public class SwipDownloadUtilityTest {
    @Test
    public void testAttemptingDownload() throws Exception {
        IntLogger intLogger = new TestLogger();
        File downloadTarget = new File("/Users/ekerwin/working/swip");

        SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        Optional<String> swipCliPath = swipDownloadUtility.getSwipCliExecutablePath();

        Assert.assertTrue(swipCliPath.isPresent());
        Assert.assertTrue(swipCliPath.get().length() > 0);
    }

}
