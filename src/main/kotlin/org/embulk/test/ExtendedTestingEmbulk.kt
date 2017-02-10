package org.embulk.test

import org.embulk.EmbulkEmbed
import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigSource
import org.embulk.exec.ResumeState
import org.embulk.spi.FileInputPlugin
import org.embulk.spi.FileOutputPlugin
import org.embulk.spi.FilterPlugin
import org.embulk.spi.InputPlugin
import org.embulk.spi.OutputPlugin
import org.embulk.spi.ParserPlugin

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
            this.registerPlugin(TestInputPlugin::class, "test")
            this.registerPlugin(TestOutputPlugin::class, "test")
            return ExtendedTestingEmbulk(this)
        }

        @JvmOverloads
        fun registerPlugin(
                impl: Class<*>, name: String = guessName(impl), iface: Class<*> = guessInterface(impl)): ExtendedTestingEmbulk.Builder {
            super.registerPlugin(iface, name, impl)
            return this
        }

        fun guessInterface(impl: Class<*>): Class<*> {
            return impl.interfaces.find {
                interfaceMap.keys.contains(it)
            }.let {
                interfaceMap[it]
            } ?: throw IllegalStateException("Plugin interface not found")
        }

        /**
         * Remove "~~~Plugin" from class name and convert to snake case
         */
        fun guessName(impl: Class<*>): String {
            return interfaceMap.values.fold(impl.simpleName) { name, iface ->
                name.removeSuffix(iface.simpleName)
            }.toSnakeCase()
        }

        private companion object {
            private val interfaceMap = mapOf(
                    InputPlugin::class.java to InputPlugin::class.java,
                    FileInputPlugin::class.java to InputPlugin::class.java,
                    ParserPlugin::class.java to ParserPlugin::class.java,
                    FilterPlugin::class.java to FilterPlugin::class.java,
                    OutputPlugin::class.java to OutputPlugin::class.java,
                    FileOutputPlugin::class.java to OutputPlugin::class.java
            )
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
