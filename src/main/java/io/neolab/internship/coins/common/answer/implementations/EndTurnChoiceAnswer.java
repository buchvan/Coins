package io.neolab.internship.coins.common.answer.implementations;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.EndTurnChoice;
import io.neolab.internship.coins.common.answer.interfaces.IEndTurnChoiceAnswer;

import java.util.Objects;

public class EndTurnChoiceAnswer extends Answer implements IEndTurnChoiceAnswer {
    private final EndTurnChoice endTurnChoice;

    public EndTurnChoiceAnswer(final EndTurnChoice endTurnChoice) {
        this.endTurnChoice = endTurnChoice;
    }

    @Override
    public EndTurnChoice getEndTurnChoice() {
        return endTurnChoice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndTurnChoiceAnswer that = (EndTurnChoiceAnswer) o;
        return endTurnChoice == that.endTurnChoice;
    }

    @Override
    public int hashCode() {
        return Objects.hash(endTurnChoice);
    }

    @Override
    public String toString() {
        return "EndTurnChoiceAnswer{" +
                "endTurnChoice=" + endTurnChoice +
                '}';
    }
}
