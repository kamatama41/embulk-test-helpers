package org.embulk.test;

import org.embulk.spi.time.Timestamp;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DateUtil {
    public static Timestamp timestamp(int year, int month, int date) {
        return timestamp(year, month, date, 0, 0, 0);
    }

    public static Timestamp timestamp(int year, int month, int date, int hour, int minute, int second) {
        return Timestamp.ofEpochSecond(
                new DateTime(year, month, date, hour, minute, second, DateTimeZone.UTC).getMillis() / 1000
        );
    }
}
