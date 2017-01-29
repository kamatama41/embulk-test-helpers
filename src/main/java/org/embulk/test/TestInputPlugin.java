package org.embulk.test;

import org.embulk.config.Config;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigInject;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.BufferAllocator;
import org.embulk.spi.Exec;
import org.embulk.spi.FileInputPlugin;
import org.embulk.spi.TransactionalFileInput;
import org.embulk.spi.time.TimestampParser;
import org.embulk.spi.util.InputStreamTransactionalFileInput;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestInputPlugin implements FileInputPlugin {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public interface PluginTask extends Task, TimestampParser.Task {
        @Config("data")
        List<String> getData();

        @ConfigInject
        BufferAllocator getBufferAllocator();
    }

    @Override
    public ConfigDiff transaction(ConfigSource config, FileInputPlugin.Control control) {
        PluginTask task = config.loadConfig(PluginTask.class);
        int taskCount = 1;
        return resume(task.dump(), taskCount, control);
    }

    @Override
    public ConfigDiff resume(TaskSource taskSource, int taskCount, Control control) {
        control.run(taskSource, taskCount);
        return Exec.newConfigDiff();
    }

    @Override
    public void cleanup(TaskSource taskSource, int taskCount, List<TaskReport> successTaskReports) {
    }

    @Override
    public TransactionalFileInput open(TaskSource taskSource, int taskIndex) {
        final PluginTask task = taskSource.loadTask(PluginTask.class);

        return new InputStreamTransactionalFileInput(
                task.getBufferAllocator(),
                new InputStreamTransactionalFileInput.Opener() {
                    @Override
                    public InputStream open() throws IOException {
                        StringBuilder sb = new StringBuilder();
                        for (String row : task.getData()) {
                            sb.append(row).append(LINE_SEPARATOR);
                        }
                        return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
                    }
                }
        ) {
            @Override
            public void abort() {
            }

            @Override
            public TaskReport commit() {
                return Exec.newTaskReport();
            }
        };
    }
}
