package com.kamatama41.embulk.plugin

import org.embulk.test.EmbulkPluginTest
import org.junit.Test

import org.embulk.test.TestOutputPlugin.Matcher.assertRecords
import org.embulk.test.record
import org.embulk.test.registerPlugin
import org.junit.Before

/**
 * Kotlin version of [TestLongIncrementFilterPlugin]
 */
class TestLongIncrementFilterPluginKt : EmbulkPluginTest() {

    @Before
    fun setup() {
        builder.registerPlugin(LongIncrementFilterPlugin::class)
    }

    @Test
    fun incrementsJustLongColumns() {
        // Construct filter-config
        val config = config().set("type", "long_increment")

        // Run Embulk
        runFilter(config, inConfigPath = "yaml/long_increment_input.yml")

        // Check read records
        assertRecords(
                record("user1", 21),
                record("user2", 22)
        )
    }
}
