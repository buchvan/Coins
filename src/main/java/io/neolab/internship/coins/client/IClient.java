package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.ServerMessage;
import io.neolab.internship.coins.exceptions.CoinsException;

public interface IClient {
    Answer getAnswer(final ServerMessage serverMessage) throws CoinsException;
}
