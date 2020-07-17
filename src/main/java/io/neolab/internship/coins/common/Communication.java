package io.neolab.internship.coins.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.Question;

public class Communication {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serializeQuestion(final Question question) throws JsonProcessingException {
        return mapper.writeValueAsString(question);
    }

    public static Question deserializeQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, Question.class);
    }

    public static PlayerQuestion deserializeGameQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, PlayerQuestion.class);
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
