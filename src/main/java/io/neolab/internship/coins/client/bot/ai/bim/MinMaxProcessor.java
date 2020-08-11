package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.model.Edge;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.model.action.Action;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MinMaxProcessor {

    /**
     * Взять какого-нибудь оппонента игрока
     *
     * @param nodeTree - текущий узел дерева
     * @param player   - игрок
     * @return оппонента игрока
     */
    static @NotNull Player getSomeOpponent(final @NotNull NodeTree nodeTree, final @NotNull Player player) {
        final NodeTree someNodeTree = nodeTree.getEdges().get(0).getTo();
        if (someNodeTree.getWinsCount() != null) {
            return someNodeTree.getWinsCount()
                    .keySet()
                    .stream()
                    .filter(item ->
                            !item.equals(player))
                    .findFirst()
                    .orElseThrow();
        } // else
        return Objects.requireNonNull(someNodeTree.getPlayerToMaxAndMinCoinsCount())
                .keySet()
                .stream()
                .filter(item ->
                        !item.equals(player))
                .findFirst()
                .orElseThrow();
    }

    /**
     * @param nodeTree     - корень дерева
     * @param player       - игрок
     * @param functionType - тип бота игрока
     * @return процент, соответствующий типу бота: отношение числа побед к общему числу случаев
     */
    static double getPercent(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                             final @NotNull FunctionType functionType) {
        switch (functionType) {
            case MAX_PERCENT:
                return nodeTree.getEdges().stream()
                        .map(edge ->
                                (double) Objects.requireNonNull(edge.getTo().getWinsCount()).get(player)
                                        / edge.getTo().getCasesCount())
                        .max(Double::compareTo)
                        .orElseThrow();
            case MIN_PERCENT:
                return nodeTree.getEdges().stream()
                        .map(edge ->
                                (double) Objects.requireNonNull(edge.getTo().getWinsCount()).get(player)
                                        / edge.getTo().getCasesCount())
                        .min(Double::compareTo)
                        .orElseThrow();
            default:
                return -1;
        }
    }

    /**
     * @param nodeTree     - корень дерева
     * @param player       - игрок
     * @param functionType - тип бота игрока
     * @return процент, соответствующий типу бота: отношение числа побед к общему числу случаев
     */
    static int getValue(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                        final @NotNull FunctionType functionType) {
        switch (functionType) {
            case MAX_VALUE:
                return nodeTree.getEdges().stream()
                        .map(edge ->
                                Objects.requireNonNull(
                                        edge.getTo().getPlayerToMaxAndMinCoinsCount()).get(player).getFirst())
                        .max(Integer::compareTo)
                        .orElseThrow();
            case MIN_VALUE:
                return nodeTree.getEdges().stream()
                        .map(edge ->
                                Objects.requireNonNull(
                                        edge.getTo().getPlayerToMaxAndMinCoinsCount()).get(player).getSecond())
                        .min(Integer::compareTo)
                        .orElseThrow();
            default:
                return -1;
        }
    }

    /**
     * Игрок имеет право первого хода в данном дереве?
     *
     * @param nodeTree - корень дерева
     * @param player   - игрок
     * @return true, если игрок ходит первым, false - иначе
     */
    static boolean isFirstPlayer(final @NotNull NodeTree nodeTree, final @NotNull Player player) {
        if (nodeTree.getWinsCount() != null) {
            for (final Player item : nodeTree.getWinsCount().keySet()) {
                if (item.getId() < player.getId()) {
                    return false;
                }
            }
            return true;
        } // else
        for (final Player item : Objects.requireNonNull(nodeTree.getPlayerToMaxAndMinCoinsCount()).keySet()) {
            if (item.getId() < player.getId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Поиск действия, максимизирующего вероятность выигрыша игрока
     *
     * @param nodeTree - корень дерева
     * @param player   - игрок
     * @return действие, максимизирующее вероятность выигрыша игрока
     */
    static @NotNull Action maxMinPercentAlgorithm(final @NotNull NodeTree nodeTree, final @NotNull Player player) {
        final List<Edge> edges = nodeTree.getEdges();
        final Map<Edge, Boolean> edgeToPercent = new HashMap<>(edges.size());
        edges.forEach(edge -> edgeToPercent.put(edge, getMaxPercent(edge.getTo(), player, player)));
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(getProfitableEdgesPercent(edgeToPercent)).getAction());
    }

    private static @NotNull List<Edge> getProfitableEdgesPercent(final @NotNull Map<Edge, Boolean> edgeToPercent) {
        final List<Edge> profitableEdges = new LinkedList<>();
        for (final Map.Entry<Edge, Boolean> entry : edgeToPercent.entrySet()) {
            if (entry.getValue()) {
                profitableEdges.add(entry.getKey());
            }
        }
        if (profitableEdges.isEmpty()) {
            profitableEdges.addAll(edgeToPercent.keySet());
        }
        return profitableEdges;
    }

    /**
     * @param nodeTree      - корень дерева
     * @param player        - думающий игрок
     * @param currentPlayer - игрок, который ходит на данном этапе
     * @return true, если есть последовательность действий, при которой игрок побеждает, false - если нет
     */
    private static boolean getMaxPercent(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                         final @NotNull Player currentPlayer) {
        return nodeTree.getEdges().isEmpty()
                ? Objects.requireNonNull(nodeTree.getWinsCount()).get(player) == 1
                : getDefaultPercent(nodeTree.getEdges(), player, currentPlayer);
    }

    private static boolean getDefaultPercent(final @NotNull List<Edge> edges, final @NotNull Player player,
                                             final @NotNull Player currentPlayer) {
        if (player == currentPlayer) {
            boolean isWin = false;
            for (final Edge edge : edges) {
                isWin = isWin || getMaxPercent(edge.getTo(), player,
                        edge.getPlayerId() == currentPlayer.getId() ? currentPlayer : player);
            }
            return isWin;
        }
        boolean isWin = true;
        for (final Edge edge : edges) {
            isWin = isWin && getMinPercent(edge.getTo(), player,
                    edge.getPlayerId() == currentPlayer.getId() ? currentPlayer : player);
        }
        return isWin;
    }

    /**
     * @param nodeTree      - корень дерева
     * @param opponent      - оппонент
     * @param currentPlayer - игрок, который ходит на данном этапе
     * @return true, если есть последовательность действий, при которой оппонент проигрывает, false - если нет
     */
    private static boolean getMinPercent(final @NotNull NodeTree nodeTree, final @NotNull Player opponent,
                                         final @NotNull Player currentPlayer) {
        return nodeTree.getEdges().isEmpty()
                ? Objects.requireNonNull(nodeTree.getWinsCount()).get(opponent) == 0
                : getDefaultPercent(nodeTree.getEdges(), opponent, currentPlayer);
    }

    /**
     * Поиск действия, минимизирующего вероятность выигрыша оппонента
     *
     * @param nodeTree - корень дерева
     * @param player   - игрок
     * @param opponent - оппонент игрока
     * @return действие, минимизирующее вероятность выигрыша оппонента
     */
    static @NotNull Action minMaxPercentAlgorithm(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                                  final @NotNull Player opponent) {
        final List<Edge> edges = nodeTree.getEdges();
        final Map<Edge, Boolean> edgeToPercent = new HashMap<>(edges.size());
        edges.forEach(edge -> edgeToPercent.put(edge, getMinPercent(edge.getTo(), opponent, player)));
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(getProfitableEdgesPercent(edgeToPercent)).getAction());
    }

    /**
     * Поиск действия, максимизирующего доход игрока
     *
     * @param nodeTree - корень дерева
     * @param player   - думающий игрок
     * @param opponent - оппонент игрока
     * @return действие, максимизирующее доход игрока
     */
    static @NotNull Action maxMinValueAlgorithm(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                                final @NotNull Player opponent) {
        final List<Edge> edges = nodeTree.getEdges();
        final Map<Edge, Integer> edgeToValue = new HashMap<>(edges.size());
        edges.forEach(edge -> edgeToValue.put(edge, getMaxValue(edge.getTo(), player, opponent, player)));
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(getProfitableEdgesMaxValue(edgeToValue)).getAction());
    }

    private static @NotNull List<Edge> getProfitableEdgesMaxValue(final @NotNull Map<Edge, Integer> edgeToValue) {
        final List<Edge> profitableEdges = new LinkedList<>();
        int maxValue = -1;
        for (final Map.Entry<Edge, Integer> entry : edgeToValue.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                profitableEdges.clear();
                profitableEdges.add(entry.getKey());
                continue;
            }
            if (entry.getValue() == maxValue) {
                profitableEdges.add(entry.getKey());
            }
        }
        return profitableEdges;
    }

    private static int getMaxValue(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                   final @NotNull Player opponent, final @NotNull Player currentPlayer) {
        return nodeTree.getEdges().isEmpty()
                ? Objects.requireNonNull(nodeTree.getPlayerToMaxAndMinCoinsCount()).get(player).getFirst()
                : getDefaultValue(nodeTree.getEdges(), player, opponent, currentPlayer);
    }

    private static int getDefaultValue(final @NotNull List<Edge> edges, final @NotNull Player player,
                                       final @NotNull Player opponent, final @NotNull Player currentPlayer) {
        if (player == currentPlayer) {
            int maxValue = -1;
            for (final Edge edge : edges) {
                maxValue = Math.max(maxValue, getMaxValue(edge.getTo(), player, opponent,
                        edge.getPlayerId() == currentPlayer.getId() ? currentPlayer : opponent));
            }
            return maxValue;
        }
        int minValue = Integer.MAX_VALUE;
        for (final Edge edge : edges) {
            minValue = Math.min(minValue, getMinValue(edge.getTo(), player, opponent,
                    edge.getPlayerId() == currentPlayer.getId() ? currentPlayer : player));
        }
        return minValue;
    }

    private static int getMinValue(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                   final @NotNull Player opponent, final @NotNull Player currentPlayer) {
        return nodeTree.getEdges().isEmpty()
                ? Objects.requireNonNull(nodeTree.getPlayerToMaxAndMinCoinsCount()).get(opponent).getSecond()
                : getDefaultValue(nodeTree.getEdges(), player, opponent, currentPlayer);
    }

    /**
     * Поиск действия, минимизирующего доход оппонента игрока
     *
     * @param nodeTree - корень дерева
     * @param player   - думающий игрок
     * @param opponent - оппонент игрока
     * @return действие, минимизирующее доход оппонента игрока
     */
    static @NotNull Action minMaxValueAlgorithm(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                                final @NotNull Player opponent) {
        final List<Edge> edges = nodeTree.getEdges();
        final Map<Edge, Integer> edgeToValue = new HashMap<>(edges.size());
        edges.forEach(edge -> edgeToValue.put(edge, getMinValue(edge.getTo(), player, opponent, player)));
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(getProfitableEdgesMinValue(edgeToValue)).getAction());
    }

    private static @NotNull List<Edge> getProfitableEdgesMinValue(final @NotNull Map<Edge, Integer> edgeToValue) {
        final List<Edge> profitableEdges = new LinkedList<>();
        int minValue = Integer.MAX_VALUE;
        for (final Map.Entry<Edge, Integer> entry : edgeToValue.entrySet()) {
            if (entry.getValue() < minValue) {
                minValue = entry.getValue();
                profitableEdges.clear();
                profitableEdges.add(entry.getKey());
                continue;
            }
            if (entry.getValue() == minValue) {
                profitableEdges.add(entry.getKey());
            }
        }
        return profitableEdges;
    }
}
