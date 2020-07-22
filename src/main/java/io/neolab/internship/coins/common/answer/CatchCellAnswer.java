package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.utils.Pair;

import java.util.List;
import java.util.Objects;

public class CatchCellAnswer extends Answer {
    @JsonProperty
    private final Pair<Position, List<Unit>> resolution;

    @JsonCreator
    public CatchCellAnswer(@JsonProperty("resolution") final Pair<Position, List<Unit>> resolution) {
        this.resolution = resolution;
    }

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
