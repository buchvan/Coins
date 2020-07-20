package io.neolab.internship.coins.common.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.neolab.internship.coins.server.game.player.Player;

import java.io.IOException;

public class PlayerSerializer extends JsonSerializer<Player> {
    @Override
    public void serialize(final Player player, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeFieldName(mapper.writeValueAsString(player));
    }
}
