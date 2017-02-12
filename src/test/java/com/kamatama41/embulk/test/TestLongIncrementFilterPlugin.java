package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.junit.Before;
import org.junit.Test;

import static org.embulk.test.TestOutputPlugin.assertRecords;
import static org.embulk.test.Utils.record;

public class TestLongIncrementFilterPlugin extends EmbulkPluginTest {

    @Before
    public void setup() {
        getBuilder().registerPlugin(LongIncrementFilterPlugin.class);
    }

    @Test
    public void incrementsJustLongColumns() {
        // Specify input data
        final String inConfigPath = "yaml/long_increment_input.yml";

        // Construct filter-config
        ConfigSource config = config().set("type", "long_increment");

        // Run Embulk
        runFilter(config, inConfigPath);

        // Check read records
        assertRecords(
                record("user1", 21),
                record("user2", 22)
        );
    }
}
