package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.embulk.test.TestingEmbulk;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.embulk.test.ExtendedEmbulkTests.configFromResource;
import static org.embulk.test.Utils.record;
import static org.embulk.test.Utils.timestamp;
import static org.embulk.test.TestOutputPlugin.assertRecords;

@RunWith(Enclosed.class)
public class TestLocalFileInputPlugin {

    public static class DefaultCase extends EmbulkPluginTest {
        @Test
        public void readFile() {
            // Read in-config from resources
            ConfigSource config = configFromResource("yaml/file_input.yml");

            // Run Embulk
            runInput(config);

            // Check read records
            assertRecords(
                    record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                    record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23), timestamp(2015, 1, 27), "embulk jruby"),
                    record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2), timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                    record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
            );
        }
    }

    public static class ConfDiffCase extends EmbulkPluginTest {
        private static final String LINE_SEPARATOR = System.getProperty("line.separator");
        private static final Path TMP_FILE = Paths.get("./src/test/resources/input/file_input_tmp.csv");

        @After
        public void cleanup() throws IOException {
            Files.delete(TMP_FILE);
        }

        @Test
        public void testConfDiff() throws IOException {
            // Read in-config from resources
            ConfigSource config = configFromResource("yaml/file_input_confdiff.yml");

            // Run Embulk
            TestingEmbulk.RunResult runResult = runInput(config);

            // Will read only file_input.csv
            assertRecords(
                    record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                    record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23), timestamp(2015, 1, 27), "embulk jruby"),
                    record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2), timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                    record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
            );

            // Run again with conf diff and new file
            createFile();
            runInput(config, runResult.getConfigDiff());

            // Will read only new file
            assertRecords(
                    record(5, 45505, timestamp(2017, 1, 31, 10, 21, 2), timestamp(2017, 1, 31), "test")
            );
        }

        private void createFile() throws IOException {
            String msg = ""
                    + "id,account,time,purchase,comment" + LINE_SEPARATOR
                    + "5,45505,2017-01-31 10:21:02,20170131,test";
            Files.write(TMP_FILE, msg.getBytes());
        }
    }
}
