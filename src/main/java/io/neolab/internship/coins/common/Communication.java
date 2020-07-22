package io.neolab.internship.coins.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.GameOverMessage;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.ServerMessage;

/**
 * Модуль сериализации-десериализации
 */
public class Communication {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Сериализация вопроса
     *
     * @param question - вопрос
     * @return json-строку - сериализованный вопрос
     * @throws JsonProcessingException, если writeValueAsString this throws
     */
    public static String serializeQuestion(final ServerMessage serverMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(serverMessage);
    }

    public static ServerMessage deserializeQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, ServerMessage.class);
    }

    /**
     * Десериализация вопроса игроку
     *
     * @param json - json-строка - сериализованный вопрос
     * @return вопрос игроку
     * @throws JsonProcessingException, если readValue this throws
     */
    public static PlayerQuestion deserializePlayerQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, PlayerQuestion.class);
    }

    /**
     * Десериализация сообщения игроку в конце игры
     *
     * @param json - json-строка - сериализованный вопрос
     * @return вопрос в конце игры
     * @throws JsonProcessingException, если readValue this throws
     */
    public static GameOverMessage deserializeGameOverQuestion(final String json) throws JsonProcessingException {
        return mapper.readValue(json, GameOverMessage.class);
    }

    /**
     * Сериализация ответа
     *
     * @param answer - ответ
     * @return json-строку - сериализованный ответ
     * @throws JsonProcessingException, если writeValueAsString this throws
     */
    public static String serializeAnswer(final Answer answer) throws JsonProcessingException {
        return mapper.writeValueAsString(answer);
    }

    /**
     * Десериализация ответа на вопрос захвата клетки
     *
     * @param json - json-строка - сериализованный ответ на вопрос захвата клетки
     * @return ответ на вопрос захвата клетки
     * @throws JsonProcessingException, если readValue this throws
     */
    public static CatchCellAnswer deserializeCatchCellAnswer(final String json) throws JsonProcessingException {
        return mapper.readValue(json, CatchCellAnswer.class);
    }

    /**
     * Десериализация ответа на вопрос смены расы
     *
     * @param json - json-строка - сериализованный ответ на вопрос смены расы
     * @return ответ на вопрос смены расы
     * @throws JsonProcessingException, если readValue this throws
     */
    public static ChangeRaceAnswer deserializeChangeRaceAnswer(final String json) throws JsonProcessingException {
        return mapper.readValue(json, ChangeRaceAnswer.class);
    }

    /**
     * Десериализация ответа на вопрос ухода в упадок
     *
     * @param json - json-строка - сериализованный ответ на вопрос ухода в упадок
     * @return ответ на вопрос ухода в упадок
     * @throws JsonProcessingException, если readValue this throws
     */
    public static DeclineRaceAnswer deserializeDeclineRaceAnswer(final String json) throws JsonProcessingException {
        return mapper.readValue(json, DeclineRaceAnswer.class);
    }

    /**
     * Десериализация ответа на вопрос распределения юнитов
     *
     * @param json - json-строка - сериализованный ответ на вопрос распределения юнитов
     * @return ответ на вопрос распределения юнитов
     * @throws JsonProcessingException, если readValue this throws
     */
    public static DistributionUnitsAnswer deserializeDistributionUnitsAnswer(final String json)
            throws JsonProcessingException {
        return mapper.readValue(json, DistributionUnitsAnswer.class);
    }
}
