package com.kamatama41.embulk.test

import org.embulk.test.EmbulkPluginTest
import org.embulk.test.EmbulkTest
import org.embulk.test.LocalObjectOutputPlugin.Matcher.assertRecords
import org.embulk.test.configFromResource
import org.embulk.test.record
import org.junit.jupiter.api.Test

/**
 * Kotlin version of [TestSimpleExecutorPlugin]
 */
@EmbulkTest(value = [SimpleExecutorPlugin::class], name = "simple_exec")
class TestSimpleExecutorPluginKt : EmbulkPluginTest() {
    @Test
    fun runWithSimpleExecutor() {
        // Read in-config from resources
        val inConfig = configFromResource("yaml/simple_executor_input.yml")
        val execConfig = config().set("type", "simple_exec")

        runExec(inConfig, execConfig)

        // Check read records
        assertRecords(
                record("user1", 20),
                record("user2", 21)
        )
    }

}
