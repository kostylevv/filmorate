package ru.yandex.practicum.filmorate.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String dateString = reader.nextString();
        return LocalDate.parse(dateString, dateFormatter);
    }

    @Override
    public void write(JsonWriter writer, LocalDate time) throws IOException {
        if (time == null) {
            writer.nullValue();
            return;
        }
        writer.value(time.format(dateFormatter));
    }
}