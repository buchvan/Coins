package io.neolab.internship.coins.common.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerQuestion extends ServerMessage {
    @JsonProperty
    private final PlayerQuestionType playerQuestionType;

    @JsonProperty
    private final @NotNull IGame game;

    @JsonProperty
    private final @NotNull Player player;

    @JsonCreator
    public PlayerQuestion(@NotNull @JsonProperty("serverMessageType") final ServerMessageType serverMessageType,
                          @NotNull @JsonProperty("playerQuestionType") final PlayerQuestionType playerQuestionType,
                          @NotNull @JsonProperty("game") final IGame game,
                          @NotNull @JsonProperty("player") final Player player) {
        super(serverMessageType);
        this.playerQuestionType = playerQuestionType;
        this.game = game;
        this.player = player;
    }

    public @NotNull IGame getGame() {
        return game;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public PlayerQuestionType getPlayerQuestionType() {
        return playerQuestionType;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final PlayerQuestion that = (PlayerQuestion) o;
        return playerQuestionType == that.playerQuestionType &&
                Objects.equals(game, that.game) &&
                Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerQuestionType, game, player);
    }

    @Override
    public String toString() {
        return "PlayerQuestion{" +
                "playerQuestionType=" + playerQuestionType +
                ", game=" + game +
                ", player=" + player +
                '}';
    }
}
