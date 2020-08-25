package io.neolab.internship.coins.ai.vika.utils;

import io.neolab.internship.coins.ai.vika.exception.AIBotException;
import io.neolab.internship.coins.ai.vika.exception.AIBotExceptionErrorCode;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.getBonusAttackToCatchCell;
import static io.neolab.internship.coins.server.service.GameLoopProcessor.getUnitsCountNeededToCatchCell;

public class AIDecisionMakerUtils {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AIDecisionMakerUtils.class);

    /**
     * Проверка возможности захвата клетки
     *
     * @param cell   -клетка
     * @param player - текущий игрок
     * @param game   - текущее состояние игры
     * @return - возможность захвата клетки
     */
    public static boolean checkCellCaptureOpportunity(@NotNull final Cell cell, @NotNull final Player player,
                                                      @NotNull final IGame game) {
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        final List<Unit> playerAvailableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        if (controlledCells.contains(cell)) {
            return playerAvailableUnits.size() >= cell.getType().getCatchDifficulty();
        }
        final GameFeatures features = game.getGameFeatures();
        return playerAvailableUnits.size() >=
                getUnitsCountNeededToCatchCell(features, cell, false)
                        + getBonusAttackToCatchCell(player, features, cell, false);
    }

    /**
     * Рекурсивная функция, которая возвращает всевозможные комбинации
     * (клетка; количество юнитов, отправляемых в клетку)
     *
     * @param cellForDistribution  - клетки, для которых нужно решить сколько юнитов туда отправлять
     * @param remainingUnitsAmount - оставшееся количество юнитов
     * @return - всевозможные комбинации клетка->количество юнитов для нее
     */
    @NotNull
    public static List<List<Pair<Cell, Integer>>> getDistributionUnitsCombination(final List<Cell> cellForDistribution,
                                                                                  final int remainingUnitsAmount) {
        final List<List<Pair<Cell, Integer>>> combinations = new LinkedList<>();
        if (remainingUnitsAmount <= 0 || cellForDistribution.isEmpty()) {
            return combinations;
        }
        for (int i = 0; i <= remainingUnitsAmount; i++) {
            final List<Cell> cellForDistributionCopy = new LinkedList<>(cellForDistribution);
            for (final Cell cell : cellForDistributionCopy) {
                final List<Pair<Cell, Integer>> currentCombination = new LinkedList<>();
                currentCombination.add(new Pair<>(cell, i));
                final List<Cell> cells = new LinkedList<>(cellForDistributionCopy);
                cells.remove(cell);
               // cellForDistributionCopy.remove(cell);
                final List<List<Pair<Cell, Integer>>> remainingCellCombinations =
                        getDistributionUnitsCombination(cells, remainingUnitsAmount - i);
                if (remainingCellCombinations.size() > 0) {
                    for (final List<Pair<Cell, Integer>> remainingCellCombination : remainingCellCombinations) {
                        if (remainingCellCombination.size() > 0) {
                            currentCombination.addAll(remainingCellCombination);
                        }
                    }
                }
                combinations.add(currentCombination);
            }
        }
        return combinations;
    }

    /**
     * Возвращает копию текущего игрока из копии игры
     *
     * @param game            - копия игры
     * @param currentPlayerId - id текущего игрока
     * @return - копия нужного игрока
     * @throws AIBotException - если копия не найдена
     */
    public static Player getPlayerCopy(@NotNull final IGame game, final int currentPlayerId) throws AIBotException {
        return game.getPlayers()
                .stream()
                .filter(player -> player.getId() == currentPlayerId)
                .findFirst()
               .orElseThrow();
    }

    public static int getPlayerIndexFromGame(final List<Player> players, final int currentPlayerId) {
        final Player currentPlayer = players
                .stream()
                .filter(player -> player.getId() == currentPlayerId)
                .findFirst()
                .orElseThrow();
        return players.indexOf(currentPlayer);
    }

}
