package com.kamatama41.embulk.test

import org.embulk.spi.type.Types.*
import org.embulk.test.EmbulkPluginTest
import org.embulk.test.EmbulkTest
import org.embulk.test.LocalObjectOutputPlugin.Matcher.assertRecords
import org.embulk.test.LocalObjectOutputPlugin.Matcher.assertSchema
import org.junit.jupiter.api.Test

import org.embulk.test.json
import org.embulk.test.record
import org.embulk.test.set

/**
 * Kotlin version of [TestRenameFilterPlugin]
 */
@EmbulkTest
class TestRenameFilterPluginKt : EmbulkPluginTest() {

    @Test
    fun renameColumn() {
        // Construct filter-config
        val config = config().set(
                "type" to "rename",
                "columns" to config().set("age" to "renamed_age")
        )

        // Run Embulk
        runFilter(config, inConfigPath = "yaml/filter_input.yml")

        // Check schema definition
        assertSchema(
                "username" to STRING,
                "renamed_age" to LONG
        )

        // Check read records
        assertRecords(
                record("user1", 20),
                record("user2", 21)
        )
    }

    @Test fun renameJsonColumn() {
        val config = config().set(
                "type" to "rename",
                "columns" to config().set("record" to "user_info")
        )

        runFilter(config, inConfigPath = "yaml/filter_json_input.yml")

        assertSchema("user_info" to JSON)

        assertRecords(
                record(json("username" to "user1", "age" to 20)),
                record(json("username" to "user2", "age" to 21))
        )
    }
}
