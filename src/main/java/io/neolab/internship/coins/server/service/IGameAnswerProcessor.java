package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.GameQuestion;
import io.neolab.internship.coins.exceptions.CoinsException;

public interface IGameAnswerProcessor {
    void process(final GameQuestion gameQuestion, final Answer answer) throws CoinsException;
}
