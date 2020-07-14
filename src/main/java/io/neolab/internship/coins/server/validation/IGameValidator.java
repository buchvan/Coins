package io.neolab.internship.coins.server.validation;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.implementations.ChooseRaceAnswer;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.Race;

import java.util.List;

public interface IGameValidator {
    static void checkIfAnswerEmpty(final Answer answer) throws CoinsException {
        if (answer == null) {
            throw new CoinsException(ErrorCode.EMPTY_ANSWER);
        }
    }

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
}
