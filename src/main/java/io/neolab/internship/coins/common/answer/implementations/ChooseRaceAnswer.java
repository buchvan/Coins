package io.neolab.internship.coins.common.answer.implementations;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.interfaces.IChooseRaceAnswer;
import io.neolab.internship.coins.server.game.Race;

import java.util.Objects;

public class ChooseRaceAnswer extends Answer implements IChooseRaceAnswer {
    private final Race newRace;

    public ChooseRaceAnswer(final Race newRace) {
        this.newRace = newRace;
    }

    @Override
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
