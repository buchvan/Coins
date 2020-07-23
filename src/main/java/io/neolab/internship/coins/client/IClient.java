package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.ServerMessage;
import io.neolab.internship.coins.exceptions.CoinsException;

public interface IClient {
    /**
     * Взять ответ на вопрос
     *
     * @param question - вопрос
     * @return ответ на вопрос
     * @throws CoinsException в случае неизвестного типа вопроса
     */
    Answer getAnswer(final ServerMessage question) throws CoinsException;
}
