package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.Race;

import java.util.Objects;

public class ChooseRaceAnswer extends Answer {
    private final Race newRace;

    @JsonCreator
    public ChooseRaceAnswer(@JsonProperty("newRace") final Race newRace) {
        this.newRace = newRace;
    }

    public Race getNewRace() {
        return newRace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ChooseRaceAnswer that = (ChooseRaceAnswer) o;
        return newRace == that.newRace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newRace);
    }

    @Override
    public String toString() {
        return "ChooseRaceAnswer{" +
                "newRace=" + newRace +
                '}';
    }
}
