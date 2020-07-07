package io.neolab.internship.coins.answer.interfaces;

import io.neolab.internship.coins.server.board.Position;
import io.neolab.internship.coins.server.board.Unit;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для ответа на вопрос DISTRIBUTION_UNITS (распределение войск в конце хода)
 */
public interface IDistributionUnitsAnswer {

    /**
     * Геттер для решений (по факту ответ на вопрос)
     * @return отображение позиции клетки в список юнитов
     */
    Map<Position, List<Unit>> getResolutions();
}
