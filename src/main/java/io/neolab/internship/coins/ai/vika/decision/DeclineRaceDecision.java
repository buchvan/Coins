package io.neolab.internship.coins.ai.vika.decision;


import java.util.Objects;

public class DeclineRaceDecision extends Decision {

    private final boolean declineRace;

    public DeclineRaceDecision(final boolean declineRace) {
        super(DecisionType.DECLINE_RACE);
        this.declineRace = declineRace;
    }

    public boolean isDeclineRace() {
        return declineRace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DeclineRaceDecision)) return false;
        if (!super.equals(o)) return false;
        final DeclineRaceDecision that = (DeclineRaceDecision) o;
        return isDeclineRace() == that.isDeclineRace();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isDeclineRace());
    }

    @Override
    public String toString() {
        return "DeclineRaceDecision{" +
                "declineRace=" + declineRace +
                '}';
    }
}


