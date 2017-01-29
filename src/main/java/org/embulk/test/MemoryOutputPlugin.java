package org.embulk.test;

import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.Exec;
import org.embulk.spi.OutputPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;
import org.embulk.spi.TransactionalPageOutput;

import java.util.List;

public class MemoryOutputPlugin implements OutputPlugin {
    private static final Recorder recorder = new Recorder();

    public interface PluginTask extends Task {
    }

    @Override
    public ConfigDiff transaction(ConfigSource config,
                                  Schema schema, int taskCount,
                                  Control control) {
        final PluginTask task = config.loadConfig(PluginTask.class);
        return resume(task.dump(), schema, taskCount, control);
    }

    @Override
    public ConfigDiff resume(TaskSource taskSource,
                             Schema schema, int taskCount,
                             Control control) {
        control.run(taskSource);
        return Exec.newConfigDiff();
    }

    @Override
    public void cleanup(TaskSource taskSource,
                        Schema schema, int taskCount,
                        List<TaskReport> successTaskReports) {
    }

    @Override
    public TransactionalPageOutput open(final TaskSource taskSource, final Schema schema, final int taskIndex) {
        recorder.clear();
        recorder.setSchema(schema);
        return new TransactionalPageOutput() {
            private final PageReader reader = new PageReader(schema);

            public void add(Page page) {
                reader.setPage(page);
                while (reader.nextRecord()) {
                    recorder.addRecord(reader);
                }
            }

            public void finish() {
            }

            public void close() {
                reader.close();
            }

            public void abort() {
            }

            public TaskReport commit() {
                return Exec.newTaskReport();
            }
        };
    }

    public static void assertRecords(Record... records) {
        recorder.assertRecords(records);
    }
}
