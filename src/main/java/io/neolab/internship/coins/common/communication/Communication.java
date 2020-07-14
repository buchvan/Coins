package io.neolab.internship.coins.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.server.game.IGame;

public class Communication {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serialize(final IGame game) throws JsonProcessingException {
        return mapper.writeValueAsString(game);
    }

    public static IGame deserialize(final String json) throws JsonProcessingException {
        return mapper.readValue(json, IGame.class);
    }
}
