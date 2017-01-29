package org.embulk.test;

import org.embulk.spi.Column;
import org.embulk.spi.json.JsonParser;
import org.embulk.spi.time.Timestamp;
import org.embulk.spi.type.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.msgpack.value.Value;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final JsonParser jsonParser = new JsonParser();

    public static Timestamp timestamp(int year, int month, int date) {
        return timestamp(year, month, date, 0, 0, 0);
    }

    public static Timestamp timestamp(int year, int month, int date, int hour, int minute, int second) {
        return Timestamp.ofEpochSecond(
                new DateTime(year, month, date, hour, minute, second, DateTimeZone.UTC).getMillis() / 1000
        );
    }

    public static Record record(Object... values) {
        List<Object> converted = new ArrayList<>(values.length);
        for (Object value : values) {
            if (value instanceof Integer) {
                converted.add(((Integer) value).longValue());
            } else {
                converted.add(value);
            }
        }
        return new Record(converted);
    }

    public static Column column(String name, Type type) {
        return new Column(-1, name, type);
    }

    public static Value json(String json) {
        return jsonParser.parse(json);
    }
}
