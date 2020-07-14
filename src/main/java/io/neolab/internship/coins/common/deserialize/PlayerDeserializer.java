package io.neolab.internship.coins.common.deserialize;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.server.game.Player;

import java.io.IOException;

public class PlayerDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(final String s, final DeserializationContext deserializationContext)
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(s, Player.class);
    }
}
