package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.model.Edge;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
import io.neolab.internship.coins.client.bot.ai.bim.model.action.Action;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MinMaxProcessor {

    /**
     * Взять какого-нибудь оппонента игрока
     *
     * @param nodeTree - текущий узел дерева
     * @param player   - игрок
     * @return оппонента игрока
     */
    static @NotNull Player getSomeOpponent(final @NotNull NodeTree nodeTree, final @NotNull Player player) {
        return nodeTree.getEdges().get(0).getTo()
                .getWinsCount()
                .keySet()
                .stream()
                .filter(item ->
                        !item.equals(player))
                .findFirst()
                .orElseThrow();
    }

    static double getValue(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                           final @NotNull FunctionType functionType) {
        switch (functionType) {
            case MAX:
                return nodeTree.getEdges().stream()
                        .map(edge ->
                                (double) edge.getTo().getWinsCount().get(player)
                                        / edge.getTo().getCasesCount())
                        .max(Double::compareTo)
                        .orElseThrow();
            case MIN:
                return nodeTree.getEdges().stream()
                        .map(edge ->
                                (double) edge.getTo().getWinsCount().get(player)
                                        / edge.getTo().getCasesCount())
                        .min(Double::compareTo)
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
        for (final Player item : nodeTree.getWinsCount().keySet()) {
            if (item.getId() < player.getId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Поиск действия, максимизирующего доход игрока
     *
     * @param nodeTree - корень дерева
     * @param player   - игрок
     * @return действие, максимизирующее доход игрока
     */
    static @NotNull Action maxAlgorithm(final @NotNull NodeTree nodeTree, final @NotNull Player player) {
        final List<Edge> edges = nodeTree.getEdges();
        final Map<Edge, Integer> map = new HashMap<>(edges.size());
        final ExecutorService executorService = Executors.newFixedThreadPool(edges.size());
        edges.forEach(edge ->
                executorService.execute(() ->
                        map.put(edge,
                                getMaxValue(edge.getTo(), player, Objects.requireNonNull(edge.getPlayer())))));
        ExecutorServiceProcessor.executeExecutorService(executorService);
        final List<Edge> profitableEdges = new LinkedList<>();
        int maxValue = -1;
        for (final Map.Entry<Edge, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxValue) {
                profitableEdges.clear();
                profitableEdges.add(entry.getKey());
                maxValue = entry.getValue();
                continue;
            }
            if (entry.getValue() == maxValue) {
                profitableEdges.add(entry.getKey());
            }
        }
        return Objects.requireNonNull(RandomGenerator.chooseItemFromList(profitableEdges).getAction());
    }

    /**
     * @param nodeTree      - корень дерева
     * @param player        - думающий игрок
     * @param currentPlayer - игрок, который ходит на данном этапе
     * @return максимальное значение в дереве для игрока player
     */
    private static int getMaxValue(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                   final @NotNull Player currentPlayer) {
        if (nodeTree.getEdges().isEmpty()) {
            return nodeTree.getWinsCount().get(player);
        }
        if (player == currentPlayer) {
            int maxValue = -1;
            for (final Edge edge : nodeTree.getEdges()) {
                maxValue = Math.max(maxValue, getMaxValue(edge.getTo(), player,
                        edge.getPlayer() == null ? currentPlayer : edge.getPlayer()));
            }
            return maxValue;
        }
        int minValue = 2;
        for (final Edge edge : nodeTree.getEdges()) {
            minValue = Math.min(minValue, getMaxValue(edge.getTo(), player,
                    edge.getPlayer() == null ? currentPlayer : edge.getPlayer()));
        }
        return minValue;
    }

    /**
     * Поиск действия, минимизирующего доход оппонента игрока
     *
     * @param nodeTree - корень дерева
     * @param player   - игрок
     * @return действие, минимизирующее доход оппонента игрока
     */
    static @NotNull Action minAlgorithm(final @NotNull NodeTree nodeTree, final @NotNull Player player) {
        final List<Edge> edges = nodeTree.getEdges();
        final Map<Edge, Integer> map = new HashMap<>(edges.size());
        final ExecutorService executorService = Executors.newFixedThreadPool(edges.size());
        edges.forEach(edge ->
                executorService.execute(() ->
                        map.put(edge,
                                getMinValue(edge.getTo(), player, Objects.requireNonNull(edge.getPlayer())))));
        ExecutorServiceProcessor.executeExecutorService(executorService);
        final List<Edge> profitableEdges = new LinkedList<>();
        int minValue = 2;
        for (final Map.Entry<Edge, Integer> entry : map.entrySet()) {
            if (entry.getValue() < minValue) {
                profitableEdges.clear();
                profitableEdges.add(entry.getKey());
                minValue = entry.getValue();
                continue;
            }
            if (entry.getValue() == minValue) {
                profitableEdges.add(entry.getKey());
            }
        }
        return Objects.requireNonNull(RandomGenerator.chooseItemFromList(profitableEdges).getAction());
    }

    /**
     * @param nodeTree      - корень дерева
     * @param player        - думающий игрок
     * @param currentPlayer - игрок, который ходит на данном этапе
     * @return минимальное значение в дереве для игрока currentPlayer
     */
    private static int getMinValue(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                   final @NotNull Player currentPlayer) {
        if (nodeTree.getEdges().isEmpty()) {
            return nodeTree.getWinsCount().get(currentPlayer);
        }
        if (player == currentPlayer) {
            int minValue = 2;
            for (final Edge edge : nodeTree.getEdges()) {
                minValue = Math.min(minValue, getMinValue(edge.getTo(), player,
                        edge.getPlayer() == null ? currentPlayer : edge.getPlayer()));
            }
            return minValue;
        }
        int maxValue = -1;
        for (final Edge edge : nodeTree.getEdges()) {
            maxValue = Math.max(maxValue, getMinValue(edge.getTo(), player,
                    edge.getPlayer() == null ? currentPlayer : edge.getPlayer()));
        }
        return maxValue;
    }
}
