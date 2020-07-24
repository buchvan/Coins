package io.neolab.internship.coins.client;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.question.PlayerQuestion;
import io.neolab.internship.coins.common.answer.ClientMessage;
import io.neolab.internship.coins.common.question.ServerMessage;
import io.neolab.internship.coins.exceptions.CoinsException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IClient {
    /**
     * Взять ответ на вопрос
     * @param question - вопрос
     * @return ответ на вопрос
     * @throws CoinsException в случае неизвестного типа вопроса
     */
    @NotNull Answer getAnswer(final @NotNull PlayerQuestion question) throws CoinsException;

    /**
     * Прочитать сообщение от сервера
     * @param message - сообщение
     * @throws CoinsException в случае неизвестного типа сообщения
     */
    void readMessage(final @NotNull ServerMessage message) throws CoinsException, IOException;
}
