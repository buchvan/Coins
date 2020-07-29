package io.neolab.internship.coins.common.message.server.question;

public enum PlayerQuestionType {

    /**
     * Захватить клетку
     */
    CATCH_CELL,

    /**
     * Распределить войска в конце хода
     */
    DISTRIBUTION_UNITS,

    /**
     * Отправить расу в упадок
     */
    DECLINE_RACE,

    /**
     * Сменить расу
     */
    CHANGE_RACE,

    /* Неверный тип вопроса. Используется только для тестирования обработки всех корректных типов */
    WRONG_QUESTION_TYPE,
    ;
}
