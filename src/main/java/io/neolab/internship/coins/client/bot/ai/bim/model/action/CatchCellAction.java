package io.neolab.internship.coins.client.bot.ai.bim.model.action;

import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CatchCellAction extends Action {
    private final @Nullable Pair<Position, List<Unit>> resolution;

    public CatchCellAction(final @Nullable Pair<Position, List<Unit>> resolution) {
        super(ActionType.CATCH_CELL);
        this.resolution = resolution;
    }

    public @Nullable Pair<Position, List<Unit>> getResolution() {
        return resolution;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CatchCellAction that = (CatchCellAction) o;
        return Objects.equals(resolution, that.resolution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolution);
    }

    @Override
    public String toString() {
        return "CatchCellAction{" +
                "resolution=" + resolution +
                '}';
    }
}
