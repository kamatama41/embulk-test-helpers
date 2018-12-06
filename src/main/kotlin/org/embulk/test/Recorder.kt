package org.embulk.test

import org.embulk.spi.Column
import org.embulk.spi.PageReader
import org.embulk.spi.Schema
import org.embulk.spi.util.Pages
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`

/**
 * Repository of [Record]
 */
internal class Recorder {
    private val records = mutableListOf<Record>()
    private var schema: Schema? = null

    @Synchronized fun addRecord(reader: PageReader) {
        val values: MutableList<Any?> = arrayListOf()
        reader.schema.visitColumns(object : Pages.ObjectColumnVisitor(reader) {
            override fun visit(column: Column, value: Any?) {
                values.add(value)
            }
        })
        this.records.add(Record(values))
    }

    @Synchronized fun setSchema(schema: Schema) {
        this.schema = schema
    }

    @Synchronized fun clear() {
        this.records.clear()
        this.schema = null
    }

    fun assertRecords(vararg records: Record) {
        assertThat(this.records.toHashSet(), `is`(records.toHashSet()))
    }

    fun assertSchema(vararg columns: Column) {
        val builder = columns.fold(Schema.builder()) { builder, column -> builder.add(column.name, column.type) }
        assertThat(builder.build(), `is`(this.schema))
    }
}
