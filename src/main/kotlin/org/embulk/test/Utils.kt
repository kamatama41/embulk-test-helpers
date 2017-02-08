package org.embulk.test

import org.embulk.spi.Column
import org.embulk.spi.json.JsonParser
import org.embulk.spi.time.Timestamp
import org.embulk.spi.type.Type
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.msgpack.value.Value

import java.util.ArrayList

object Utils {
    private val jsonParser = JsonParser()

    @JvmOverloads
    @JvmStatic
    fun timestamp(year: Int, month: Int, date: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): Timestamp {
        return Timestamp.ofEpochSecond(
                DateTime(year, month, date, hour, minute, second, DateTimeZone.UTC).millis / 1000
        )
    }

    @JvmStatic
    fun record(vararg values: Any): Record {
        val converted = ArrayList<Any>(values.size)
        for (value in values) {
            if (value is Int) {
                converted.add(value.toLong())
            } else {
                converted.add(value)
            }
        }
        return Record(converted)
    }

    @JvmStatic
    fun column(name: String, type: Type): Column {
        return Column(-1, name, type)
    }

    @JvmStatic
    fun json(json: String): Value {
        return jsonParser.parse(json)
    }
}
