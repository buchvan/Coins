package io.neolab.internship.coins.common.serialization.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.neolab.internship.coins.server.game.board.Position;

import java.io.IOException;

public class PositionSerializer extends JsonSerializer<Position> {
    @Override
    public void serialize(final Position position, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeFieldName(mapper.writeValueAsString(position));
    }
}
