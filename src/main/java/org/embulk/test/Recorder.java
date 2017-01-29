package org.embulk.test;

import com.google.common.collect.ImmutableList;
import org.embulk.spi.Column;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;
import org.embulk.spi.util.Pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Repository of {@link Record}
 */
class Recorder {
    private List<Record> records = new ArrayList<>();
    private Schema schema;

    Recorder() {
    }

    List<Record> getRecords() {
        return new ArrayList<>(this.records);
    }

    synchronized void addRecord(PageReader reader) {
        final ImmutableList.Builder<Object> values = ImmutableList.builder();
        reader.getSchema().visitColumns(new Pages.ObjectColumnVisitor(reader) {
            @Override
            public void visit(Column column, Object value) {
                values.add(value);
            }
        });
        this.records.add(new Record(values.build()));
    }

    Schema getSchema() {
        return schema;
    }

    synchronized void setSchema(Schema schema) {
        this.schema = schema;
    }

    void clear() {
        this.records = new ArrayList<>();
        this.schema = null;
    }

    void assertRecords(Record... records) {
        Set<Record> actual = new HashSet<>();
        for (Record record : getRecords()) {
            actual.add(record);
        }

        Set<Record> expected = new HashSet<>();
        Collections.addAll(expected, records);

        assertThat(actual, is(expected));
    }
}
