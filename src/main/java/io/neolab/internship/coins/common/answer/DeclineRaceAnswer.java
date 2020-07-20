package io.neolab.internship.coins.common.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class DeclineRaceAnswer extends Answer {
    private final boolean declineRace;

    @JsonCreator
    public DeclineRaceAnswer(@JsonProperty("declineRace") final boolean declineRace) {
        this.declineRace = declineRace;
    }

    public boolean isDeclineRace() {
        return declineRace;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DeclineRaceAnswer)) return false;
        final DeclineRaceAnswer that = (DeclineRaceAnswer) o;
        return isDeclineRace() == that.isDeclineRace();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDeclineRace());
    }
}
