package com.kamatama41.embulk.test

import org.embulk.test.*

import org.embulk.test.LocalObjectOutputPlugin.Matcher.assertRecords
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Kotlin version of [TestLocalFileInputPlugin]
 */
class TestLocalFileInputPluginKt {

    @Nested @EmbulkTest
    inner class DefaultCase : EmbulkPluginTest() {
        @Test
        fun readFile() {
            // Run Embulk
            runConfig("yaml/file_input.yml").run()

            // Check read records
            assertRecords(
                    record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                    record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23), timestamp(2015, 1, 27), "embulk jruby"),
                    record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2), timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                    record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
            )
        }
    }

    @Nested @EmbulkTest
    inner class ConfDiffCase : EmbulkPluginTest() {
        @AfterEach fun cleanup() {
            tmpFile.delete()
        }

        @Test fun testConfDiff() {
            // Run Embulk
            val inConfigPath = "yaml/file_input_confdiff.yml"
            val runResult = runConfig(inConfigPath).run()

            // Will read only file_input.csv
            assertRecords(
                    record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                    record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23),  timestamp(2015, 1, 27), "embulk jruby"),
                    record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2),   timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                    record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
            )

            // Run again with conf diff and new file
            createFile()
            runConfig(inConfigPath).configDiff(runResult.configDiff).run()

            // Will read only new file
            assertRecords(
                    record(5, 45505, timestamp(2017, 1, 31, 10, 21, 2), timestamp(2017, 1, 31), "test")
            )
        }

        private fun createFile() {
            val msg = """
            |id,account,time,purchase,comment
            |5,45505,2017-01-31 10:21:02,20170131,test
            """.trimMargin()
            tmpFile.writeText(msg)
        }

        private val tmpFile = File("./src/test/resources/input/file_input_tmp.csv")
    }
}
