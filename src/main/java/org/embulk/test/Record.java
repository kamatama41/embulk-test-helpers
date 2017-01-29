package org.embulk.test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Record {
    private final List<Object> values;

    Record(List<Object> values) {
        this.values = values;
    }

    public static Record record(Object... values) {
        return new Record(Arrays.asList(values));
    }

    List<Object> getValues() {
        return values;
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
}
