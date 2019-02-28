package org.embulk.test

import org.embulk.test.TestOutputPlugin.Matcher.assertRecords
import org.junit.jupiter.api.Test

@EmbulkTest
internal class TestTestOutputPlugin: EmbulkPluginTest() {
    @Test
    fun runByDefault() {
        val inConfig = configFromResource("yaml/test_output_input.yml")
        val outConfig = outConfigBase()
        // Run Embulk
        runOutput(inConfig, outConfig)

        // Check read records
        assertRecords(
                record(1, "user2", 150.3),
                record(2, "user3", 150.1),
                record(3, "user1", 150.2)
        )
    }

    @Test
    fun runWithIncrementalColumn() {
        val inConfig = configFromResource("yaml/test_output_input.yml")
        val outConfig = outConfigBase()
                .set("incremental", true)
                .set("incremental_column", "id")
        val records = arrayOf(
                record(1, "user2", 150.3),
                record(2, "user3", 150.1),
                record(3, "user1", 150.2)
        )

        // Run Embulk
        var result = runOutput(inConfig, outConfig)

        // 3 records should be read
        assertRecords(*records)

        // Change incremental column to String
        outConfig.set("incremental_column", "username")
        // Run Embulk with ConfigDiff
        result = runOutput(inConfig, outConfig, result.configDiff)

        // 2 records (username > "user1") should be read
        assertRecords(records[0], records[1])

        // Change incremental column to Double
        outConfig.set("incremental_column", "height")
        // Run Embulk with ConfigDiff
        result = runOutput(inConfig, outConfig, result.configDiff)

        // 2 records (height > 150.1) should be read
        assertRecords(records[0], records[2])
    }

    private fun outConfigBase() = config().set("type", "test")
}
