package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.client.bot.ai.bim.model.action.*;
import io.neolab.internship.coins.client.bot.ai.bim.model.FunctionType;
import io.neolab.internship.coins.client.bot.ai.bim.model.NodeTree;
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
     * Найти и удалить недоступные для захвата клетки юнитов
     *
     * @param board                        - борда
     * @param units                        - список юнитов
     * @param catchingCellNeighboringCells - клетки, соседние с захватываемой клеткой
     * @param catchingCell                 - захватываемая клетка
     * @param controlledCells              - контролируемые игроком клетки
     */
    public static void removeNotAvailableForCaptureUnits(final @NotNull IBoard board, final @NotNull List<Unit> units,
                                                         final @NotNull List<Cell> catchingCellNeighboringCells,
                                                         final @NotNull Cell catchingCell,
                                                         final @NotNull List<Cell> controlledCells) {
        final List<Cell> boardEdgeCells = board.getEdgeCells();
        final Iterator<Unit> iterator = units.iterator();
        while (iterator.hasNext()) {
            boolean unitAvailableForCapture = false;
            final Unit unit = iterator.next();
            for (final Cell neighboringCell : catchingCellNeighboringCells) {
                if (neighboringCell.getUnits().contains(unit)) {
                    unitAvailableForCapture = true;
                    break;
                }
            }
            if (boardEdgeCells.contains(catchingCell) && !unitAvailableForCapture) {
                unitAvailableForCapture = true;
                for (final Cell controlledCell : controlledCells) {
                    if (controlledCell.getUnits().contains(unit)) {
                        if (!catchingCellNeighboringCells.contains(controlledCell)) {
                            unitAvailableForCapture = false;
                        }
                        break;
                    }
                }
            }
            if (!unitAvailableForCapture) {
                iterator.remove();
            }
        }
    }

    /**
     * @param nodeTree     - узел дерева, в котором мы в данный момент находимся
     * @param player       - игрок
     * @param functionType - тип функции бота
     * @return самое выгодное на данном этапе действие (если таковых несколько, то берём случайное из их числа)
     */
    public static @NotNull Action getAction(final @NotNull NodeTree nodeTree,
                                            final @NotNull Player player,
                                            final @NotNull FunctionType functionType) {
        switch (functionType) {
            case MAX:
                return getAdvantageousAction(nodeTree, player,
                        MinMaxProcessor.getValue(nodeTree, player, functionType));
            case MIN:
                final Player opponent = MinMaxProcessor.getSomeOpponent(nodeTree, player);
                return getAdvantageousAction(nodeTree, opponent,
                        MinMaxProcessor.getValue(nodeTree, opponent, functionType));
            case MIN_MAX:
                return MinMaxProcessor.isFirstPlayer(nodeTree, player)
                        ? MinMaxProcessor.maxAlgorithm(nodeTree, player)
                        : MinMaxProcessor.minAlgorithm(nodeTree, player);
            default:
                return null;
        }
    }

    /**
     * Взять выгодное действие
     *
     * @param nodeTree - корень дерева
     * @param player   - игрок
     * @param value    - выгодное значение (отношение числа побед к числу случаев)
     * @return выгодное значение с точки зрения отношения числа побед к общему числу случаев,
     * которое повлечёт за собой данное действие
     */
    private static @NotNull Action getAdvantageousAction(final @NotNull NodeTree nodeTree, final @NotNull Player player,
                                                         final double value) {
        return Objects.requireNonNull(
                RandomGenerator.chooseItemFromList(nodeTree.getEdges().stream()
                        .filter(edge ->
                                Double.compare(Math.abs(value -
                                        (double) edge.getTo().getWinsCount().get(player)
                                                / edge.getTo().getCasesCount()), EPS) < 0)
                        .collect(Collectors.toList()))
                        .getAction());
    }

}
