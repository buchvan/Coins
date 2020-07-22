package io.neolab.internship.coins.common.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.ServerMessage;

public class Communication {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serializeServerMessage(final ServerMessage serverMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(serverMessage);
    }

    public static ServerMessage deserializeServerMessage(final String json) throws JsonProcessingException {
        return mapper.readValue(json, ServerMessage.class);
    }

    public static String serializeAnswer(final Answer answer) throws JsonProcessingException {
        return mapper.writeValueAsString(answer);
    }

    public static Answer deserializeAnswer(final String json) throws JsonProcessingException {
        return mapper.readValue(json, Answer.class);
    }
}
