package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.junit.Test;

import static org.embulk.test.TestOutputPlugin.assertRecords;
import static org.embulk.test.TestOutputPlugin.assertSchema;
import static org.embulk.test.Utils.column;
import static org.embulk.spi.type.Types.*;
import static org.embulk.test.Utils.json;
import static org.embulk.test.Utils.record;

public class TestRenameFilterPlugin extends EmbulkPluginTest {

    @Test
    public void renameColumn() {
        // Specify input data
        final String inConfigPath = "yaml/filter_input.yml";

        // Construct filter-config
        ConfigSource config = config()
                .set("type", "rename")
                .set("columns", config()
                        .set("age", "renamed_age")
                );

        // Run Embulk
        runFilter(config, inConfigPath);

        // Check schema definition
        assertSchema(
                column("username", STRING),
                column("renamed_age", LONG)
        );

        // Check read records
        assertRecords(
                record("user1", 20),
                record("user2", 21)
        );
    }

    @Test
    public void renameJsonColumn() {
        final String inConfigPath = "yaml/filter_json_input.yml";

        ConfigSource config = config()
                .set("type", "rename")
                .set("columns", config()
                        .set("record", "user_info")
                );

        runFilter(config, inConfigPath);

        assertSchema(
                column("user_info", JSON)
        );

        assertRecords(
                record(json("{\"username\": \"user1\", \"age\": 20}")),
                record(json("{\"username\": \"user2\", \"age\": 21}"))
        );
    }
}
