package io.neolab.internship.coins.client.bot;

import io.neolab.internship.coins.client.bot.ai.bim.AIProcessor;
import io.neolab.internship.coins.client.bot.ai.bim.action.*;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.RandomGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.neolab.internship.coins.client.bot.ai.bim.AIProcessor.removeNotAvailableForCaptureUnits;

public class SimpleBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(SimpleBot.class);

    @Override
    public boolean declineRaceChoose(final @NotNull Player player, final @NotNull IGame game) {
        final boolean choice;
        if (SmartBot.tree != null) {
            final DeclineRaceAction action =
                    (DeclineRaceAction) RandomGenerator.chooseItemFromList(SmartBot.tree.getEdges()).getAction();
            choice = Objects.requireNonNull(action).isDeclineRace();
            SmartBot.tree = AIProcessor.updateTree(SmartBot.tree, action);
        } else {
            choice = RandomGenerator.isYes();
        }
        LOGGER.debug("Simple bot decline race choice: {} ", choice);
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(final @NotNull Player player, final @NotNull IGame game) {
        final Race race;
        if (SmartBot.tree != null) {
            final ChangeRaceAction action =
                    (ChangeRaceAction) RandomGenerator.chooseItemFromList(SmartBot.tree.getEdges()).getAction();
            race = Objects.requireNonNull(action).getNewRace();
            SmartBot.tree = AIProcessor.updateTree(SmartBot.tree, action);
        } else {
            race = RandomGenerator.chooseItemFromList(game.getRacesPool());
        }
        LOGGER.debug("Simple bot choice race: {} ", race);
        return race;
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(final @NotNull Player player,
                                                                   final @NotNull IGame game) {
        final Pair<Position, List<Unit>> resolution;
        if (SmartBot.tree != null) {
            final CatchCellAction action =
                    (CatchCellAction) RandomGenerator.chooseItemFromList(SmartBot.tree.getEdges()).getAction();
            resolution = Objects.requireNonNull(action).getResolution();
            SmartBot.tree = AIProcessor.updateTree(SmartBot.tree, action);
            LOGGER.debug("Resolution of simple bot (by tree): {} ", resolution);
            return resolution;
        }
        if (RandomGenerator.isYes()) {
            LOGGER.debug("Simple bot will capture of cells");
            final IBoard board = game.getBoard();
            final List<Cell> controlledCells = game.getOwnToCells().get(player);
            final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
            GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells);
            final Cell catchingCell = RandomGenerator.chooseItemFromSet(achievableCells);

            /* Оставляем только те подконтрольные клетки, через которые можно добраться до catchingCell */
            final List<Cell> catchingCellNeighboringCells =
                    new LinkedList<>(
                            Objects.requireNonNull(board.getNeighboringCells(
                                    Objects.requireNonNull(catchingCell))));
            catchingCellNeighboringCells.removeIf(neighboringCell -> !controlledCells.contains(neighboringCell));

            final List<Unit> units = new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE));
            removeNotAvailableForCaptureUnits(board, units, catchingCellNeighboringCells,
                    catchingCell, controlledCells);
            final int unitsCountNeededToCatchCell =
                    controlledCells.contains(catchingCell)
                            ? catchingCell.getType().getCatchDifficulty()
                            : GameLoopProcessor.getUnitsCountNeededToCatchCell(game.getGameFeatures(), catchingCell);
            final int remainingUnitsCount = units.size() - unitsCountNeededToCatchCell;
            resolution =
                    remainingUnitsCount >= 0
                            ? new Pair<>(board.getPositionByCell(catchingCell),
                            units.subList(0,
                                    unitsCountNeededToCatchCell + RandomGenerator.chooseNumber(
                                            units.size() - unitsCountNeededToCatchCell + 1)))
                            : null;
            LOGGER.debug("Resolution of simple bot: {} ", resolution);
            return resolution;
        }
        LOGGER.debug("Simple bot will not capture of cells");
        return null;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(final @NotNull Player player, final @NotNull IGame game) {
        LOGGER.debug("Simple bot distributes units");
        final Map<Position, List<Unit>> distributionUnits;
        if (SmartBot.tree != null) {
            final DistributionUnitsAction action =
                    (DistributionUnitsAction) RandomGenerator.chooseItemFromList(SmartBot.tree.getEdges()).getAction();
            distributionUnits = Objects.requireNonNull(action).getResolutions();
            SmartBot.tree = AIProcessor.updateTree(SmartBot.tree, action);
            LOGGER.debug("Resolutions of simple bot (by tree): {} ", distributionUnits);
            return distributionUnits;
        }
        distributionUnits = new HashMap<>();
        final List<Unit> availableUnits = new LinkedList<>(player.getUnitsByState(AvailabilityType.AVAILABLE));
        List<Unit> units = new LinkedList<>();
        if (!game.getOwnToCells().get(player).isEmpty()) {
            while (availableUnits.size() > 0 && RandomGenerator.isYes()) {
                final Cell protectedCell = RandomGenerator.chooseItemFromList(
                        game.getOwnToCells().get(player)); // клетка, в которую игрок хочет распределить войска
                units.addAll(availableUnits.subList(0, RandomGenerator.chooseNumber(
                        availableUnits.size()))); // список юнитов, которое игрок хочет распределить в эту клетку
                distributionUnits.put(game.getBoard().getPositionByCell(protectedCell), units);
                availableUnits.removeAll(units);
                units = new LinkedList<>();
            }
        }
        LOGGER.debug("Simple bot distributed units: {} ", distributionUnits);
        return distributionUnits;
    }
}
