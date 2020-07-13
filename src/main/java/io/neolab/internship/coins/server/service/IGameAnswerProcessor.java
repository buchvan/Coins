package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.exceptions.CoinsException;

public interface IGameAnswerProcessor {
    Answer process(Question question) throws CoinsException;
}
