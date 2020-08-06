package io.neolab.internship.coins.client.bot.ai.bim.model.action;

import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DistributionUnitsAction extends Action {
    private final @NotNull Map<Position, List<Unit>> resolutions;

    public DistributionUnitsAction(final @NotNull Map<Position, List<Unit>> resolutions) {
        super(ActionType.DISTRIBUTION_UNITS);
        this.resolutions = resolutions;
    }

    public @NotNull Map<Position, List<Unit>> getResolutions() {
        return resolutions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DistributionUnitsAction that = (DistributionUnitsAction) o;
        return Objects.equals(resolutions, that.resolutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolutions);
    }

    @Override
    public String toString() {
        return "DistributionUnitsAction{" +
                "resolutions=" + resolutions +
                '}';
    }
}
