package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.test.EmbulkPluginTest;
import org.embulk.test.EmbulkTest;
import org.junit.jupiter.api.Test;

import static org.embulk.test.LocalObjectOutputPlugin.assertRecords;
import static org.embulk.test.LocalObjectOutputPlugin.assertSchema;
import static org.embulk.test.Utils.column;
import static org.embulk.spi.type.Types.*;
import static org.embulk.test.Utils.json;
import static org.embulk.test.Utils.record;

@EmbulkTest
public class TestRenameFilterPlugin extends EmbulkPluginTest {

    @Test
    public void renameColumn() {
        // Construct filter-config
        ConfigSource config = config()
                .set("type", "rename")
                .set("columns", config()
                        .set("age", "renamed_age")
                );

        // Run Embulk
        runConfig("yaml/filter_input.yml").filterConfig(config).run();

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
        ConfigSource config = config()
                .set("type", "rename")
                .set("columns", config()
                        .set("record", "user_info")
                );

        runConfig("yaml/filter_json_input.yml").filterConfig(config).run();

        assertSchema(
                column("user_info", JSON)
        );

        assertRecords(
                record(json("{\"username\": \"user1\", \"age\": 20}")),
                record(json("{\"username\": \"user2\", \"age\": 21}"))
        );
    }
}
