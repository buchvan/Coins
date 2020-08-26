package io.neolab.internship.coins.bim.bot.ai;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AIDistributionProcessor {

    /**
     * Взять какое-то подмножество индексов из отрезка [tiredUnitsCount; units.size()]
     *
     * @param units           - список юнитов
     * @param tiredUnitsCount - число уставших юнитов
     * @param depth           - глубина дерева
     * @return подмножество индексов из отрезка [tiredUnitsCount; units.size()]
     */
    static @NotNull Set<Integer> getIndexes(final @NotNull List<Unit> units, final int tiredUnitsCount,
                                            final int depth) {
        final int denominator = depth < 3 ? 1 : (int) Math.pow(2, depth - 1);
        int capacity = (units.size() - tiredUnitsCount + 1) / denominator;
        capacity = capacity == 0 ? 1 : capacity;
        final Set<Integer> indexes = new HashSet<>(capacity);
        indexes.add(tiredUnitsCount);
        for (int i = tiredUnitsCount + 1; i <= units.size(); i++) {
            if (indexes.size() >= capacity) {
                i = units.size();
            }
            if (RandomGenerator.isYes()) {
                indexes.add(i);
            }
        }
        if (indexes.size() < capacity) {
            for (int i = tiredUnitsCount; i <= units.size(); i++) {
                indexes.add(i);
                if (indexes.size() >= capacity) {
                    i = units.size();
                }
            }
        }
        return indexes;
    }

    /**
     * Взять всевозможные распределения n юнитов на клетках cells
     *
     * @param cells - список клеток
     * @param n     - число юнитов, которое можно распределить на cells
     * @return список распределений. Распределение - это список пар (клетка, число юнитов, распределённых в неё)
     */
    @Contract(pure = true)
    static @NotNull List<Map<Cell, Integer>> getDistributions(final @NotNull List<Cell> cells, final int n) {
        final List<Map<Cell, Integer>> distributions = new LinkedList<>();
        if (!cells.isEmpty()) {
            final Cell cell = cells.get(0);
            for (int i = n; i > 0; i--) {
                final List<Cell> otherCells = new LinkedList<>(cells);
                otherCells.remove(cell);
                if (otherCells.isEmpty()) {
                    final Map<Cell, Integer> distribution = new HashMap<>();
                    distribution.put(cell, i);
                    distributions.add(distribution);
                    i = 0;
                    continue;
                }
                final List<Map<Cell, Integer>> miniDistributions = getDistributions(otherCells, n - i);
                final int unitsToCell = i;
                miniDistributions.forEach(miniDistribution -> miniDistribution.put(cell, unitsToCell));
                distributions.addAll(miniDistributions);
            }
        }
        return distributions;
    }

    /**
     * Сократить число распределений
     *
     * @param distributions        - список распределений
     * @param controlledCellsCount - число подконтрольных клеток
     * @return какое-то множество распределений
     */
    static @NotNull Set<Map<Cell, Integer>> distributionsNumberReduce(
            final @NotNull List<Map<Cell, Integer>> distributions, final int controlledCellsCount) {
        if (controlledCellsCount == 0) {
            return new HashSet<>(0);
        }
        final Set<Map<Cell, Integer>> actualDistributions = new HashSet<>(controlledCellsCount);
        for (final Map<Cell, Integer> distribution : distributions) {
            if (RandomGenerator.isYes()) {
                actualDistributions.add(distribution);
            }
            if (actualDistributions.size() >= controlledCellsCount) {
                break;
            }
        }
        if (actualDistributions.size() < controlledCellsCount) {
            for (final Map<Cell, Integer> distribution : distributions) {
                actualDistributions.add(distribution);
                if (actualDistributions.size() >= controlledCellsCount) {
                    break;
                }
            }
        }
        return actualDistributions;
    }
}
