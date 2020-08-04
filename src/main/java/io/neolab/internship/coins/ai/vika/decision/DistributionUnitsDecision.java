package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DistributionUnitsDecision extends Decision {

    private final @NotNull Map<Position, List<Unit>> resolutions;

    public DistributionUnitsDecision(@NotNull final Map<Position, List<Unit>> resolutions) {
        super(DecisionType.DISTRIBUTION_UNITS);
        this.resolutions = resolutions;
    }

    public @NotNull Map<Position, List<Unit>> getResolutions() {
        return resolutions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DistributionUnitsDecision)) return false;
        if (!super.equals(o)) return false;
        final DistributionUnitsDecision that = (DistributionUnitsDecision) o;
        return getResolutions().equals(that.getResolutions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getResolutions());
    }

    @Override
    public String toString() {
        return "DistributionUnitsDecision{" +
                "resolutions=" + resolutions +
                '}';
    }
}
