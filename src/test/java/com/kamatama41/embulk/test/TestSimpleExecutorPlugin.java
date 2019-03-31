package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.embulk.test.EmbulkTest;
import org.junit.jupiter.api.Test;

import static org.embulk.test.LocalObjectOutputPlugin.assertRecords;
import static org.embulk.test.Utils.record;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EmbulkTest(value = SimpleExecutorPlugin.class, name = "simple_exec")
class TestSimpleExecutorPlugin extends EmbulkPluginTest {
    @Test
    void runWithSimpleExecutor() {
        // Read in-config from resources
        ConfigSource execConfig = config().set("type", "simple_exec");

        runConfig("yaml/simple_executor_input.yml").execConfig(execConfig).run();

        // Check read records
        assertRecords(
                record("user1", 20),
                record("user2", 21)
        );
    }

    @Test
    void getInjectedSystemConfig() {
        setSystemConfig(config().set("foo", "bar"));

        SimpleExecutorPlugin plugin = getInstance(SimpleExecutorPlugin.class);

        ConfigSource injectedConfig = plugin.getSystemConfig();
        assertEquals(injectedConfig.get(String.class, "foo"), "bar");
    }
}
