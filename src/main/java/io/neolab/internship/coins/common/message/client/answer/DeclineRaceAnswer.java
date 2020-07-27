package io.neolab.internship.coins.common.message.client.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.common.message.client.ClientMessageType;

import java.util.Objects;

public class DeclineRaceAnswer extends Answer {
    @JsonProperty
    private final boolean declineRace;

    @JsonCreator
    public DeclineRaceAnswer(@JsonProperty("declineRace") final boolean declineRace) {
        super(ClientMessageType.GAME_ANSWER);
        this.declineRace = declineRace;
    }

    public boolean isDeclineRace() {
        return declineRace;
    }

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

    @Override
    public String toString() {
        return "DeclineRaceAnswer{" +
                "declineRace=" + declineRace +
                '}';
    }
}
