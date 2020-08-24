package io.neolab.internship.coins.common.message.client.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.message.client.ClientMessageType;
import io.neolab.internship.coins.common.serialization.deserialize.PositionKeyDeserializer;
import io.neolab.internship.coins.common.serialization.serialize.PositionSerializer;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.player.Unit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DistributionUnitsAnswer extends Answer {

    @JsonProperty
    @JsonSerialize(keyUsing = PositionSerializer.class)
    @JsonDeserialize(keyUsing = PositionKeyDeserializer.class)
    private final @NotNull Map<Position, List<Unit>> resolutions;

    @JsonCreator
    public DistributionUnitsAnswer(@NotNull @JsonProperty("resolutions") final Map<Position, List<Unit>> resolutions) {
        super(ClientMessageType.GAME_ANSWER);
        this.resolutions = resolutions;
    }

    public @NotNull Map<Position, List<Unit>> getResolutions() {
        return resolutions;
    }

    @Contract(value = "null -> false", pure = true)
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
