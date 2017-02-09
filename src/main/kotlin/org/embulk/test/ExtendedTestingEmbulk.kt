package org.embulk.test

import org.embulk.EmbulkEmbed
import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigSource
import org.embulk.exec.ResumeState
import org.embulk.spi.InputPlugin
import org.embulk.spi.OutputPlugin

class ExtendedTestingEmbulk internal constructor(builder: ExtendedTestingEmbulk.Builder) : TestingEmbulk(builder) {
    private val superEmbed: EmbulkEmbed

    init {
        this.superEmbed = extractSuperField<EmbulkEmbed>("embed")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> extractSuperField(fieldName: String): T {
        val field = TestingEmbulk::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(this) as T
    }

    class Builder : TestingEmbulk.Builder() {
        override fun build(): TestingEmbulk {
            this.registerPlugin(InputPlugin::class, "test", TestInputPlugin::class)
            this.registerPlugin(OutputPlugin::class, "test", TestOutputPlugin::class)
            return ExtendedTestingEmbulk(this)
        }
    }

    internal inner class RunConfig {
        private var inConfig: ConfigSource? = null
        private val filterConfigs = mutableListOf<ConfigSource>()
        private var execConfig: ConfigSource? = null
        private var outConfig: ConfigSource? = null
        private var configDiff: ConfigDiff? = null
        private var resumeState: ResumeState? = null

        fun inConfig(inConfig: ConfigSource): RunConfig {
            this.inConfig = inConfig
            return this
        }

        fun filterConfig(filterConfig: ConfigSource): RunConfig {
            this.filterConfigs.add(filterConfig)
            return this
        }

        fun execConfig(execConfig: ConfigSource): RunConfig {
            this.execConfig = execConfig
            return this
        }

        fun outConfig(outConfig: ConfigSource): RunConfig {
            this.outConfig = outConfig
            return this
        }

        fun configDiff(configDiff: ConfigDiff?): RunConfig {
            this.configDiff = configDiff
            return this
        }

        fun resumeState(resumeState: ResumeState?): RunConfig {
            this.resumeState = resumeState
            return this
        }

        fun run(): RunResult {
            val config = newConfig()
                    .set("filters", filterConfigs)
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("out", outConfig)
            // embed.run returns TestingBulkLoader.TestingExecutionResult because
            if (configDiff == null) {
                return superEmbed.run(config) as TestingEmbulk.RunResult
            } else {
                return superEmbed.run(config.merge(configDiff)) as TestingEmbulk.RunResult
            }
        }

        fun resume(): EmbulkEmbed.ResumableResult {
            val config = newConfig()
                    .set("filters", filterConfigs)
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("out", outConfig)
            if (resumeState == null) {
                return superEmbed.runResumable(config)
            } else {
                return superEmbed.ResumeStateAction(config, resumeState).resume()
            }
        }
    }

    companion object {
        @JvmStatic
        fun builder(): ExtendedTestingEmbulk.Builder {
            return ExtendedTestingEmbulk.Builder()
        }
    }
}
