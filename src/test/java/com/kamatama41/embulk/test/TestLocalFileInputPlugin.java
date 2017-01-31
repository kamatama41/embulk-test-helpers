package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.junit.Test;

import static org.embulk.test.ExtendedEmbulkTests.configFromResource;
import static org.embulk.test.Utils.record;
import static org.embulk.test.Utils.timestamp;
import static org.embulk.test.TestOutputPlugin.assertRecords;

public class TestLocalFileInputPlugin extends EmbulkPluginTest {

    @Test
    public void testInputPlugin() {
        // Read in-config from resources
        ConfigSource config = configFromResource("yaml/file_input.yml");

        // Run Embulk
        runInput(config);

        // Check read records
        assertRecords(
                record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23),  timestamp(2015, 1, 27), "embulk jruby"),
                record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2),   timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
        );
    }
}
