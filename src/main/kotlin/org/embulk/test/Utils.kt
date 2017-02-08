package org.embulk.test

import org.embulk.spi.Column
import org.embulk.spi.json.JsonParser
import org.embulk.spi.time.Timestamp
import org.embulk.spi.type.Type
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.msgpack.value.Value

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
        // Convert Int to Long since Embulk supports only Long
        return Record(values.map { (it as? Int)?.toLong() ?: it })
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
