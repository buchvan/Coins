package io.neolab.internship.coins.common.answer.interfaces;

import io.neolab.internship.coins.server.game.Race;

/**
 * Интерфейс для ответа на вопрос CHOOSE_RACE (выбрать новую расу)
 */
public interface IChooseRaceAnswer {

    /**
     * Геттер для решения (по факту ответ на вопрос)
     * @return новую расу
     */
    Race getNewRace();
}
