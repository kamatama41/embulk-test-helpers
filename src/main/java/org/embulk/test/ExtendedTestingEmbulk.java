package org.embulk.test;

import org.embulk.EmbulkEmbed;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.exec.ResumeState;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.OutputPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ExtendedTestingEmbulk extends TestingEmbulk {

    public static class Builder extends TestingEmbulk.Builder {
        public TestingEmbulk build() {
            this.registerPlugin(InputPlugin.class, "test", TestInputPlugin.class);
            this.registerPlugin(OutputPlugin.class, "test", TestOutputPlugin.class);
            return new ExtendedTestingEmbulk(this);
        }
    }

    public static TestingEmbulk.Builder builder()
    {
        return new ExtendedTestingEmbulk.Builder();
    }

    private final EmbulkEmbed superEmbed;

    ExtendedTestingEmbulk(Builder builder) {
        super(builder);
        this.superEmbed = extractSuperField("embed");
    }

    @SuppressWarnings("unchecked")
    private <T> T extractSuperField(String fieldName) {
        try {
            Field field = TestingEmbulk.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    class RunConfig {
        private ConfigSource inConfig;
        private List<ConfigSource> filterConfigs = new ArrayList<>();
        private ConfigSource execConfig;
        private ConfigSource outConfig;
        private ConfigDiff configDiff;
        private ResumeState resumeState;

        RunConfig() {}

        RunConfig inConfig(ConfigSource inConfig) {
            this.inConfig = inConfig;
            return this;
        }

        RunConfig filterConfig(ConfigSource filterConfig) {
            this.filterConfigs.add(filterConfig);
            return this;
        }

        RunConfig execConfig(ConfigSource execConfig) {
            this.execConfig = execConfig;
            return this;
        }

        RunConfig outConfig(ConfigSource outConfig) {
            this.outConfig = outConfig;
            return this;
        }

        RunConfig configDiff(ConfigDiff configDiff) {
            this.configDiff = configDiff;
            return this;
        }

        RunConfig resumeState(ResumeState resumeState) {
            this.resumeState = resumeState;
            return this;
        }

        RunResult run() {
            ConfigSource config = newConfig()
                    .set("filters", filterConfigs)
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("out", outConfig);
            // embed.run returns TestingBulkLoader.TestingExecutionResult because
            if (configDiff == null) {
                return (RunResult) superEmbed.run(config);
            } else {
                return (RunResult) superEmbed.run(config.merge(configDiff));
            }
        }

        EmbulkEmbed.ResumableResult resume() {
            ConfigSource config = newConfig()
                    .set("filters", filterConfigs)
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("out", outConfig);
            if (resumeState == null) {
                return superEmbed.runResumable(config);
            } else {
                return superEmbed.new ResumeStateAction(config, resumeState).resume();
            }
        }
    }
}
