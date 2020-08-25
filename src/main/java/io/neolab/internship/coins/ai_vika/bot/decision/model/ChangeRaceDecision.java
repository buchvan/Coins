package io.neolab.internship.coins.ai_vika.bot.decision.model;

import io.neolab.internship.coins.server.game.player.Race;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChangeRaceDecision extends Decision {

    private final @NotNull Race newRace;

    public ChangeRaceDecision(@NotNull final Race newRace) {
        super(DecisionType.CHANGE_RACE);
        this.newRace = newRace;
    }

    public @NotNull Race getDecision() {
        return newRace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangeRaceDecision)) return false;
        if (!super.equals(o)) return false;
        final ChangeRaceDecision that = (ChangeRaceDecision) o;
        return getDecision() == that.getDecision();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDecision());
    }

    @Override
    public String toString() {
        return "ChangeRaceDecision{" +
                "newRace=" + newRace +
                '}';
    }
}
