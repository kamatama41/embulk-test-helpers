package com.kamatama41.embulk.test

import org.embulk.test.EmbulkPluginTest
import org.embulk.test.EmbulkTest

import org.embulk.test.TestOutputPlugin.Matcher.assertRecords
import org.embulk.test.record
import org.junit.jupiter.api.Test

/**
 * Kotlin version of [TestLongIncrementFilterPlugin]
 */
@EmbulkTest(value = [LongIncrementFilterPlugin::class])
class TestLongIncrementFilterPluginKt : EmbulkPluginTest() {

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
