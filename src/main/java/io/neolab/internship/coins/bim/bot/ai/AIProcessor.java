package io.neolab.internship.coins.bim.bot.ai;

import io.neolab.internship.coins.bim.bot.FunctionType;
import io.neolab.internship.coins.bim.bot.ai.model.action.*;
import io.neolab.internship.coins.bim.bot.ai.model.NodeTree;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class AIProcessor {
    private static final double EPS = 1E-4;

    /**
     * @param nodeTree     - узел дерева, в котором мы в данный момент находимся
     * @param player       - игрок
     * @param functionType - тип функции бота
     * @return самое выгодное на данном этапе действие (если таковых несколько, то берём случайное из их числа)
     */
    public static @NotNull Action getAction(final @NotNull NodeTree nodeTree,
                                            final @NotNull Player player,
                                            final @NotNull FunctionType functionType) {
        final Player opponent;
        switch (functionType) {
            case MAX_VALUE_DIFFERENCE:
                return getAdvantageousValueDifferenceAction(nodeTree, player,
                        MinMaxProcessor.getValueDifference(nodeTree, player));
            case MIN_MAX_VALUE_DIFFERENCE:
                return MinMaxProcessor.maxMinValueDifferenceAlgorithm(nodeTree, player);
            case MAX_PERCENT:
                return getAdvantageousPercentAction(nodeTree, player,
                        MinMaxProcessor.getPercent(nodeTree, player, functionType));
            case MIN_PERCENT:
                opponent = MinMaxProcessor.getSomeOpponent(nodeTree, player);
                return getAdvantageousPercentAction(nodeTree, opponent,
                        MinMaxProcessor.getPercent(nodeTree, opponent, functionType));
            case MIN_MAX_PERCENT:
                return MinMaxProcessor.isFirstPlayer(nodeTree, player)
                        ? MinMaxProcessor.maxMinPercentAlgorithm(nodeTree, player)
                        : MinMaxProcessor.minMaxPercentAlgorithm(
                        nodeTree, MinMaxProcessor.getSomeOpponent(nodeTree, player));
            case MAX_VALUE:
                return getAdvantageousValueAction(nodeTree, player,
                        MinMaxProcessor.getValue(nodeTree, player, functionType), functionType);
            case MIN_VALUE:
                opponent = MinMaxProcessor.getSomeOpponent(nodeTree, player);
                return getAdvantageousValueAction(nodeTree, opponent,
                        MinMaxProcessor.getValue(nodeTree, opponent, functionType), functionType);
            case MIN_MAX_VALUE:
                return MinMaxProcessor.isFirstPlayer(nodeTree, player)
                        ? MinMaxProcessor.maxMinValueAlgorithm(nodeTree, player)
                        : MinMaxProcessor.minMaxValueAlgorithm(
                        nodeTree, MinMaxProcessor.getSomeOpponent(nodeTree, player));
            default:
                return null;
        }
    }

    /**
     * Взять выгодное с точки зрения отношения числа побед к общему числу случаев действие
     *
     * @param nodeTree - корень дерева
     * @param player   - игрок
     * @param value    - выгодное значение (отношение числа побед к числу случаев)
     * @return выгодное значение с точки зрения отношения числа побед к общему числу случаев,
     * которое повлечёт за собой данное действие
     */
    private static @NotNull Action getAdvantageousPercentAction(final @NotNull NodeTree nodeTree,
                                                                final @NotNull Player player,
                                                                final double value) {
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(nodeTree.getEdges().stream()
                        .filter(edge ->
                                Double.compare(Math.abs(value -
                                        (double) Objects.requireNonNull(edge.getTo().getWinsCount()).get(player)
                                                / edge.getTo().getCasesCount()), EPS) < 0)
                        .collect(Collectors.toList()))
                        .getAction());
    }

    /**
     * Взять выгодное с точки зрения числа монет
     *
     * @param nodeTree     - корень дерева
     * @param player       - игрок
     * @param value        - выгодное значение (число монет)
     * @param functionType - тип функции бота
     * @return выгодное значение с точки зрения числа монет,
     * которое повлечёт за собой данное действие
     */
    private static @NotNull Action getAdvantageousValueAction(final @NotNull NodeTree nodeTree,
                                                              final @NotNull Player player,
                                                              final int value,
                                                              final @NotNull FunctionType functionType) {
        return functionType == FunctionType.MAX_VALUE
                ? Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(nodeTree.getEdges().stream()
                        .filter(edge ->
                                Objects.requireNonNull(edge.getTo().getPlayerToMaxAndMinCoinsCount()).get(player)
                                        .getFirst() == value)
                        .collect(Collectors.toList()))
                        .getAction())
                : Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(nodeTree.getEdges().stream()
                        .filter(edge ->
                                Objects.requireNonNull(edge.getTo().getPlayerToMaxAndMinCoinsCount()).get(player)
                                        .getSecond() == value)
                        .collect(Collectors.toList()))
                        .getAction());
    }

    /**
     * Взять выгодное с точки зрения разности чисел монет
     *
     * @param nodeTree     - корень дерева
     * @param player       - игрок
     * @param value        - выгодное значение (число монет)
     * @return выгодное значение с точки зрения разности чисел монет,
     * которое повлечёт за собой данное действие
     */
    private static @NotNull Action getAdvantageousValueDifferenceAction(final @NotNull NodeTree nodeTree,
                                                                        final @NotNull Player player,
                                                                        final int value) {
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(nodeTree.getEdges().stream()
                        .filter(edge ->
                                Objects.requireNonNull(edge.getTo().getPlayerToValueDifference()).get(player) == value)
                        .collect(Collectors.toList()))
                        .getAction());
    }

}
