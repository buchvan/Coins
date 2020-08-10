package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.CatchCellDecision;
import io.neolab.internship.coins.ai.vika.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.Decision;
import io.neolab.internship.coins.ai.vika.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.*;

public class DecisionSimulationProcessor {
    //TODO: realize
    static void simulateDistributionUnitsDecision(final Decision decision, final Player playerCopy, final IGame game) {

    }

    /**
     * Симулирует принятое решение о захвате для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    static void simulateCatchCellDecision(final @NotNull Player player, final @NotNull IGame game,
                                          @NotNull final CatchCellDecision decision) {
        if (decision.getResolution() != null) {
            final GameFeatures gameFeatures = game.getGameFeatures();
            final IBoard board = game.getBoard();

            final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
            final List<Cell> controlledCells = ownToCells.get(player);
            final Position captureCellPosition = decision.getResolution().getFirst();
            final Cell captureCell = board.getCellByPosition(captureCellPosition);
            final boolean isControlled = controlledCells.contains(captureCell);

            final List<Unit> unitsForCapture = decision.getResolution().getSecond();

            final Map<Player, Set<Cell>> feudalToCells = game.getFeudalToCells();

            final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
            final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
            if (isControlled) {
                final int tiredUnitsCount = captureCell.getType().getCatchDifficulty();
                enterToCell(player, captureCell, ownToCells.get(player), feudalToCells.get(player),
                        unitsForCapture, tiredUnitsCount, board);
                return;
            }
            final List<Cell> neighboringCells = getAllNeighboringCells(board, captureCell);
            neighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));
            final int unitsCountNeededToCatch = getUnitsCountNeededToCatchCell(gameFeatures, captureCell);
            final int bonusAttack = getBonusAttackToCatchCell(player, gameFeatures, captureCell);
            catchCell(player, captureCell, neighboringCells, unitsForCapture.subList(0, unitsCountNeededToCatch - bonusAttack),
                    unitsForCapture, gameFeatures, ownToCells, feudalToCells, transitCells);
            if (controlledCells.size() == 1) { // если до этого у игрока не было клеток
                achievableCells.clear();
                achievableCells.add(captureCell);
            }
            achievableCells.addAll(getAllNeighboringCells(board, captureCell));
        }
    }

    /**
     * Симулирует принятое решение о смене расы для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    static void simulateChangeRaceDecision(final @NotNull Player player, final @NotNull IGame game,
                                           @NotNull final ChangeRaceDecision decision) {
        Arrays.stream(AvailabilityType.values())
                .forEach(availabilityType ->
                        player.getUnitStateToUnits().get(availabilityType).clear()); // Чистим у игрока юниты

        final List<Race> racesPool = game.getRacesPool();
        final Race newRace = decision.getNewRace();
        final Race oldRace = player.getRace();
        racesPool.remove(newRace); // Удаляем выбранную игроком расу из пула
        player.setRace(newRace);

        if (oldRace != null) {
            racesPool.add(oldRace);
        }

        /* Добавляем юнитов выбранной расы */
        int i = 0;
        while (i < newRace.getUnitsAmount()) {
            player.getUnitStateToUnits().get(AvailabilityType.AVAILABLE).add(new Unit());
            i++;
        }
    }


    /**
     * Симулирует принятое решение об упадке расы для копий игровых сущностей
     *
     * @param player   - текущий игрок
     * @param game     - текуще состояние игры
     * @param decision - принятое решение
     */
    static void simulateDeclineRaceDecision(final @NotNull Player player, final @NotNull IGame game,
                                            @NotNull final DeclineRaceDecision decision) {
        if (decision.isDeclineRace()) {
            game.getOwnToCells().get(player).clear();
        }
    }


}
