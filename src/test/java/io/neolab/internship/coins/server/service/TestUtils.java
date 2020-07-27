package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.common.message.client.answer.Answer;
import io.neolab.internship.coins.common.message.client.answer.CatchCellAnswer;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.Pair;
import org.apache.commons.collections4.BidiMap;

import java.util.*;
import java.util.stream.Collectors;

public class TestUtils {
    static void setPlayerUnits(final Player player, final int unitsAmount, final AvailabilityType type) {
        final List<Unit> playerUnits = new ArrayList<>();
        for (int i = 0; i < unitsAmount; i++) {
            playerUnits.add(new Unit());
        }
        player.getUnitStateToUnits().put(type, playerUnits);
    }

    public static Player getSomePlayer(final IGame game) {
        return game.getPlayers().get(0);
    }

    static Set<Cell> setAchievableCell(final Cell cell) {
        final Set<Cell> set = new HashSet<>();
        set.add(cell);
        return set;
    }

    static Cell getCellFromBoardByCellType(final CellType cellType, final IBoard board) {
        return board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == cellType)
                .collect(Collectors.toList())
                .get(0);
    }

    static Position getSomeBoardPosition(final BidiMap<Position, Cell> positionCellBidiMap) {
        final List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        return positionCellBidiMap.getKey(cells.get(0));
    }

    static Answer createCatchCellAnswer(final Position position, final int resolutionUnitsAmount) {
        final List<Unit> units = new ArrayList<>();
        for (int i = 0; i < resolutionUnitsAmount; i++) {
            units.add(new Unit());
        }
        return new CatchCellAnswer(new Pair<>(position, units));
    }

    static void setCellAsControlled(final Cell cell, final IGame game, final Player player) {
        final Map<Player, List<Cell>> ownToCells = game.getOwnToCells();
        final List<Cell> controlledCells = ownToCells.get(player);
        controlledCells.add(cell);
    }

    static void setUnitToCell(final Cell cell, final int unitsAmount) {
        final List<Unit> cellUnits = new ArrayList<>();
        for (int i = 0; i < unitsAmount; i++) {
            cellUnits.add(new Unit());
        }

        cell.setUnits(cellUnits);
    }
}
