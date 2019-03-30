package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.embulk.test.EmbulkTest;
import org.junit.jupiter.api.Test;

import static org.embulk.test.LocalObjectOutputPlugin.assertRecords;
import static org.embulk.test.Utils.configFromResource;
import static org.embulk.test.Utils.record;

@EmbulkTest(value = SimpleExecutorPlugin.class, name = "simple_exec")
class TestSimpleExecutorPlugin extends EmbulkPluginTest {
    @Test
    void runWithSimpleExecutor() {
        // Read in-config from resources
        ConfigSource inConfig = configFromResource("yaml/simple_executor_input.yml");
        ConfigSource execConfig = config().set("type", "simple_exec");

        runExec(inConfig, execConfig);

        // Check read records
        assertRecords(
                record("user1", 20),
                record("user2", 21)
        );
    }
}
