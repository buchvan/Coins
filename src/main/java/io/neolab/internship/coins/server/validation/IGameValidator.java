package io.neolab.internship.coins.server.validation;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;

public interface IGameValidator {
    static void validate(final Answer answer) throws CoinsException {
        if(answer == null) {
            throw new CoinsException(ErrorCode.EMPTY_ANSWER);
        }
    }
}
