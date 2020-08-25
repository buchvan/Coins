package io.neolab.internship.coins.common.serialization.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.neolab.internship.coins.server.game.board.Cell;

import java.io.IOException;

public class CellSerializer extends JsonSerializer<Cell> {
    @Override
    public void serialize(final Cell cell, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeFieldName(mapper.writeValueAsString(cell));
    }
}
