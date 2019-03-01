package org.embulk.test

import org.embulk.config.Config
import org.embulk.config.ConfigDefault
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
import org.embulk.spi.type.Type
import org.embulk.spi.util.Pages
import java.lang.IllegalArgumentException
import java.util.Optional
import kotlin.Comparator


class LocalObjectOutputPlugin : OutputPlugin {

    interface PluginTask : Task {
        @get:Config("incremental")
        @get:ConfigDefault("false")
        val incremental: Boolean

        @get:Config("incremental_column")
        @get:ConfigDefault("\"\"")
        val incrementalColumn: String

        @get:Config("last_record")
        @get:ConfigDefault("null")
        val lastRecord: Optional<Record>
    }

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
        val task = taskSource.loadTask(PluginTask::class.java)
        val configDiff = Exec.newConfigDiff()
        val taskReports = control.run(taskSource)
        incrementalColumn(task, schema)?.let {
            val lastRecord = taskReports
                    .mapNotNull { it.get(Record::class.java, "last_record") }
                    .maxWith(object : Comparator<Record> {
                        override fun compare(o1: Record?, o2: Record?): Int {
                            return compareRecord(o1, o2, it)
                        }
                    })
            configDiff.set("last_record", lastRecord)
        }
        return configDiff
    }

    override fun cleanup(taskSource: TaskSource,
                         schema: Schema, taskCount: Int,
                         successTaskReports: List<TaskReport>) {
    }

    override fun open(taskSource: TaskSource, schema: Schema, taskIndex: Int): TransactionalPageOutput {
        val task = taskSource.loadTask(PluginTask::class.java)
        val incrementalColumn = incrementalColumn(task, schema)
        val lastRecord: Record? = task.lastRecord.orElse(null)
        var biggestRecord: Record? = lastRecord
        recorder.setSchema(schema)
        return object : TransactionalPageOutput {
            private val reader = PageReader(schema)

            override fun add(page: Page) {
                reader.setPage(page)
                while (reader.nextRecord()) {
                    val values: MutableList<Any?> = arrayListOf()
                    reader.schema.visitColumns(object : Pages.ObjectColumnVisitor(reader) {
                        override fun visit(column: Column, value: Any?) {
                            values.add(value)
                        }
                    })
                    val record = Record(values)
                    if (incrementalColumn == null || lastRecord == null || compareRecord(lastRecord, record, incrementalColumn) < 0) {
                        recorder.addRecord(record)
                    }
                    if (biggestRecord == null || (incrementalColumn != null && compareRecord(biggestRecord, record, incrementalColumn) < 0)) {
                        biggestRecord = record
                    }
                }
            }

            override fun finish() {}

            override fun close() {
                reader.close()
            }

            override fun abort() {}

            override fun commit(): TaskReport {
                val taskReport = Exec.newTaskReport()
                biggestRecord?.let {
                    taskReport.set("last_record", it)
                }
                return taskReport
            }
        }
    }

    private fun incrementalColumn(task: PluginTask, schema: Schema): Column? {
        return if (task.incremental) {
            checkNotNull(schema.columns.find { it.name == task.incrementalColumn })
        } else {
            null
        }
    }

    private fun compareRecord(o1: Record?, o2: Record?, column: Column): Int {
        val v1 = o1!!.values[column.index]
        val v2 = o2!!.values[column.index]
        return when(val type = column.type.javaType) {
            String::class.java ->
                (v1 as String).compareTo(v2 as String)
            Long::class.java, Long::class.javaPrimitiveType ->
                (v1 as Number).toLong().compareTo((v2 as Number).toLong())
            Double::class.java, Double::class.javaPrimitiveType ->
                (v1 as Number).toDouble().compareTo((v2 as Number).toDouble())
            else -> throw IllegalArgumentException("Unsupported type $type")
        }
    }

    companion object Matcher {
        private val recorder = Recorder()

        @JvmStatic
        fun assertRecords(vararg records: Record) {
            recorder.assertRecords(*records)
        }

        @JvmStatic
        fun assertSchema(vararg columns: Column) {
            recorder.assertSchema(*columns)
        }

        @JvmStatic
        fun assertSchema(vararg nameAndType: Pair<String, Type>) {
            val cols = nameAndType.map { Column(-1, it.first, it.second) }
            recorder.assertSchema(*cols.toTypedArray())
        }
    }

}
