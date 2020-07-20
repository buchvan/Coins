package io.neolab.internship.coins.client;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.server.game.service.GameLoopProcessor;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import io.neolab.internship.coins.utils.RandomGenerator;

import java.util.*;

public class SimpleBot implements IBot {
    Random random = new Random();

    @Override
    public boolean declineRaceChoose(final Player player, final IGame game) {
        return random.nextInt(2) == 1;
    }

    @Override
    public Race chooseRace(final Player player, final IGame game) {
        return RandomGenerator.chooseItemFromList(game.getRacesPool());
    }

    @Override
    public Pair<Position, List<Unit>> chooseCatchingCell(final Player player, final IGame game) {
        if (random.nextInt(2) == 1) {
            final IBoard board = game.getBoard();
            final List<Cell> controlledCells = game.getOwnToCells().get(player);
            final Set<Cell> achievableCells = game.getPlayerToAchievableCells().get(player);
            GameLoopProcessor.updateAchievableCells(board, achievableCells, controlledCells);
            final Cell catchingCell = RandomGenerator.chooseItemFromSet(achievableCells);

            /* Оставляем только те клетки, через которые можно добраться до catchingCell */
            final List<Cell> catchingCellNeighboringCells = new LinkedList<>();
            catchingCellNeighboringCells.add(catchingCell);
            catchingCellNeighboringCells.addAll(GameLoopProcessor.getAllNeighboringCells(board, catchingCell));
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
            return new Pair<>(board.getPositionByCell(catchingCell),
                    units.size() > 0
                            ? units.subList(0, RandomGenerator.chooseNumber(units.size()))
                            : new LinkedList<>()
            );
        } // else
        return null;
    }

    @Override
    public Map<Position, List<Unit>> distributionUnits(final Player player, final IGame game) {
        final Map<Position, List<Unit>> distributionUnits = new HashMap<>();
        final List<Unit> availableUnits = player.getUnitsByState(AvailabilityType.AVAILABLE);
        List<Unit> units = new LinkedList<>();
        while (availableUnits.size() > 0 && random.nextInt(2) == 1) {
            final Cell protectedCell = RandomGenerator.chooseItemFromList(
                    game.getOwnToCells().get(player)); // клетка, в которую игрок хочет распределить войска
            units.addAll(availableUnits.subList(units.size(), RandomGenerator.chooseNumber(
                    availableUnits.size() - units.size()) + units.size()
            )); // список юнитов, которое игрок хочет распределить в эту клетку
            distributionUnits.put(game.getBoard().getPositionByCell(protectedCell), units);
            units = new LinkedList<>();
        }
        return distributionUnits;
    }
}
