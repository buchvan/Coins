package io.neolab.internship.coins.common.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.server.game.board.Board;

import java.io.IOException;

public class BoardDeserializer extends JsonDeserializer<Board> {
    @Override
    public Board deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonParser, Board.class);
    }
}
