package org.embulk.input;

import org.embulk.config.ConfigSource;
import org.embulk.test.ExtendedEmbulkTests;
import org.embulk.test.ExtendedTestingEmbulk;
import org.junit.Rule;
import org.junit.Test;

import static org.embulk.test.Utils.record;
import static org.embulk.test.Utils.timestamp;
import static org.embulk.test.TestOutputPlugin.assertRecords;

public class TestLocalFileInputPlugin {
    @Rule
    public ExtendedTestingEmbulk embulk = (ExtendedTestingEmbulk) ExtendedTestingEmbulk
            .builder()
            .build();

    @Test
    public void loadFile() {
        ConfigSource config = ExtendedEmbulkTests.configFromResource("yaml/file_input.yml");
        embulk.runInput(config);
        assertRecords(
                record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23), timestamp(2015, 1, 27), "embulk jruby"),
                record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2), timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
        );
    }
}
