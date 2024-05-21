package ru.yandex.practicum.filmorate.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {

    @Override
    public Duration read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String durationString = reader.nextString();
        try {
            long durationLong = Long.parseLong(durationString);
            return Duration.ofMinutes(durationLong);
        } catch (Exception e) {
            //log
        }
        return null;
    }

    @Override
    public void write(JsonWriter writer, Duration duration) throws IOException {
        if (duration == null) {
            writer.nullValue();
            return;
        }
        writer.value(duration.toMinutes());
    }
}