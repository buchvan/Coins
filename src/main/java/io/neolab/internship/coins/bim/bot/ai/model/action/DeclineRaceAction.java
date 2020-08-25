package io.neolab.internship.coins.bim.bot.ai.model.action;

import java.util.Objects;

public class DeclineRaceAction extends Action {
    private final boolean declineRace;

    public DeclineRaceAction(final boolean declineRace) {
        super(ActionType.DECLINE_RACE);
        this.declineRace = declineRace;
    }

    public boolean isDeclineRace() {
        return declineRace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DeclineRaceAction that = (DeclineRaceAction) o;
        return declineRace == that.declineRace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(declineRace);
    }

    @Override
    public String toString() {
        return "DeclineRaceAction{" +
                "declineRace=" + declineRace +
                '}';
    }
}
