package io.neolab.internship.coins;

import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.Position;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.apache.commons.collections4.BidiMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TestUtils {
    protected static void setPlayerUnits(final @NotNull Player player, final int unitsAmount,
                                         final @NotNull AvailabilityType type) {
        final List<Unit> playerUnits = new ArrayList<>();
        for (int i = 0; i < unitsAmount; i++) {
            playerUnits.add(new Unit());
        }
        player.getUnitStateToUnits().put(type, playerUnits);
    }

    protected static @NotNull Player getSomePlayer(final @NotNull IGame game) {
        return game.getPlayers().get(0);
    }

    protected static @NotNull Set<Cell> getAchievableCellSet(final @NotNull Cell cell) {
        final Set<Cell> set = new HashSet<>();
        set.add(cell);
        return set;
    }

    protected static @NotNull Cell getCellFromBoardByCellType(final @NotNull CellType cellType,
                                                              final @NotNull IBoard board) {
        return board.getPositionToCellMap()
                .values()
                .stream()
                .filter(cell -> cell.getType() == cellType)
                .collect(Collectors.toList())
                .get(0);
    }

    protected static @NotNull Position getSomeBoardPosition(final
                                                            @NotNull BidiMap<Position, Cell> positionCellBidiMap) {
        final List<Cell> cells = new ArrayList<>(positionCellBidiMap.values());
        return positionCellBidiMap.getKey(cells.get(0));
    }

}
