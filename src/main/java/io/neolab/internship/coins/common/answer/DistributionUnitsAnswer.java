package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.deserialize.PositionDeserializer;
import io.neolab.internship.coins.common.serialize.PositionSerializer;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Unit;

import java.util.*;

public class DistributionUnitsAnswer extends Answer {

    @JsonSerialize(keyUsing = PositionSerializer.class)
    @JsonDeserialize(keyUsing = PositionDeserializer.class)
    private final Map<Position, List<Unit>> resolutions;

    public DistributionUnitsAnswer() {
        this(null);
    }

    @JsonCreator
    public DistributionUnitsAnswer(@JsonProperty("resolutions") final Map<Position, List<Unit>> resolutions) {
        this.resolutions = resolutions;
    }

    public Map<Position, List<Unit>> getResolutions() {
        return resolutions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DistributionUnitsAnswer that = (DistributionUnitsAnswer) o;
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
