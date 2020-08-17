package io.neolab.internship.coins.client.bot.ai.bim.model;

import io.neolab.internship.coins.client.bot.ai.bim.model.action.Action;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Edge {
    private final int playerId;
    private final @Nullable Action action;
    private final @NotNull NodeTree to;

    @Contract(pure = true)
    public Edge(final int playerId, @Nullable final Action action, @NotNull final NodeTree to) {
        this.playerId = playerId;
        this.action = action;
        this.to = to;
    }

    public int getPlayerId() {
        return playerId;
    }

    public @Nullable Action getAction() {
        return action;
    }

    public @NotNull NodeTree getTo() {
        return to;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Edge edge = (Edge) o;
        return playerId == edge.playerId &&
                Objects.equals(action, edge.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, action);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "action=" + action +
                '}';
    }
}
