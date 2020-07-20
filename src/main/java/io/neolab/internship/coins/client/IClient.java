package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.Question;
import io.neolab.internship.coins.exceptions.CoinsException;

public interface IClient {
    Answer getAnswer(final Question question) throws CoinsException;
}
