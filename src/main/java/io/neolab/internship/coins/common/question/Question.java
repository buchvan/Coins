package io.neolab.internship.coins.common.question;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerQuestion.class, name = "PlayerQuestion"),
})
public class Question {
    @JsonProperty
    private final QuestionType questionType;

    @JsonCreator
    public Question(@JsonProperty("questionType") final QuestionType questionType) {
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
