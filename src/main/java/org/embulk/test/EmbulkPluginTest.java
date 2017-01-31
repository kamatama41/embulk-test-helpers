package org.embulk.test;

import org.embulk.EmbulkEmbed;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.exec.ResumeState;
import org.junit.Before;

public abstract class EmbulkPluginTest {
    private ExtendedTestingEmbulk embulk;

    @Before
    public void setup() {
        TestingEmbulk.Builder builder = ExtendedTestingEmbulk.builder();
        setup(builder);
        embulk = (ExtendedTestingEmbulk) builder.build();
    }

    protected void setup(TestingEmbulk.Builder builder) {
        // You can override this method in your test class
    }

    protected TestingEmbulk.RunResult runInput(ConfigSource inConfig) {
        return runInput(inConfig, null);
    }

    protected TestingEmbulk.RunResult runInput(ConfigSource inConfig, ConfigDiff confDiff) {
        return embulk.new RunConfig()
                .inConfig(inConfig)
                .configDiff(confDiff)
                .execConfig(newConfig().set("min_output_tasks", 1))
                .outConfig(newConfig().set("type", "test"))
                .run();
    }

    protected EmbulkEmbed.ResumableResult resume(ConfigSource inConfig) {
        return resume(inConfig, null);
    }

    protected EmbulkEmbed.ResumableResult resume(ConfigSource inConfig, ResumeState resumeState) {
        return embulk.new RunConfig()
                .inConfig(inConfig)
                .resumeState(resumeState)
                .execConfig(newConfig().set("min_output_tasks", 1))
                .outConfig(newConfig().set("type", "test"))
                .resume();
    }

    protected TestingEmbulk.RunResult runFilter(ConfigSource filterConfig, String inConfigPath) {
        return embulk.new RunConfig()
                .inConfig(ExtendedEmbulkTests.configFromResource(inConfigPath))
                .filterConfig(filterConfig)
                .execConfig(newConfig().set("min_output_tasks", 1))
                .outConfig(newConfig().set("type", "test"))
                .run();
    }

    protected ConfigSource newConfig() {
        return embulk.newConfig();
    }
}
