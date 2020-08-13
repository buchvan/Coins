package io.neolab.internship.coins.client.bot.ai.bim.model;

import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeTree {
    private final @NotNull List<Edge> edges;

    private final @Nullable Map<Player, Integer> winsCount; // игрок -> число побед в потомках этого узла
    private final int casesCount;

    private final @Nullable Map<Player, Pair<Integer, Integer>> playerToMaxAndMinCoinsCount; // игрок ->
    // пара (максимально число монет, минимальное число монет)

    private final @Nullable Map<Player, Integer> playerToValueDifference; // для каждого игрока минимум по всем
    // разностям количеств монет между данным игроком и остальными

    @Contract(pure = true)
    public NodeTree(final @NotNull List<Edge> edges, final @Nullable Map<Player, Integer> winsCount,
                    final int casesCount,
                    final @Nullable Map<Player, Pair<Integer, Integer>> playerToMaxAndMinCoinsCount,
                    final @Nullable Map<Player, Integer> playerToValueDifference) {
        this.edges = edges;
        this.winsCount = winsCount;
        this.casesCount = casesCount;
        this.playerToMaxAndMinCoinsCount = playerToMaxAndMinCoinsCount;
        this.playerToValueDifference = playerToValueDifference;
    }

    public @NotNull List<Edge> getEdges() {
        return edges;
    }

    public @Nullable Map<Player, Integer> getWinsCount() {
        return winsCount;
    }

    public int getCasesCount() {
        return casesCount;
    }

    public @Nullable Map<Player, Pair<Integer, Integer>> getPlayerToMaxAndMinCoinsCount() {
        return playerToMaxAndMinCoinsCount;
    }

    public @Nullable Map<Player, Integer> getPlayerToValueDifference() {
        return playerToValueDifference;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NodeTree nodeTree = (NodeTree) o;
        return casesCount == nodeTree.casesCount &&
                edges.equals(nodeTree.edges) &&
                Objects.equals(winsCount, nodeTree.winsCount) &&
                Objects.equals(playerToMaxAndMinCoinsCount, nodeTree.playerToMaxAndMinCoinsCount) &&
                Objects.equals(playerToValueDifference, nodeTree.playerToValueDifference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edges, winsCount, casesCount, playerToMaxAndMinCoinsCount, playerToValueDifference);
    }
}
