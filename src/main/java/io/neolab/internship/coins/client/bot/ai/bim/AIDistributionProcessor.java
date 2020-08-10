package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AIDistributionProcessor {

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
     * @param distributions - список распределений
     * @param player        - игрок
     */
    static void distributionsNumberReduce(final @NotNull List<List<Pair<Cell, Integer>>> distributions,
                                          final @NotNull Player player) {
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

        final Iterator<List<Pair<Cell, Integer>>> iterator = distributions.listIterator();
        while (iterator.hasNext()) {
            boolean flag = false;
            final List<Pair<Cell, Integer>> item = iterator.next();
            for (final Pair<Cell, Integer> pair : item) {
                if (pair.getSecond() == player.getUnitsByState(AvailabilityType.AVAILABLE).size()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                iterator.remove();
            }
        }
    }
}
