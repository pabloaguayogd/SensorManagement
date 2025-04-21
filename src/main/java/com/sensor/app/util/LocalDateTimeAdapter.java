package com.sensor.app.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        out.value(value != null ? value.toString() : null);
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        return in.hasNext() ? LocalDateTime.parse(in.nextString()) : null;
    }
}