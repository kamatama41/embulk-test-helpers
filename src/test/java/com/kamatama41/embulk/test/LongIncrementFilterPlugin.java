package com.kamatama41.embulk.test;


import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Column;
import org.embulk.spi.DataException;
import org.embulk.spi.Exec;
import org.embulk.spi.FilterPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;
import org.embulk.spi.time.Timestamp;
import org.embulk.spi.type.Types;
import org.msgpack.value.Value;

public class LongIncrementFilterPlugin implements FilterPlugin {

    interface PluginTask extends Task {
    }

    @Override
    public void transaction(ConfigSource config, Schema inputSchema, FilterPlugin.Control control) {
        PluginTask task = config.loadConfig(PluginTask.class);
        control.run(task.dump(), inputSchema);
    }

    @Override
    public PageOutput open(final TaskSource taskSource, final Schema inputSchema,
                           final Schema outputSchema, final PageOutput output) {

        return new PageOutput() {
            private PageReader reader = new PageReader(inputSchema);
            private PageBuilder builder = new PageBuilder(Exec.getBufferAllocator(), outputSchema, output);

            @Override
            public void add(Page page) {
                reader.setPage(page);
                while (reader.nextRecord()) {
                    setValue();
                    builder.addRecord();
                }
            }

            private void setValue() {
                for (Column inputColumn : inputSchema.getColumns()) {
                    if (reader.isNull(inputColumn)) {
                        builder.setNull(inputColumn);
                        continue;
                    }

                    if (Types.STRING.equals(inputColumn.getType())) {
                        final String value = reader.getString(inputColumn);
                        builder.setString(inputColumn, value);
                    } else if (Types.BOOLEAN.equals(inputColumn.getType())) {
                        final boolean value = reader.getBoolean(inputColumn);
                        builder.setBoolean(inputColumn, value);
                    } else if (Types.DOUBLE.equals(inputColumn.getType())) {
                        final double value = reader.getDouble(inputColumn);
                        builder.setDouble(inputColumn, value);
                    } else if (Types.LONG.equals(inputColumn.getType())) {
                        // Increment if long value
                        final long value = reader.getLong(inputColumn);
                        builder.setLong(inputColumn, value + 1);
                    } else if (Types.TIMESTAMP.equals(inputColumn.getType())) {
                        final Timestamp value = reader.getTimestamp(inputColumn);
                        builder.setTimestamp(inputColumn, value);
                    } else if (Types.JSON.equals(inputColumn.getType())) {
                        final Value value = reader.getJson(inputColumn);
                        builder.setJson(inputColumn, value);
                    } else {
                        throw new DataException("Unexpected type:" + inputColumn.getType());
                    }
                }
            }

            @Override
            public void finish() {
                builder.finish();
            }

            @Override
            public void close() {
                builder.close();
            }
        };
    }
}
