package io.neolab.internship.coins.common.answer.implementations;

import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.board.Unit;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.interfaces.ICatchCellAnswer;

import java.util.List;
import java.util.Objects;

public class CatchCellAnswer extends Answer implements ICatchCellAnswer {
    private final Pair<Position, List<Unit>> resolution;

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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CatchCellAnswer that = (CatchCellAnswer) o;
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
