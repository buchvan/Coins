package io.neolab.internship.coins.common.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.neolab.internship.coins.server.game.board.Board;

import java.io.IOException;

public class BoardSerializer extends JsonSerializer<Board> {
    @Override
    public void serialize(final Board board, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectFieldStart("positionToCellMap");
        jsonGenerator.writeArrayFieldStart("positions:");
        jsonGenerator.writeEndObject();
    }
}
