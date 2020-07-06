package io.neolab.internship.coins.server;

import io.neolab.internship.coins.answer.Answer;
import io.neolab.internship.coins.question.Question;

/**
 * Интерфейс для валидатора
 */
public interface IValidator {

    /**
     * Основной метод валидатора
     * @param answer - ответ, который необходимо проверить на валидность
     * @return true - если ответ валидный, false - иначе
     */
    boolean isValid(Question question, Answer answer);
}
