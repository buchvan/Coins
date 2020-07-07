package io.neolab.internship.coins.answer.interfaces;

import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.server.board.Position;
import io.neolab.internship.coins.server.board.Unit;

import java.util.List;

/**
 * Интерфейс для ответа на вопрос CATCH_CELL (захватить клетку)
 */
public interface ICatchCellAnswer {

    /**
     * Геттер для решения (по факту ответ на вопрос)
     * @return пару позиции клетки и списка юнитов
     */
    Pair<Position, List<Unit>> getResolution();
}
