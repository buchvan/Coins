package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class CatchCellAnswer extends Answer {
    private final @Nullable Pair<Position, List<Unit>> resolution;

    @JsonCreator
    public CatchCellAnswer(@Nullable @JsonProperty("resolution") final Pair<Position, List<Unit>> resolution) {
        this.resolution = resolution;
    }

    public @Nullable Pair<Position, List<Unit>> getResolution() {
        return resolution;
    }

    @Contract(value = "null -> false", pure = true)
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
