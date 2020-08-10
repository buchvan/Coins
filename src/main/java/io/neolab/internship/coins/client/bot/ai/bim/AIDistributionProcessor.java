package io.neolab.internship.coins.client.bot.ai.bim;

import io.neolab.internship.coins.server.game.board.Cell;
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
}
