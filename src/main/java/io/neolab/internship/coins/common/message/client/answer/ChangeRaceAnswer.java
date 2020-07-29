package io.neolab.internship.coins.common.message.client.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.common.message.client.ClientMessageType;
import io.neolab.internship.coins.server.game.player.Race;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChangeRaceAnswer extends Answer {
    @JsonProperty
    private final @NotNull Race newRace;

    @JsonCreator
    public ChangeRaceAnswer(@NotNull @JsonProperty("newRace") final Race newRace) {
        super(ClientMessageType.GAME_ANSWER);
        this.newRace = newRace;
    }

    public @NotNull Race getNewRace() {
        return newRace;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ChangeRaceAnswer that = (ChangeRaceAnswer) o;
        return newRace == that.newRace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newRace);
    }

    @Override
    public String toString() {
        return "ChangeRaceAnswer{" +
                "newRace=" + newRace +
                '}';
    }
}
