package io.neolab.internship.coins.answer;

/**
 * Интерфейс для ответа на вопрос END_TURN_CHOICE (выбор в конце хода)
 */
public interface IEndTurnChoiceAnswer {

    /**
     * Геттер для решения (по факту ответ на вопрос)
     * @return выбор в конце хода
     */
    EndTurnChoice getEndTurnChoice();
}