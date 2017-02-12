package org.embulk.test

import org.embulk.config.Config
import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigInject
import org.embulk.config.ConfigSource
import org.embulk.config.Task
import org.embulk.config.TaskReport
import org.embulk.config.TaskSource
import org.embulk.spi.BufferAllocator
import org.embulk.spi.Exec
import org.embulk.spi.FileInputPlugin
import org.embulk.spi.TransactionalFileInput
import org.embulk.spi.time.TimestampParser
import org.embulk.spi.util.InputStreamTransactionalFileInput

class TestFileInputPlugin : FileInputPlugin {

    interface PluginTask : Task, TimestampParser.Task {
        @get:Config("data")
        val data: List<String>
        @get:ConfigInject
        val bufferAllocator: BufferAllocator
    }

    override fun transaction(config: ConfigSource, control: FileInputPlugin.Control): ConfigDiff {
        val task = config.loadConfig(PluginTask::class.java)
        val taskCount = 1
        return resume(task.dump(), taskCount, control)
    }

    override fun resume(taskSource: TaskSource, taskCount: Int, control: FileInputPlugin.Control): ConfigDiff {
        control.run(taskSource, taskCount)
        return Exec.newConfigDiff()
    }

    override fun cleanup(taskSource: TaskSource, taskCount: Int, successTaskReports: List<TaskReport>) {}

    override fun open(taskSource: TaskSource, taskIndex: Int): TransactionalFileInput {
        val task = taskSource.loadTask(PluginTask::class.java)
        return object : InputStreamTransactionalFileInput(
                task.bufferAllocator, { task.data.joinToString(LINE_SEPARATOR).byteInputStream() }) {
            override fun abort() {}

            override fun commit(): TaskReport {
                return Exec.newTaskReport()
            }
        }
    }

    companion object {
        private val LINE_SEPARATOR = System.getProperty("line.separator")
    }
}
