package org.embulk.test;

import org.embulk.config.ConfigSource;
import org.junit.Rule;
import org.junit.Test;

import static org.embulk.test.TestOutputPlugin.assertRecords;
import static org.embulk.test.TestOutputPlugin.assertSchema;
import static org.embulk.test.Utils.column;
import static org.embulk.spi.type.Types.*;
import static org.embulk.test.Utils.json;
import static org.embulk.test.Utils.record;

public class TestRenameFilterPlugin {
    @Rule
    public ExtendedTestingEmbulk embulk = (ExtendedTestingEmbulk) ExtendedTestingEmbulk
            .builder()
            .build();

    @Test
    public void renameColumn() {
        final String inConfigPath = "yaml/filter_input.yml";

        ConfigSource config = embulk.newConfig()
                .set("type", "rename")
                .set("columns", embulk.newConfig()
                        .set("age", "renamed_age")
                );
        embulk.runFilter(config, inConfigPath);

        assertSchema(
                column("username", STRING),
                column("renamed_age", LONG)
        );
        assertRecords(
                record("user1", 20),
                record("user2", 21)
        );
    }

    @Test
    public void renameJsonColumn() {
        final String inConfigPath = "yaml/filter_json_input.yml";

        ConfigSource config = embulk.newConfig()
                .set("type", "rename")
                .set("columns", embulk.newConfig()
                        .set("record", "user_info")
                );
        embulk.runFilter(config, inConfigPath);

        assertSchema(
                column("user_info", JSON)
        );

        assertRecords(
                record(json("{\"username\": \"user1\", \"age\": 20}")),
                record(json("{\"username\": \"user2\", \"age\": 21}"))
        );
    }
}
