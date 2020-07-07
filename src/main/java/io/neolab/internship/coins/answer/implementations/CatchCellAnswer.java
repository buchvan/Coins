package io.neolab.internship.coins.answer.implementations;

import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.server.board.Position;
import io.neolab.internship.coins.answer.Answer;
import io.neolab.internship.coins.answer.interfaces.ICatchCellAnswer;
import io.neolab.internship.coins.server.board.Unit;

import java.util.List;
import java.util.Objects;

/**
 * Ответ на вопрос CATCH_CELL (захватить клетку)
 */
public class CatchCellAnswer extends Answer implements ICatchCellAnswer {
    private final Pair<Position, List<Unit>> resolution; // позиция клетки и список юнитов

    public CatchCellAnswer() {
        this.resolution = null;
    }

    public CatchCellAnswer(final Pair<Position, List<Unit>> resolution) {
        this.resolution = resolution;
    }

    @Override
    public Pair<Position, List<Unit>> getResolution() {
        return resolution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatchCellAnswer that = (CatchCellAnswer) o;
        return Objects.equals(resolution, that.resolution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolution);
    }

    @Override
    public String toString() {
        return "CatchCellAnswer{" +
                "resolution=" + resolution +
                '}';
    }
}
