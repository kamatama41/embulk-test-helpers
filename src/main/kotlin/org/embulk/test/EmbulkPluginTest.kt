package org.embulk.test

import org.embulk.EmbulkEmbed
import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigSource
import org.embulk.exec.ResumeState

abstract class EmbulkPluginTest {
    protected val builder: ExtendedTestingEmbulk.Builder = ExtendedTestingEmbulk.builder()
    private val embulk: ExtendedTestingEmbulk by lazy {
        builder.build() as ExtendedTestingEmbulk
    }

    @JvmOverloads
    protected fun runInput(inConfig: ConfigSource, confDiff: ConfigDiff? = null): TestingEmbulk.RunResult {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .configDiff(confDiff)
                .execConfig(config().set("min_output_tasks", 1))
                .outConfig(config().set("type", "test"))
                .run()
    }

    @JvmOverloads
    protected fun resume(inConfig: ConfigSource, resumeState: ResumeState? = null): EmbulkEmbed.ResumableResult {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .resumeState(resumeState)
                .execConfig(config().set("min_output_tasks", 1))
                .outConfig(config().set("type", "test"))
                .resume()
    }

    protected fun runFilter(filterConfig: ConfigSource, inConfigPath: String): TestingEmbulk.RunResult {
        return embulk.RunConfig()
                .inConfig(configFromResource(inConfigPath))
                .filterConfig(filterConfig)
                .execConfig(config().set("min_output_tasks", 1))
                .outConfig(config().set("type", "test"))
                .run()
    }

    protected fun config(): ConfigSource {
        return embulk.newConfig()
    }
}
