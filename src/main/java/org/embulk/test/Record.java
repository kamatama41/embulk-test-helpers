package org.embulk.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Record {
    private final List<Object> values;

    Record(List<Object> values) {
        this.values = values;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(values, record.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
