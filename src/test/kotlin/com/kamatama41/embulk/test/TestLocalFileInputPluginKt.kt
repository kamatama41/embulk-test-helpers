package com.kamatama41.embulk.test

import org.embulk.test.EmbulkPluginTest
import org.junit.After
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

import org.embulk.test.TestOutputPlugin.Matcher.assertRecords
import org.embulk.test.configFromResource
import org.embulk.test.record
import org.embulk.test.timestamp
import java.io.File

@RunWith(Enclosed::class)
class TestLocalFileInputPluginKt {

    class DefaultCase : EmbulkPluginTest() {
        @Test fun readFile() {
            // Run Embulk
            runInput(inConfig = configFromResource("yaml/file_input.yml"))

            // Check read records
            assertRecords(
                    record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                    record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23), timestamp(2015, 1, 27), "embulk jruby"),
                    record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2), timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                    record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
            )
        }
    }

    class ConfDiffCase : EmbulkPluginTest() {
        @After fun cleanup() {
            TMP_FILE.delete()
        }

        @Test fun testConfDiff() {
            // Run Embulk
            val inConfig = configFromResource("yaml/file_input_confdiff.yml")
            val runResult = runInput(inConfig)

            // Will read only file_input.csv
            assertRecords(
                    record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                    record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23),  timestamp(2015, 1, 27), "embulk jruby"),
                    record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2),   timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                    record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
            )

            // Run again with conf diff and new file
            createFile()
            runInput(inConfig, runResult.configDiff)

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
            TMP_FILE.writeText(msg)
        }

        companion object {
            private val TMP_FILE = File("./src/test/resources/input/file_input_tmp.csv")
        }
    }
}
