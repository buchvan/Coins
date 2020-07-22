package io.neolab.internship.coins.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.GameOverMessage;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.ServerMessage;

public class Communication {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serializeQuestion(final ServerMessage serverMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(serverMessage);
    }

    public static ServerMessage deserializeQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, ServerMessage.class);
    }

    public static PlayerQuestion deserializePlayerQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, PlayerQuestion.class);
    }

    public static GameOverMessage deserializeGameOverQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, GameOverMessage.class);
    }

    public static String serializeAnswer(final Answer answer) throws JsonProcessingException {
        return mapper.writeValueAsString(answer);
    }

    public static CatchCellAnswer deserializeCatchCellAnswer(final String json) throws JsonProcessingException {
        return mapper.readValue(json, CatchCellAnswer.class);
    }

    public static ChangeRaceAnswer deserializeChangeRaceAnswer(final String json) throws JsonProcessingException {
        return mapper.readValue(json, ChangeRaceAnswer.class);
    }

    public static DeclineRaceAnswer deserializeDeclineRaceAnswer(final String json) throws JsonProcessingException {
        return mapper.readValue(json, DeclineRaceAnswer.class);
    }

    public static DistributionUnitsAnswer deserializeDistributionUnitsAnswer(final String json)
            throws JsonProcessingException {
        return mapper.readValue(json, DistributionUnitsAnswer.class);
    }
}
