package io.neolab.internship.coins.client;

import io.neolab.internship.coins.ai_vika.bot.exception.AIBotException;
import io.neolab.internship.coins.common.message.client.answer.Answer;
import io.neolab.internship.coins.common.message.server.question.PlayerQuestion;
import io.neolab.internship.coins.common.message.server.ServerMessage;
import io.neolab.internship.coins.exceptions.CoinsException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IClient {
    /**
     * Взять ответ на вопрос
     *
     * @param question - вопрос
     * @return ответ на вопрос
     * @throws CoinsException в случае неизвестного типа вопроса
     */
    @NotNull Answer getAnswer(final @NotNull PlayerQuestion question) throws CoinsException, AIBotException;

    /**
     * Обработать сообщение от сервера
     *
     * @param message - сообщение от сервера
     * @throws CoinsException в случае неизвестного типа сообщения
     * @throws IOException    при ошибке отправки ответа серверу
     */
    void processMessage(final @NotNull ServerMessage message) throws CoinsException, IOException, AIBotException;
}
