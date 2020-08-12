package io.neolab.internship.coins.ai.vika.decision.model;

import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CatchCellDecision extends Decision {
    private final @Nullable Pair<Position, List<Unit>> resolution;

    public CatchCellDecision(@Nullable final Pair<Position, List<Unit>> resolution) {
        super(DecisionType.CATCH_CELL);
        this.resolution = resolution;
    }

    public @Nullable Pair<Position, List<Unit>> getDecision() {
        return resolution;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CatchCellDecision)) return false;
        if (!super.equals(o)) return false;
        final CatchCellDecision that = (CatchCellDecision) o;
        return Objects.equals(getDecision(), that.getDecision());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDecision());
    }

    @Override
    public String toString() {
        return "CatchCellDecision{" +
                "resolution=" + resolution +
                '}';
    }
}
