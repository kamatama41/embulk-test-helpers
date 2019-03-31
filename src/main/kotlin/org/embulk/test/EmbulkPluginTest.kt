package org.embulk.test

import org.embulk.config.ConfigSource

abstract class EmbulkPluginTest {
    @Embulk
    internal lateinit var embulk: ExtendedTestingEmbulk

    protected fun runConfig(inConfigPath: String): ExtendedTestingEmbulk.RunConfig {
        return runConfig(configFromResource(inConfigPath))
    }

    protected fun runConfig(inConfig: ConfigSource): ExtendedTestingEmbulk.RunConfig {
        return embulk.RunConfig()
                .inConfig(inConfig)
                .execConfig(config().set("min_output_tasks", 1))
                .outConfig(config().set("type", "local_object"))
    }

    protected fun config(): ConfigSource {
        return embulk.newConfig()
    }

    protected fun <T> getInstance(type: Class<T>): T {
        return embulk.superEmbed.injector.getInstance(type)
    }

    protected fun setSystemConfig(systemConfig: ConfigSource) {
        embulk.setSystemConfig(systemConfig)
    }
}
