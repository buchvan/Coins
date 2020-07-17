package io.neolab.internship.coins.common.question;

import java.util.Objects;

public class Question {
    private final QuestionType questionType;

    public Question(final QuestionType questionType) {
        this.questionType = questionType;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

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
