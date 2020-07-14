package io.neolab.internship.coins.server.validation;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.implementations.ChooseRaceAnswer;
import io.neolab.internship.coins.common.answer.implementations.DeclineRaceAnswer;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.Race;

import java.util.List;

public interface IGameValidator {
    /**
     * Проверка на пустой ответ
     * @param answer ответ, который нужно проверить
     * @throws CoinsException в случае пустого ответа выбрасывается исключение с кодом ошибки EMPTY_ANSWER
     */
    static void checkIfAnswerEmpty(final Answer answer) throws CoinsException {
        if (answer == null) {
            throw new CoinsException(ErrorCode.EMPTY_ANSWER);
        }
    }

    /**
     * Проверка ответа, отвечающего за выбор расы в начале игры
     * @param answer ответ, который нужно проверить
     * @param currentPlayerRace текущая раса игрока
     * @param racesPool пул доступных рас
     * @throws CoinsException при совпадении новых и текущей расы - SAME_RACES, расы нет в пуле - UNAVAILABLE_NEW_RACE,
     * пустой ответ - EMPTY_ANSWER
     */
    static void validateChooseRaceAnswer(final ChooseRaceAnswer answer,
                                         final List<Race> racesPool,
                                         final Race currentPlayerRace) throws CoinsException {
        final Race newRace = answer.getNewRace();
        checkIfAnswerEmpty(answer);
        if (!racesPool.contains(newRace)) {
            throw new CoinsException(ErrorCode.UNAVAILABLE_NEW_RACE);
        }
        if (currentPlayerRace == newRace) {
            throw new CoinsException(ErrorCode.SAME_RACES);
        }

    }

    /**
     * Проверка ответа, отвечающего за уход игрока в упадок
     * @param answer ответ, который нужно проверить
     * @throws CoinsException пустой ответ - EMPTY_ANSWER
     */
    static void validateDeclineRaceAnswer(final DeclineRaceAnswer answer) throws CoinsException {
        checkIfAnswerEmpty(answer);
    }
}
