package org.embulk.input;

import org.embulk.config.ConfigSource;
import org.embulk.test.MyEmbulkTests;
import org.embulk.test.MyTestingEmbulk;
import org.junit.Rule;
import org.junit.Test;

import static org.embulk.test.DateUtil.timestamp;
import static org.embulk.test.TestOutputPlugin.assertRecords;
import static org.embulk.test.Record.record;

public class TestLocalFileInputPlugin {
    @Rule
    public MyTestingEmbulk embulk = (MyTestingEmbulk) MyTestingEmbulk
            .builder()
            .build();

    @Test
    public void loadFile() throws Exception {
        ConfigSource config = MyEmbulkTests.configFromResource("yaml/file_input.yml");
        embulk.runInput(config);
        assertRecords(
                record(1, 32864, timestamp(2015, 1, 27, 19, 23, 49), timestamp(2015, 1, 27), "embulk"),
                record(2, 14824, timestamp(2015, 1, 27, 19, 1, 23), timestamp(2015, 1, 27), "embulk jruby"),
                record(3, 27559, timestamp(2015, 1, 28, 2, 20, 2), timestamp(2015, 1, 28), "Embulk \"csv\" parser plugin"),
                record(4, 11270, timestamp(2015, 1, 29, 11, 54, 36), timestamp(2015, 1, 29), "NULL")
        );
    }
}
