package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.Pair;
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
        final int denominator = (int) Math.pow(2, depth - 1);
        int capacity = (units.size() - tiredUnitsCount + 1) / denominator;
        capacity = capacity == 0 ? 1 : capacity;
        final Set<Integer> indexes = new HashSet<>(capacity);
        indexes.add(tiredUnitsCount);
        indexes.add(units.size());
        indexes.add((tiredUnitsCount + units.size()) / 2);
        for (int i = tiredUnitsCount; i <= units.size(); i++) {
            if (RandomGenerator.isYes()) {
                indexes.add(i);
            }
            if (indexes.size() >= capacity) {
                i = units.size();
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
    static @NotNull List<List<Pair<Cell, Integer>>> getDistributions(final @NotNull List<Cell> cells,
                                                                     final int n) {
        final List<List<Pair<Cell, Integer>>> distributions = new LinkedList<>();
        if (!cells.isEmpty()) {
            final Cell cell = cells.get(0);
            for (int i = n; i >= 0; i--) {
                final List<Cell> otherCells = new LinkedList<>(cells);
                otherCells.remove(cell);
                final List<List<Pair<Cell, Integer>>> miniDistributions = getDistributions(otherCells, n - i);
                if (i > 0) {
                    final int unitsToCell = i;
                    miniDistributions.forEach(miniDistribution -> miniDistribution.add(new Pair<>(cell, unitsToCell)));
                    final List<Pair<Cell, Integer>> distribution = new LinkedList<>();
                    distribution.add(new Pair<>(cell, unitsToCell));
                    miniDistributions.add(distribution);
                }
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
    static @NotNull Set<List<Pair<Cell, Integer>>> distributionsNumberReduce(
            final @NotNull List<List<Pair<Cell, Integer>>> distributions, final int controlledCellsCount) {
        if (controlledCellsCount == 0) {
            return new HashSet<>(0);
        }
        final Set<List<Pair<Cell, Integer>>> actualDistributions = new HashSet<>(controlledCellsCount);
        for (final List<Pair<Cell, Integer>> distribution : distributions) {
            if (RandomGenerator.isYes()) {
                actualDistributions.add(distribution);
            }
            if (actualDistributions.size() >= controlledCellsCount) {
                break;
            }
        }
        if (actualDistributions.size() < controlledCellsCount) {
            for (final List<Pair<Cell, Integer>> distribution : distributions) {
                actualDistributions.add(distribution);
                if (actualDistributions.size() >= controlledCellsCount) {
                    break;
                }
            }
        }
        return actualDistributions;

//        final int removeDistributionsNumber = distributions.size() - distributions.size() / 5;
//        final Iterator<List<Pair<Cell, Integer>>> iterator1 = distributions.listIterator();
//        int i = 0;
//        while (iterator1.hasNext() && i < removeDistributionsNumber) {
//            if (RandomGenerator.isYes()) {
//                i++;
//                iterator1.next();
//                iterator1.remove();
//            }
//        }
//        if (distributions.size() > 0 && i < removeDistributionsNumber) {
//            final Iterator<List<Pair<Cell, Integer>>> iterator2 = distributions.listIterator();
//            while (iterator2.hasNext() && i < removeDistributionsNumber) {
//                i++;
//                iterator2.next();
//                iterator2.remove();
//            }
//        }
    }
}
