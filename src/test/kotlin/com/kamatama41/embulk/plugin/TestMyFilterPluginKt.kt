package com.kamatama41.embulk.plugin

import org.embulk.spi.FilterPlugin
import org.embulk.test.EmbulkPluginTest
import org.embulk.test.TestingEmbulk
import org.junit.Test

import org.embulk.test.TestOutputPlugin.Matcher.assertRecords
import org.embulk.test.record
import org.embulk.test.registerPlugin

class TestMyFilterPluginKt : EmbulkPluginTest() {

    override fun setup(builder: TestingEmbulk.Builder?) {
        // Register custom plugin
        builder!!.registerPlugin(FilterPlugin::class, "my_filter", MyFilterPlugin::class)
    }

    @Test
    fun incrementsJustLongColumns() {
        // Construct filter-config
        val config = config().set("type", "my_filter")

        // Run Embulk
        runFilter(config, inConfigPath = "yaml/myfilter_input.yml")

        // Check read records
        assertRecords(
                record("user1", 21),
                record("user2", 22)
        )
    }
}
