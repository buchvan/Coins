package io.neolab.internship.coins.common.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GameOverQuestion extends ServerMessage {
    private final @NotNull List<Player> winners;
    private final @NotNull List<Player> playerList;

    @JsonCreator
    public GameOverQuestion(@NotNull @JsonProperty("questionType") final ServerMessageType questionType,
                            @NotNull @JsonProperty("winners") final List<Player> winners,
                            @NotNull @JsonProperty("playerList") final List<Player> playerList) {
        super(questionType);
        this.winners = winners;
        this.playerList = playerList;
    }

    public @NotNull List<Player> getWinners() {
        return winners;
    }

    public @NotNull List<Player> getPlayerList() {
        return playerList;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final GameOverQuestion that = (GameOverQuestion) o;
        return Objects.equals(winners, that.winners) &&
                Objects.equals(playerList, that.playerList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), winners, playerList);
    }

    @Override
    public String toString() {
        return "GameOverQuestion{" +
                "winners=" + winners +
                ", playerList=" + playerList +
                '}';
    }
}
