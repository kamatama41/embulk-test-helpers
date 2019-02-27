package org.embulk.test

import org.embulk.EmbulkEmbed
import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigSource
import org.embulk.exec.ResumeState

abstract class EmbulkPluginTest {
    @Embulk
    lateinit var embulk: ExtendedTestingEmbulk

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
    protected fun runOutput(
            inConfig: ConfigSource,
            outConfig: ConfigSource,
            confDiff: ConfigDiff? = null,
            minOutputTasks: Int = 1
    ): TestingEmbulk.RunResult {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .configDiff(confDiff)
                .execConfig(config().set("min_output_tasks", minOutputTasks))
                .outConfig(outConfig)
                .run()
    }

    @JvmOverloads
    protected fun resumeInput(inConfig: ConfigSource, resumeState: ResumeState? = null): EmbulkEmbed.ResumableResult {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .resumeState(resumeState)
                .execConfig(config().set("min_output_tasks", 1))
                .outConfig(config().set("type", "test"))
                .resume()
    }

    @JvmOverloads
    protected fun resumeOutput(
            inConfig: ConfigSource,
            outConfig: ConfigSource,
            resumeState: ResumeState? = null,
            minOutputTasks: Int = 1
    ): EmbulkEmbed.ResumableResult {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .resumeState(resumeState)
                .execConfig(config().set("min_output_tasks", minOutputTasks))
                .outConfig(outConfig)
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
