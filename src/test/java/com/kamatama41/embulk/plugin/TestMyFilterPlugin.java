package com.kamatama41.embulk.plugin;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.junit.Test;

import java.util.List;

import static org.embulk.test.TestOutputPlugin.assertRecords;
import static org.embulk.test.Utils.listOf;
import static org.embulk.test.Utils.record;

public class TestMyFilterPlugin extends EmbulkPluginTest {

    @Override
    protected List<Class<?>> plugins() {
        return listOf(MyFilterPlugin.class);
    }

    @Test
    public void incrementsJustLongColumns() {
        // Specify input data
        final String inConfigPath = "yaml/myfilter_input.yml";

        // Construct filter-config
        ConfigSource config = config().set("type", "my");

        // Run Embulk
        runFilter(config, inConfigPath);

        // Check read records
        assertRecords(
                record("user1", 21),
                record("user2", 22)
        );
    }
}
