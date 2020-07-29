package io.neolab.internship.coins.client;

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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimpleBot implements IBot {
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(SimpleBot.class);
    private final @NotNull Random random = new Random();

    @Override
    public boolean declineRaceChoose(final @NotNull Player player, final @NotNull IGame game) {
        final boolean choice = RandomGenerator.isYes();
        LOGGER.debug("Simple bot decline race choice: {} ", choice);
        return choice;
    }

    @Override
    public @NotNull Race chooseRace(final @NotNull Player player, final @NotNull IGame game) {
        final Race race = RandomGenerator.chooseItemFromList(game.getRacesPool());
        LOGGER.debug("Simple bot choice race: {} ", race);
        return race;
    }

    @Override
    public @Nullable Pair<Position, List<Unit>> chooseCatchingCell(final @NotNull Player player,
                                                                   final @NotNull IGame game) {
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
                        if (!catchingCellNeighboringCells.contains(controlledCell)
                                && controlledCell.getUnits().contains(unit)) {
                            unitAvailableForCapture = false;
                            break;
                        }
                    }
                }
                if (!unitAvailableForCapture) {
                    iterator.remove();
                }
            }
            final Pair<Position, List<Unit>> resolution = new Pair<>(board.getPositionByCell(catchingCell),
                    units.size() > 0
                            ? units.subList(0, RandomGenerator.chooseNumber(units.size()))
                            : new LinkedList<>()
            );
            LOGGER.debug("Resolution of simple bot: {} ", resolution);
            return resolution;
        } // else
        LOGGER.debug("Simple bot will not capture of cells");
        return null;
    }

    @Override
    public @NotNull Map<Position, List<Unit>> distributionUnits(final @NotNull Player player, final @NotNull IGame game) {
        LOGGER.debug("Simple bot distributes units");
        final Map<Position, List<Unit>> distributionUnits = new HashMap<>();
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        List<Unit> units = new LinkedList<>();
        while (availableUnits.size() > 0 && RandomGenerator.isYes()) {
            final Cell protectedCell = RandomGenerator.chooseItemFromList(
                    game.getOwnToCells().get(player)); // клетка, в которую игрок хочет распределить войска
            units.addAll(availableUnits.subList(0, RandomGenerator.chooseNumber(
                    availableUnits.size()))); // список юнитов, которое игрок хочет распределить в эту клетку
            distributionUnits.put(game.getBoard().getPositionByCell(protectedCell), units);
            availableUnits.removeAll(units);
            units = new LinkedList<>();
        }
        LOGGER.debug("Simple bot distributed units: {} ", distributionUnits);
        return distributionUnits;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SimpleBot simpleBot = (SimpleBot) o;
        return random.equals(simpleBot.random);
    }

    @Override
    public int hashCode() {
        return Objects.hash(random);
    }

    @Override
    public String toString() {
        return "SimpleBot{" +
                "random=" + random +
                '}';
    }
}
