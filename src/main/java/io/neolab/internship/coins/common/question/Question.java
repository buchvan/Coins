package io.neolab.internship.coins.common.question;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;

import java.util.Objects;

public class Question {
    private final QuestionType questionType;
    private final IGame game;
    private final Player player;

    public Question(QuestionType questionType, IGame game, Player player) {
        this.questionType = questionType;
        this.game = game;
        this.player = player;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public IGame getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
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
