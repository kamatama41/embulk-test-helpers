package com.kamatama41.embulk.test

import org.embulk.test.EmbulkPluginTest
import org.embulk.test.EmbulkTest
import org.embulk.test.LocalObjectOutputPlugin.Matcher.assertRecords
import org.embulk.test.record
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Kotlin version of [TestSimpleExecutorPlugin]
 */
@EmbulkTest(value = [SimpleExecutorPlugin::class], name = "simple_exec")
class TestSimpleExecutorPluginKt : EmbulkPluginTest() {
    @Test
    fun runWithSimpleExecutor() {
        // Read in-config from resources
        val execConfig = config().set("type", "simple_exec")

        runConfig("yaml/simple_executor_input.yml").execConfig(execConfig).run()

        // Check read records
        assertRecords(
                record("user1", 20),
                record("user2", 21)
        )
    }

    @Test
    fun getInjectedSystemConfig() {
        setSystemConfig(config().set("foo", "bar"))

        val plugin = getInstance(SimpleExecutorPlugin::class.java)

        val injectedConfig = plugin.systemConfig
        assertEquals(injectedConfig.get(String::class.java, "foo"), "bar")
    }

}
