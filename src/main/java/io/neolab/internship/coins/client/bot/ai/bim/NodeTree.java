package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.server.game.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeTree {
    private final @NotNull List<Edge> edges;
    private final @NotNull Map<Player, Integer> winsCount;
    private final int casesCount;

    public NodeTree(final @NotNull List<Edge> edges, final @NotNull Map<Player, Integer> winsCount,
                    final int casesCount) {
        this.edges = edges;
        this.winsCount = winsCount;
        this.casesCount = casesCount;
    }

    public @NotNull List<Edge> getEdges() {
        return edges;
    }

    public @NotNull Map<Player, Integer> getWinsCount() {
        return winsCount;
    }

    public int getCasesCount() {
        return casesCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NodeTree nodeTree = (NodeTree) o;
        return casesCount == nodeTree.casesCount &&
                edges.equals(nodeTree.edges) &&
                winsCount.equals(nodeTree.winsCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edges, winsCount, casesCount);
    }
}
