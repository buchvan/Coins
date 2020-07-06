package io.neolab.internship.coins.server;

import io.neolab.internship.coins.answer.Answer;

/**
 * Интерфейс для обработчика ответов
 */
public interface IAnswerProcessor {
    /**
     * Основной метод обработчика ответов
     * @param answer - ответ, который необходимо обработать
     */
    void processor(/*TODO Question question, */ Answer answer);
}
