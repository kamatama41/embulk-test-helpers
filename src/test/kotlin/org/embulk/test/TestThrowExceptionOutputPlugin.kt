package org.embulk.test

import org.embulk.test.LocalObjectOutputPlugin.Matcher.assertRecords
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

@EmbulkTest
internal class TestThrowExceptionOutputPlugin: EmbulkPluginTest() {
    @Test
    fun testPassingConfigDiffOfSource() {
        val inConfig = configFromString("""
            type: config
            columns:
            - {name: username, type: string}
            values:
            - - [user1]
            - - [user2]
        """.trimIndent())

        val outConfig = configFromString("""
            type: throw_exception
            thrown_on: AFTER_TRANSACTION
            source:
              type: local_object
              incremental: true
              incremental_column: username
        """.trimIndent())


        val configDiff = configDiffFromString("""
            in: {last_path: tmp/sample_bk.json}
            out:
              source:
                last_record: {values: [user1]}
        """.trimIndent())

        try {
            runConfig(inConfig).outConfig(outConfig).configDiff(configDiff).run()
            fail<Void>("No exception")
        } catch (e: RuntimeException) {
            assertEquals("Failed on after transaction.", e.cause!!.message)
        }

        // Only [user2] should be loaded (running Embulk failed, though)
        assertRecords(record("user2"))
    }
}
