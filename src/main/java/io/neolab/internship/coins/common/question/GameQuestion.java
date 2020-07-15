package io.neolab.internship.coins.common.question;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neolab.internship.coins.common.deserialize.GameQuestionDeserializer;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;

import java.util.Objects;

@JsonDeserialize(using = GameQuestionDeserializer.class)
public class GameQuestion extends Question {
    private final IGame game;
    private final Player player;

    public GameQuestion(final QuestionType questionType, final IGame game, final Player player) {
        super(questionType);
        this.game = game;
        this.player = player;
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
        if (!(o instanceof GameQuestion)) return false;
        final GameQuestion gameQuestion = (GameQuestion) o;
        return getQuestionType() == gameQuestion.getQuestionType() &&
                Objects.equals(getGame(), gameQuestion.getGame());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestionType(), getGame());
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionType=" + getQuestionType() +
                ", game=" + game +
                '}';
    }
}
