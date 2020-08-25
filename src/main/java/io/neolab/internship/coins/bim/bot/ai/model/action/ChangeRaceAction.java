package io.neolab.internship.coins.bim.bot.ai.model.action;

import io.neolab.internship.coins.server.game.player.Race;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChangeRaceAction extends Action {
    private final @NotNull Race newRace;

    public ChangeRaceAction(final @NotNull Race newRace) {
        super(ActionType.CHANGE_RACE);
        this.newRace = newRace;
    }

    public @NotNull Race getNewRace() {
        return newRace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ChangeRaceAction that = (ChangeRaceAction) o;
        return newRace == that.newRace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newRace);
    }

    @Override
    public String toString() {
        return "ChangeRaceAction{" +
                "newRace=" + newRace +
                '}';
    }
}
