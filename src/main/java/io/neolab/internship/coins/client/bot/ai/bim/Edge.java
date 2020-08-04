package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.action.Action;
import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Edge {
    private final @NotNull Player player;
    private final @NotNull Action action;
    private final @NotNull NodeTree to;

    public Edge(@NotNull final Player player, @NotNull final Action action, @NotNull final NodeTree to) {
        this.player = player;
        this.action = action;
        this.to = to;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Action getAction() {
        return action;
    }

    public @NotNull NodeTree getTo() {
        return to;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Edge edge = (Edge) o;
        return player.equals(edge.player) &&
                action.equals(edge.action) &&
                to.equals(edge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, action, to);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "player=" + player +
                ", action=" + action +
                ", to=" + to +
                '}';
    }
}
