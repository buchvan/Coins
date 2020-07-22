package io.neolab.internship.coins.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.neolab.internship.coins.common.answer.*;
import io.neolab.internship.coins.common.question.GameOverMessage;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.question.ServerMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Модуль сериализации-десериализации
 */
public class Communication {
    private static final @NotNull ObjectMapper mapper = new ObjectMapper();

    /**
     * Сериализация вопроса
     *
     * @param serverMessage - сообщение от сервера
     * @return json-строку - сериализованное сообщение
     * @throws JsonProcessingException, если writeValueAsString this throws
     */
    public static @NotNull String serializeServerMessage(final @NotNull ServerMessage serverMessage) throws JsonProcessingException {
        return mapper.writeValueAsString(serverMessage);
    }

    /**
     * Десериализация вопроса игроку
     *
     * @param json - json-строка - сериализованный вопрос
     * @return вопрос игроку
     * @throws JsonProcessingException, если readValue this throws
     */
    public static @NotNull ServerMessage deserializeServerMessage(final @NotNull String json) throws JsonProcessingException {
        return mapper.readValue(json, ServerMessage.class);
    }

    /**
     * Десериализация вопроса игроку
     *
     * @param json - json-строка - сериализованный вопрос
     * @return вопрос игроку
     * @throws JsonProcessingException, если readValue this throws
     */
    public static @NotNull PlayerQuestion deserializePlayerQuestion(final @NotNull String json)
            throws JsonProcessingException {
        return mapper.readValue(json, PlayerQuestion.class);
    }

    /**
     * Десериализация вопроса в конце игры
     *
     * @param json - json-строка - сериализованный вопрос
     * @return вопрос в конце игры
     * @throws JsonProcessingException, если readValue this throws
     */
    public static @NotNull GameOverMessage deserializeGameOverQuestion(final @NotNull String json)
            throws JsonProcessingException {
        return mapper.readValue(json, GameOverMessage.class);
    }

    /**
     * Сериализация ответа
     *
     * @param answer - ответ
     * @return json-строку - сериализованный ответ
     * @throws JsonProcessingException, если writeValueAsString this throws
     */
    public static @NotNull String serializeAnswer(final @NotNull Answer answer) throws JsonProcessingException {
        return mapper.writeValueAsString(answer);
    }

    /**
     * Десериализация ответа на вопрос захвата клетки
     *
     * @param json - json-строка - сериализованный ответ на вопрос захвата клетки
     * @return ответ на вопрос захвата клетки
     * @throws JsonProcessingException, если readValue this throws
     */
    public static @NotNull CatchCellAnswer deserializeCatchCellAnswer(final @NotNull String json)
            throws JsonProcessingException {
        return mapper.readValue(json, CatchCellAnswer.class);
    }

    /**
     * Десериализация ответа на вопрос смены расы
     *
     * @param json - json-строка - сериализованный ответ на вопрос смены расы
     * @return ответ на вопрос смены расы
     * @throws JsonProcessingException, если readValue this throws
     */
    public static @NotNull ChangeRaceAnswer deserializeChangeRaceAnswer(final @NotNull String json)
            throws JsonProcessingException {
        return mapper.readValue(json, ChangeRaceAnswer.class);
    }

    /**
     * Десериализация ответа на вопрос ухода в упадок
     *
     * @param json - json-строка - сериализованный ответ на вопрос ухода в упадок
     * @return ответ на вопрос ухода в упадок
     * @throws JsonProcessingException, если readValue this throws
     */
    public static @NotNull DeclineRaceAnswer deserializeDeclineRaceAnswer(final @NotNull String json)
            throws JsonProcessingException {
        return mapper.readValue(json, DeclineRaceAnswer.class);
    }

    /**
     * Десериализация ответа на вопрос распределения юнитов
     *
     * @param json - json-строка - сериализованный ответ на вопрос распределения юнитов
     * @return ответ на вопрос распределения юнитов
     * @throws JsonProcessingException, если readValue this throws
     */
    public static @NotNull DistributionUnitsAnswer deserializeDistributionUnitsAnswer(final @NotNull String json)
            throws JsonProcessingException {
        return mapper.readValue(json, DistributionUnitsAnswer.class);
    }
}
