package com.kamatama41.embulk.test;

import com.kamatama41.embulk.plugin.MyFilterPlugin;
import org.embulk.config.ConfigSource;
import org.embulk.spi.FilterPlugin;
import org.embulk.test.EmbulkPluginTest;
import org.embulk.test.TestingEmbulk;
import org.junit.Test;

import static org.embulk.test.TestOutputPlugin.assertRecords;
import static org.embulk.test.Utils.record;

public class TestMyFilterPlugin extends EmbulkPluginTest {

    @Override
    protected void setup(TestingEmbulk.Builder builder) {
        // Register custom plugin
        builder.registerPlugin(FilterPlugin.class, "my_filter", MyFilterPlugin.class);
    }

    @Test
    public void incrementsJustLongColumns() {
        // Specify input data
        final String inConfigPath = "yaml/myfilter_input.yml";

        // Construct filter-config
        ConfigSource config = newConfig().set("type", "my_filter");

        // Run Embulk
        runFilter(config, inConfigPath);

        // Check read records
        assertRecords(
                record("user1", 21),
                record("user2", 22)
        );
    }
}
