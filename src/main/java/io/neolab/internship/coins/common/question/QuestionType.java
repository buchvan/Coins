package io.neolab.internship.coins.common.question;

public enum QuestionType {
    /*
      Возможно, появятся новые типы вопросов
     */
    /**
     * Инициализировать игру
     */
    INIT_GAME,

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
     * Выбрать новую расу
     */
    CHOOSE_RACE,

    /**
     * Сменить расу
     */
    CHANGE_RACE
}
