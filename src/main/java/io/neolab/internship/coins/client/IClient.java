package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.answer.ClientMessage;
import io.neolab.internship.coins.common.question.ServerMessage;
import io.neolab.internship.coins.exceptions.CoinsException;

import java.io.IOException;

public interface IClient {
    /**
     * Взять ответ на вопрос
     * @param question - вопрос
     * @return ответ на вопрос
     * @throws CoinsException в случае неизвестного типа вопроса
     */
    Answer getAnswer(final PlayerQuestion question) throws CoinsException;

    /**
     * Взять ответ на вопрос
     * @param message - сообщение
     * @throws CoinsException в случае неизвестного типа сообщения
     */
    void readMessage(final ServerMessage message) throws CoinsException, IOException;
}
