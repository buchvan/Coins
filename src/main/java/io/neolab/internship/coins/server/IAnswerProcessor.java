package io.neolab.internship.coins.server;

import io.neolab.internship.coins.answer.Answer;
import io.neolab.internship.coins.question.Question;

/**
 * Интерфейс для обработчика ответов
 */
public interface IAnswerProcessor {
    /**
     * Основной метод обработчика ответов
     * @param answer - ответ, который необходимо обработать
     */
    void processor(Question question, Answer answer);
}
