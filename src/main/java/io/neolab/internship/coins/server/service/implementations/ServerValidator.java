package io.neolab.internship.coins.server.service.implementations;

import io.neolab.internship.coins.answer.Answer;
import io.neolab.internship.coins.question.Question;
import io.neolab.internship.coins.server.IServer;
import io.neolab.internship.coins.server.service.interfaces.IValidator;

/**
 * Валидатор ответов клиента для сервера
 */
public class ServerValidator implements IValidator {
    private final IServer server;

    public ServerValidator(IServer server) {
        this.server = server;
    }

    @Override
    public boolean isValid(Question question, Answer answer) {
        return false;
    }
}
