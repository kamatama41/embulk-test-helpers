package com.kamatama41.embulk.test;

import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.spi.Exec;
import org.embulk.spi.ExecutorPlugin;
import org.embulk.spi.ProcessState;
import org.embulk.spi.Schema;
import org.embulk.spi.util.Executors;

public class SimpleExecutorPlugin implements ExecutorPlugin {
    @Override
    public void transaction(ConfigSource config, Schema outputSchema, int inputTaskCount, Control control) {
        control.transaction(outputSchema, inputTaskCount, (task, state) -> {
            state.initialize(inputTaskCount, inputTaskCount);
            for (int i = 0; i < inputTaskCount; i++) {
                final int taskIndex = i;
                if (state.getOutputTaskState(i).isCommitted()) {
                    System.out.println("Skipped resumed task " + i);
                    continue;
                }
                try {
                    Executors.process(Exec.session(), task, taskIndex, new Executors.ProcessStateCallback() {
                        @Override
                        public void started() {
                            state.getInputTaskState(taskIndex).start();
                            state.getOutputTaskState(taskIndex).start();
                        }

                        @Override
                        public void inputCommitted(TaskReport report) {
                            state.getInputTaskState(taskIndex).setTaskReport(report);
                        }

                        @Override
                        public void outputCommitted(TaskReport report) {
                            state.getOutputTaskState(taskIndex).setTaskReport(report);
                        }
                    });
                } finally {
                    state.getInputTaskState(taskIndex).finish();
                    state.getOutputTaskState(taskIndex).finish();
                    showProgress(state, inputTaskCount);
                }
            }
        });
    }

    private static void showProgress(ProcessState state, int taskCount) {
        int started = 0;
        int finished = 0;
        for (int i = 0; i < taskCount; i++) {
            if (state.getOutputTaskState(i).isStarted()) {
                started++;
            }
            if (state.getOutputTaskState(i).isFinished()) {
                finished++;
            }
        }
        System.out.println(String.format("{done:%3d / %d, running: %d}", finished, taskCount, started - finished));
    }
}
