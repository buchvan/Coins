package io.neolab.internship.coins.common.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.neolab.internship.coins.common.deserialize.GameDeserializer;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;

import java.util.Objects;

public class PlayerQuestion extends ServerMessage {
    private final PlayerQuestionType playerQuestionType;

    @JsonDeserialize(using = GameDeserializer.class)
    private final IGame game;

    private final Player player;

    @JsonCreator
    public PlayerQuestion(@JsonProperty("serverMessageType") final ServerMessageType serverMessageType,
                          @JsonProperty("playerQuestionType") final PlayerQuestionType playerQuestionType,
                          @JsonProperty("game") final IGame game,
                          @JsonProperty("player") final Player player) {
        super(serverMessageType);
        this.playerQuestionType = playerQuestionType;
        this.game = game;
        this.player = player;
    }

    public IGame getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerQuestionType getPlayerQuestionType() {
        return playerQuestionType;
    }

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
