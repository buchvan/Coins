package io.neolab.internship.coins.server.service.implementations;

import io.neolab.internship.coins.answer.Answer;
import io.neolab.internship.coins.question.Question;
import io.neolab.internship.coins.server.IServer;
import io.neolab.internship.coins.server.service.interfaces.IAnswerProcessor;

/**
 * Обработчик ответов клиента для сервера
 */
public class ServerAnswerProcessor implements IAnswerProcessor {
    private final IServer server;

    public ServerAnswerProcessor(IServer server) {
        this.server = server;
    }

    @Override
    public void process(Question question, Answer answer) {

    }
}
