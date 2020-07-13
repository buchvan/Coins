package io.neolab.internship.coins.common.answer.interfaces;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.utils.Pair;

import java.util.List;

/**
 * Интерфейс для ответа на вопрос CATCH_CELL (захватить клетку)
 */
public interface ICatchCellAnswer {

    /**
     * Геттер для решения (по факту ответ на вопрос)
     * @return пару клетки и списка юнитов
     */
    Pair<Cell, List<Unit>> getResolution();
}
