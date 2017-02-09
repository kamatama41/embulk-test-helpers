package org.embulk.test

import org.embulk.config.ConfigDiff
import org.embulk.config.ConfigSource
import org.embulk.config.Task
import org.embulk.config.TaskReport
import org.embulk.config.TaskSource
import org.embulk.spi.Column
import org.embulk.spi.Exec
import org.embulk.spi.OutputPlugin
import org.embulk.spi.Page
import org.embulk.spi.PageReader
import org.embulk.spi.Schema
import org.embulk.spi.TransactionalPageOutput


class TestOutputPlugin : OutputPlugin {

    interface PluginTask : Task

    override fun transaction(config: ConfigSource,
                             schema: Schema, taskCount: Int,
                             control: OutputPlugin.Control): ConfigDiff {
        val task = config.loadConfig(PluginTask::class.java)
        return resume(task.dump(), schema, taskCount, control)
    }

    override fun resume(taskSource: TaskSource,
                        schema: Schema, taskCount: Int,
                        control: OutputPlugin.Control): ConfigDiff {
        recorder.clear()
        control.run(taskSource)
        return Exec.newConfigDiff()
    }

    override fun cleanup(taskSource: TaskSource,
                         schema: Schema, taskCount: Int,
                         successTaskReports: List<TaskReport>) {
    }

    override fun open(taskSource: TaskSource, schema: Schema, taskIndex: Int): TransactionalPageOutput {
        recorder.setSchema(schema)
        return object : TransactionalPageOutput {
            private val reader = PageReader(schema)

            override fun add(page: Page) {
                reader.setPage(page)
                while (reader.nextRecord()) {
                    recorder.addRecord(reader)
                }
            }

            override fun finish() {}

            override fun close() {
                reader.close()
            }

            override fun abort() {}

            override fun commit(): TaskReport {
                return Exec.newTaskReport()
            }
        }
    }

    companion object {
        private val recorder = Recorder()

        @JvmStatic
        fun assertRecords(vararg records: Record) {
            recorder.assertRecords(*records)
        }

        @JvmStatic
        fun assertSchema(vararg columns: Column) {
            recorder.assertSchema(*columns)
        }
    }

}
