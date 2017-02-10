package com.kamatama41.embulk.plugin

import org.embulk.test.EmbulkPluginTest
import org.junit.Test

import org.embulk.test.TestOutputPlugin.Matcher.assertRecords
import org.embulk.test.listOf
import org.embulk.test.record

class TestMyFilterPluginKt : EmbulkPluginTest() {

    override fun plugins(): List<Class<*>>? {
        return listOf(MyFilterPlugin::class)
    }

    @Test
    fun incrementsJustLongColumns() {
        // Construct filter-config
        val config = config().set("type", "my")

        // Run Embulk
        runFilter(config, inConfigPath = "yaml/myfilter_input.yml")

        // Check read records
        assertRecords(
                record("user1", 21),
                record("user2", 22)
        )
    }
}
