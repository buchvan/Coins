package io.neolab.internship.coins.common.question;

import io.neolab.internship.coins.server.game.Game;

import java.util.Objects;

public class Question {
    private final QuestionType questionType;
    private Game game;

    public Question(final QuestionType questionType, final Game game) {
        this.questionType = questionType;
        this.game = game;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(final Game game) {
        this.game = game;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        final Question question = (Question) o;
        return getQuestionType() == question.getQuestionType() &&
                Objects.equals(getGame(), question.getGame());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestionType(), getGame());
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionType=" + questionType +
                ", game=" + game +
                '}';
    }
}
