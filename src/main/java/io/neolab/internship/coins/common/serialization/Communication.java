package io.neolab.internship.coins.common.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.message.client.ClientMessage;
import io.neolab.internship.coins.common.message.server.ServerMessage;

public class Communication {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serializeServerMessage(final ServerMessage serverMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(serverMessage);
    }

    public static ServerMessage deserializeServerMessage(final String json) throws JsonProcessingException {
        return mapper.readValue(json, ServerMessage.class);
    }

    public static String serializeClientMessage(final ClientMessage clientMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(clientMessage);
    }

    public static ClientMessage deserializeClientMessage(final String json) throws JsonProcessingException {
        return mapper.readValue(json, ClientMessage.class);
    }
}
