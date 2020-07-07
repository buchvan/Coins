package io.neolab.internship.coins.common.answer.implementations;

import io.neolab.internship.coins.common.answer.Answer;
import io.neolab.internship.coins.common.answer.BeginRoundChoice;
import io.neolab.internship.coins.common.answer.interfaces.IEndTurnChoiceAnswer;

import java.util.Objects;

public class BeginRoundChoiceAnswer extends Answer implements IEndTurnChoiceAnswer {
    private final BeginRoundChoice beginRoundChoice;

    public BeginRoundChoiceAnswer(final BeginRoundChoice beginRoundChoice) {
        this.beginRoundChoice = beginRoundChoice;
    }

    @Override
    public BeginRoundChoice getBeginRoundChoice() {
        return beginRoundChoice;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BeginRoundChoiceAnswer that = (BeginRoundChoiceAnswer) o;
        return beginRoundChoice == that.beginRoundChoice;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginRoundChoice);
    }

    @Override
    public String toString() {
        return "EndTurnChoiceAnswer{" +
                "endTurnChoice=" + beginRoundChoice +
                '}';
    }
}
