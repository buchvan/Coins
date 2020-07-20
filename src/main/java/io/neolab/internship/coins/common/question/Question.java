package io.neolab.internship.coins.common.question;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Question {
    private final @NotNull QuestionType questionType;

    @Contract(pure = true)
    public Question(final @NotNull QuestionType questionType) {
        this.questionType = questionType;
    }

    public @NotNull QuestionType getQuestionType() {
        return questionType;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Question question = (Question) o;
        return questionType == question.questionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionType);
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionType=" + questionType +
                '}';
    }
}
