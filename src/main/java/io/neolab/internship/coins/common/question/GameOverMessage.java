package io.neolab.internship.coins.common.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.Player;

import java.util.List;
import java.util.Objects;

public class GameOverMessage extends ServerMessage {
    private final List<Player> winners;
    private final List<Player> playerList;

    @JsonCreator
    public GameOverMessage(@JsonProperty("questionType") final ServerMessageType serverMessageType,
                           @JsonProperty("winners") final List<Player> winners,
                           @JsonProperty("playerList") final List<Player> playerList) {
        super(serverMessageType);
        this.winners = winners;
        this.playerList = playerList;
    }

    public List<Player> getWinners() {
        return winners;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final GameOverMessage that = (GameOverMessage) o;
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
