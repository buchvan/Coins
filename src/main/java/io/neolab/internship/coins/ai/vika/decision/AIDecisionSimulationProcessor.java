package io.neolab.internship.coins.ai.vika.decision;

import io.neolab.internship.coins.ai.vika.decision.model.CatchCellDecision;
import io.neolab.internship.coins.ai.vika.decision.model.ChangeRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.DeclineRaceDecision;
import io.neolab.internship.coins.ai.vika.decision.model.DistributionUnitsDecision;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.service.GameLogger;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.neolab.internship.coins.server.service.GameLoopProcessor.*;

class AIDecisionSimulationProcessor {
    static void simulateDistributionUnitsDecision(final DistributionUnitsDecision decision, final Player player, final IGame game) {
        GameLoopProcessor.playerRoundBeginUpdate(player);
        final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
        final List<Cell> controlledCells = game.getOwnToCells().get(player);
        GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells);
        GameLoopProcessor.loseCells(controlledCells, controlledCells, game.getFeudalToCells().get(player));
        controlledCells.forEach(controlledCell -> controlledCell.getUnits().clear());
        GameLoopProcessor.makeAllUnitsSomeState(player,
                AvailabilityType.AVAILABLE); // доступными юнитами становятся все имеющиеся у игрока юниты
        decision.getResolutions().forEach((position, units) -> {
            GameLogger.printCellDefendingLog(player, units.size(), position);
            GameLoopProcessor.protectCell(player,
                    Objects.requireNonNull(game.getBoard().getCellByPosition(position)), units);
        });
        loseCells(controlledCells, controlledCells, game.getFeudalToCells().get(player));
        GameLoopProcessor.playerRoundEndUpdate(player);
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
        GameLoopProcessor.playerRoundBeginUpdate(player);
        if (decision.getDecision() != null) {
            final GameFeatures gameFeatures = game.getGameFeatures();
            final IBoard board = game.getBoard();

            final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
            final List<Cell> controlledCells = ownToCells.get(player);

            final Position captureCellPosition = decision.getDecision().getFirst();
            final Cell captureCell = board.getCellByPosition(captureCellPosition);
            final boolean isControlled = controlledCells.contains(captureCell);

            final List<Unit> unitsForCapture = decision.getDecision().getSecond();

            final Map<Player, Set<Cell>> feudalToCells = game.getFeudalToCells();

            final List<Cell> transitCells = game.getPlayerToTransitCells().get(player);
            final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
            if (isControlled) {
                final int tiredUnitsCount = Objects.requireNonNull(captureCell).getType().getCatchDifficulty();
                enterToCell(player, captureCell, ownToCells.get(player), feudalToCells.get(player),
                        unitsForCapture, tiredUnitsCount, board);
                return;
            }
            final List<Cell> neighboringCells = getAllNeighboringCells(board, Objects.requireNonNull(captureCell));
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
        GameLoopProcessor.playerRoundEndUpdate(player);
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
        GameLoopProcessor.playerRoundBeginUpdate(player);
        Arrays.stream(AvailabilityType.values())
                .forEach(availabilityType ->
                        player.getUnitStateToUnits().get(availabilityType).clear()); // Чистим у игрока юниты

        final List<Race> racesPool = game.getRacesPool();
        final Race newRace = decision.getDecision();
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
        GameLoopProcessor.playerRoundEndUpdate(player);
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
        GameLoopProcessor.playerRoundBeginUpdate(player);
        if (decision.isDeclineRace()) {
            game.getOwnToCells().get(player).clear();
        }
        GameLoopProcessor.playerRoundEndUpdate(player);
    }


}
