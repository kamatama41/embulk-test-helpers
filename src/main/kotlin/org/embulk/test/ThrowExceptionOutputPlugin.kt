package org.embulk.test

import org.embulk.config.Config
import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigSource
import org.embulk.config.Task
import org.embulk.config.TaskReport
import org.embulk.config.TaskSource
import org.embulk.plugin.PluginType
import org.embulk.spi.Exec
import org.embulk.spi.ExecSession
import org.embulk.spi.OutputPlugin
import org.embulk.spi.Page
import org.embulk.spi.Schema
import org.embulk.spi.TransactionalPageOutput
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ThrowExceptionOutputPlugin : OutputPlugin {

    interface PluginTask : Task {
        @get:Config("source")
        val source: ConfigSource

        @get:Config("thrown_on")
        val thrownOn: ThrownOn

        var taskSource: TaskSource?
    }

    enum class ThrownOn {
        BEFORE_TRANSACTION,
        AFTER_TRANSACTION,
        BEFORE_RESUME,
        AFTER_RESUME,
        CLEANUP,
        OPEN,
        ADD,
        FINISH,
        ABORT,
        CLOSE,
        COMMIT;
    }

    private val executorService = Executors.newCachedThreadPool()!!
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun transaction(config: ConfigSource,
                             schema: Schema, taskCount: Int,
                             control: OutputPlugin.Control): ConfigDiff {
        val task = config.loadConfig(PluginTask::class.java)
        if (task.thrownOn == ThrownOn.BEFORE_TRANSACTION) {
            throw RuntimeException("Failed on before transaction.")
        }

        val controlTask = RunControlTask(task, control, executorService).apply { runAsynchronously() }
        val originalDiff = buildPluginDelegate(task, Exec.session())
                .transaction(schema, taskCount, controlTask)
                .get()

        if (task.thrownOn == ThrownOn.AFTER_TRANSACTION) {
            throw RuntimeException("Failed on after transaction.")
        }
        return buildConfigDiff(originalDiff)
    }

    override fun resume(taskSource: TaskSource,
                        schema: Schema, taskCount: Int,
                        control: OutputPlugin.Control): ConfigDiff {
        val task = taskSource.loadTask(PluginTask::class.java)
        if (task.thrownOn == ThrownOn.BEFORE_RESUME) {
            throw RuntimeException("Failed on before resume.")
        }

        val controlTask = RunControlTask(task, control, executorService).apply { runAsynchronously() }
        val originalDiff = buildPluginDelegate(task, Exec.session())
                .resume(schema, taskCount, controlTask)
                .get()

        if (task.thrownOn == ThrownOn.AFTER_RESUME) {
            throw RuntimeException("Failed on after resume.")
        }
        return buildConfigDiff(originalDiff)
    }

    override fun cleanup(taskSource: TaskSource,
                         schema: Schema, taskCount: Int,
                         successTaskReports: List<TaskReport>) {
        val task = taskSource.loadTask(PluginTask::class.java)
        if (task.thrownOn == ThrownOn.AFTER_RESUME) {
            throw RuntimeException("Failed on after resume.")
        }
        buildPluginDelegate(task, Exec.session()).cleanup(schema, taskCount, successTaskReports)
    }

    override fun open(taskSource: TaskSource, schema: Schema, taskIndex: Int): TransactionalPageOutput {
        val task = taskSource.loadTask(PluginTask::class.java)
        if (task.thrownOn == ThrownOn.OPEN) {
            throw RuntimeException("Failed on open.")
        }
        val originalOutput = buildPluginDelegate(task, Exec.session()).open(schema, taskIndex)

        return object : TransactionalPageOutput {

            override fun add(page: Page) {
                if (task.thrownOn == ThrownOn.ADD) {
                    throw RuntimeException("Failed on add.")
                }
                originalOutput.add(page)
            }

            override fun finish() {
                if (task.thrownOn == ThrownOn.FINISH) {
                    throw RuntimeException("Failed on finish.")
                }
                originalOutput.finish()
            }

            override fun close() {
                if (task.thrownOn == ThrownOn.CLOSE) {
                    throw RuntimeException("Failed on close.")
                }
                originalOutput.close()
            }

            override fun abort() {
                if (task.thrownOn == ThrownOn.ABORT) {
                    throw RuntimeException("Failed on abort.")
                }
                originalOutput.abort()
            }

            override fun commit(): TaskReport {
                if (task.thrownOn == ThrownOn.COMMIT) {
                    throw RuntimeException("Failed on commit.")
                }
                return originalOutput.commit()
            }
        }
    }

    private class RunControlTask internal constructor(
            private val task: PluginTask,
            private val control: OutputPlugin.Control,
            private val executorService: ExecutorService) : Callable<List<TaskReport>> {
        private val latch: CountDownLatch = CountDownLatch(1)
        private lateinit var taskSource: TaskSource
        private lateinit var future: Future<List<TaskReport>>

        override fun call(): List<TaskReport> {
            latch.await()
            task.taskSource = taskSource
            return control.run(task.dump())
        }

        internal fun runAsynchronously() {
            future = executorService.submit(this)
        }

        internal fun cancel() {
            future.cancel(true)
        }

        internal fun addTaskSource(taskSource: TaskSource) {
            this.taskSource = taskSource
            latch.countDown()
        }

        internal fun waitAndGetResult(): List<TaskReport> {
            return future.get()
        }
    }

    private class OutputPluginDelegate internal constructor(
            private val type: PluginType,
            private val plugin: OutputPlugin,
            private val config: ConfigSource,
            private val taskSource: TaskSource?,
            private val executorService: ExecutorService
    ) {
        private val pluginNameForLogging: String get() = String.format("%s output plugin", type.name)

        internal fun transaction(schema: Schema, taskCount: Int, controlTask: RunControlTask): Future<ConfigDiff> {
            return executorService.submit<ConfigDiff> {
                try {
                    return@submit plugin.transaction(config, schema, taskCount, ControlDelegate(controlTask))
                } catch (e: CancellationException) {
                    logger.error("Canceled transaction for {} by other plugin's error", pluginNameForLogging)
                    throw e
                } catch (e: Exception) {
                    logger.error("Transaction for {} failed.", pluginNameForLogging, e)
                    controlTask.cancel()
                    throw e
                }
            }
        }

        internal fun resume(schema: Schema, taskCount: Int, controlTask: RunControlTask): Future<ConfigDiff> {
            return executorService.submit<ConfigDiff> {
                try {
                    return@submit plugin.resume(taskSource, schema, taskCount, ControlDelegate(controlTask))
                } catch (e: CancellationException) {
                    logger.error("Canceled resume for {} by other plugin's error", pluginNameForLogging)
                    throw e
                } catch (e: Exception) {
                    logger.error("Resume for {} failed.", pluginNameForLogging, e)
                    controlTask.cancel()
                    throw e
                }
            }
        }

        internal fun cleanup(schema: Schema, taskCount: Int, successTaskReports: List<TaskReport>) {
            plugin.cleanup(taskSource, schema, taskCount, successTaskReports)
        }

        internal fun open(schema: Schema, taskIndex: Int): TransactionalPageOutput {
            return plugin.open(taskSource, schema, taskIndex)
        }
    }

    private class ControlDelegate internal constructor(private val controlTask: RunControlTask) : OutputPlugin.Control {
        override fun run(taskSource: TaskSource): List<TaskReport> {
            controlTask.addTaskSource(taskSource)
            try {
                return controlTask.waitAndGetResult()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun buildPluginDelegate(task: PluginTask, session: ExecSession): OutputPluginDelegate {
        val config = task.source
        val pluginType = config.get(PluginType::class.java, "type")
        val outputPlugin = session.newPlugin(OutputPlugin::class.java, pluginType)
        var taskSource: TaskSource? = null
        if (task.taskSource != null) {
            taskSource = task.taskSource
        }
        return OutputPluginDelegate(pluginType, outputPlugin, config, taskSource, executorService)
    }

    private fun buildConfigDiff(originalDiff: ConfigDiff): ConfigDiff {
        val result = Exec.newConfigDiff()
        result.set("source", originalDiff)
        return result
    }
}
