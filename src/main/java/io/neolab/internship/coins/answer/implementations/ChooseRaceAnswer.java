package io.neolab.internship.coins.answer.implementations;

import io.neolab.internship.coins.answer.Answer;
import io.neolab.internship.coins.answer.interfaces.IChooseRaceAnswer;
import io.neolab.internship.coins.server.board.Race;

import java.util.Objects;

/**
 * Ответ на вопрос CHOOSE_RACE (выбрать новую расу)
 */
public class ChooseRaceAnswer extends Answer implements IChooseRaceAnswer {
    private final Race newRace; // Новая раса

    public ChooseRaceAnswer(final Race newRace) {
        this.newRace = newRace;
    }

    @Override
    public Race getNewRace() {
        return newRace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseRaceAnswer that = (ChooseRaceAnswer) o;
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
