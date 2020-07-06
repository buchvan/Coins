package io.neolab.internship.coins.answer;


import io.neolab.internship.coins.Position;
import io.neolab.internship.coins.server.board.Unit;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Ответ на вопрос DISTRIBUTION_UNITS (распределение войск в конце хода)
 */
public class DistributionUnitsAnswer extends Answer implements IDistributionUnitsAnswer {

    private final Map<Position, List<Unit>> resolutions; // позиция клетки в список юнитов

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
