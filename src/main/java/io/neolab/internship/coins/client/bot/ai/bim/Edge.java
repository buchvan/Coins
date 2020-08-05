package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.action.Action;
import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Edge {
    private final @Nullable Player player;
    private final @Nullable Action action;
    private final @NotNull NodeTree to;

    public Edge(@Nullable final Player player, @Nullable final Action action, @NotNull final NodeTree to) {
        this.player = player;
        this.action = action;
        this.to = to;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public @Nullable Action getAction() {
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
        return Objects.equals(player, edge.player) &&
                Objects.equals(action, edge.action) &&
                to.equals(edge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, action, to);
    }
}
