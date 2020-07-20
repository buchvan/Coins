package io.neolab.internship.coins.common.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neolab.internship.coins.common.deserialize.GameDeserializer;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerQuestion extends Question {

    @JsonDeserialize(using = GameDeserializer.class)
    private final @NotNull IGame game;

    private final @NotNull Player player;

    @JsonCreator
    public PlayerQuestion(@NotNull @JsonProperty("questionType") final QuestionType questionType,
                          @NotNull @JsonProperty("game") final IGame game,
                          @NotNull @JsonProperty("player") final Player player) {
        super(questionType);
        this.game = game;
        this.player = player;
    }

    public @NotNull IGame getGame() {
        return game;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerQuestion)) return false;
        final PlayerQuestion playerQuestion = (PlayerQuestion) o;
        return getQuestionType() == playerQuestion.getQuestionType() &&
                Objects.equals(getGame(), playerQuestion.getGame());
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
