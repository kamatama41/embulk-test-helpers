package org.embulk.test

import org.embulk.EmbulkEmbed
import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigSource
import org.embulk.exec.ResumeState
import org.junit.Before

abstract class EmbulkPluginTest {
    lateinit private var embulk: ExtendedTestingEmbulk

    @Before
    fun setup() {
        val builder = ExtendedTestingEmbulk.builder()
        setup(builder)
        embulk = builder.build() as ExtendedTestingEmbulk
    }

    protected open fun setup(builder: TestingEmbulk.Builder?) {
        // You can override this method in your test class
    }

    @JvmOverloads
    protected fun runInput(inConfig: ConfigSource, confDiff: ConfigDiff? = null): TestingEmbulk.RunResult {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .configDiff(confDiff)
                .execConfig(newConfig().set("min_output_tasks", 1))
                .outConfig(newConfig().set("type", "test"))
                .run()
    }

    @JvmOverloads
    protected fun resume(inConfig: ConfigSource, resumeState: ResumeState? = null): EmbulkEmbed.ResumableResult {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .resumeState(resumeState)
                .execConfig(newConfig().set("min_output_tasks", 1))
                .outConfig(newConfig().set("type", "test"))
                .resume()
    }

    protected fun runFilter(filterConfig: ConfigSource, inConfigPath: String): TestingEmbulk.RunResult {
        return embulk.RunConfig()
                .inConfig(configFromResource(inConfigPath))
                .filterConfig(filterConfig)
                .execConfig(newConfig().set("min_output_tasks", 1))
                .outConfig(newConfig().set("type", "test"))
                .run()
    }

    protected fun newConfig(): ConfigSource {
        return embulk.newConfig()
    }
}
