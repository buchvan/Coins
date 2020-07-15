package io.neolab.internship.coins.common.answer;

import java.util.Objects;

public class DeclineRaceAnswer extends Answer {
    private final boolean declineRace;

    public DeclineRaceAnswer(final boolean declineRace) {
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
}
