package io.neolab.internship.coins.common.answer.implementations;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.interfaces.IDistributionUnitsAnswer;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Position;

import java.util.*;

public class DistributionUnitsAnswer extends Answer implements IDistributionUnitsAnswer {
    private final Map<Position, List<Unit>> resolutions;

    public DistributionUnitsAnswer() {
        this.resolutions = new HashMap<>();
    }

    public DistributionUnitsAnswer(final Map<Position, List<Unit>> resolutions) {
        this.resolutions = resolutions;
    }

    @Override
    public Map<Position, List<Unit>> getResolutions() {
        return resolutions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistributionUnitsAnswer that = (DistributionUnitsAnswer) o;
        return Objects.equals(resolutions, that.resolutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolutions);
    }

    @Override
    public String toString() {
        return "DistributionUnitsAnswer{" +
                "resolutions=" + resolutions +
                '}';
    }
}
