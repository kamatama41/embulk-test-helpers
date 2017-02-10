@file:JvmName("Utils")

package org.embulk.test

import com.fasterxml.jackson.databind.ObjectMapper
import org.embulk.EmbulkEmbed
import org.embulk.config.ConfigSource
import org.embulk.spi.Column
import org.embulk.spi.json.JsonParser
import org.embulk.spi.time.Timestamp
import org.embulk.spi.type.Type
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.msgpack.value.Value

private val jsonParser = JsonParser()
private val mapper = ObjectMapper()

@JvmOverloads
fun timestamp(year: Int, month: Int, date: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): Timestamp {
    return Timestamp.ofEpochSecond(
            DateTime(year, month, date, hour, minute, second, DateTimeZone.UTC).millis / 1000
    )
}

fun record(vararg values: Any): Record {
    // Convert Int to Long since Embulk supports only Long
    return Record(values.map { (it as? Int)?.toLong() ?: it })
}

fun column(name: String, type: Type): Column {
    return Column(-1, name, type)
}

fun json(json: String): Value {
    return jsonParser.parse(json)
}

fun json(vararg pairs: Pair<String, Any>): Value {
    return json(mapper.writeValueAsString(mapOf(*pairs)))
}

fun configFromString(yaml: String): ConfigSource {
    return EmbulkEmbed.newSystemConfigLoader().fromYamlString(yaml)
}

fun configFromResource(name: String): ConfigSource {
    return configFromString(EmbulkTests.readResource(name))
}

data class Record internal constructor(private val values: List<Any>)
