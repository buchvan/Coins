package io.neolab.internship.coins.server;

import io.neolab.internship.coins.answer.Answer;

/**
 * Интерфейс для валидатора
 */
public interface IValidator {

    /**
     * Основной метод валидатора
     * @param answer - ответ, который необходимо проверить на валидность
     * @return true - если ответ валидный, false - иначе
     */
    boolean isValid( /*TODO Question question, */ Answer answer);
}
