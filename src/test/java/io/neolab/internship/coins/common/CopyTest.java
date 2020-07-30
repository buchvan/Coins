package io.neolab.internship.coins.common;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.IGame;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.factory.BoardFactory;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.service.GameInitializer;
import io.neolab.internship.coins.TestUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CopyTest extends TestUtils {

    @Test
    public void testGameGetCopyEquals() throws CoinsException {
        final IGame expected = GameInitializer.gameInit(3, 4, 2);
        final IGame actual = expected.getCopy();
        if (expected == actual) {
            fail();
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testGameGetCopyLinks1() throws CoinsException {
        final IGame expected = GameInitializer.gameInit(3, 4, 2);
        final IGame actual = expected.getCopy();
        if (actual.getBoard() == expected.getBoard() ||
                actual.getPlayers() == expected.getPlayers() ||
                actual.getOwnToCells() == expected.getOwnToCells() ||
                actual.getFeudalToCells() == expected.getFeudalToCells() ||
                actual.getPlayerToTransitCells() == expected.getPlayerToTransitCells() ||
                actual.getRacesPool() == expected.getRacesPool() ||
                actual.getPlayerToAchievableCells() == expected.getPlayerToAchievableCells() ||
                actual.getGameFeatures() == expected.getGameFeatures()) {
            fail();
        }
    }

    @Test
    public void testGameGetCopyLinks2() throws CoinsException {
        final IGame expected = GameInitializer.gameInit(3, 4, 2);
        final IGame actual = expected.getCopy();
        expected.getPlayers().forEach(player ->
                actual.getPlayers().forEach(player1 -> {
                    if (player == player1) {
                        fail();
                    }
                }));
        expected.getOwnToCells().forEach((player, cells) ->
                actual.getOwnToCells().forEach((player1, cells1) -> {
                    if (player == player1 || cells == cells1) {
                        fail();
                    }
                    cells.forEach(cell ->
                            cells1.forEach(cell1 -> {
                                if (cell == cell1) {
                                    fail();
                                }
                            }));
                }));
        expected.getFeudalToCells().forEach((player, cells) ->
                actual.getFeudalToCells().forEach((player1, cells1) -> {
                    if (player == player1 || cells == cells1) {
                        fail();
                    }
                    cells.forEach(cell ->
                            cells1.forEach(cell1 -> {
                                if (cell == cell1) {
                                    fail();
                                }
                            }));
                }));
        expected.getPlayerToAchievableCells().forEach((player, cells) ->
                actual.getPlayerToAchievableCells().forEach((player1, cells1) -> {
                    if (player == player1 || cells == cells1) {
                        fail();
                    }
                    cells.forEach(cell ->
                            cells1.forEach(cell1 -> {
                                if (cell == cell1) {
                                    fail();
                                }
                            }));
                }));
        expected.getPlayerToTransitCells().forEach((player, cells) ->
                actual.getPlayerToTransitCells().forEach((player1, cells1) -> {
                    if (player == player1 || cells == cells1) {
                        fail();
                    }
                    cells.forEach(cell ->
                            cells1.forEach(cell1 -> {
                                if (cell == cell1) {
                                    fail();
                                }
                            }));
                }));
        expected.getGameFeatures().getRaceCellTypeFeatures().forEach((raceCellTypePair, features) ->
                actual.getGameFeatures().getRaceCellTypeFeatures().forEach((raceCellTypePair1, features1) -> {
                    if (raceCellTypePair == raceCellTypePair1) {
                        fail();
                    }
                    features.forEach(feature ->
                            features1.forEach(feature1 -> {
                                if (feature == feature1) {
                                    fail();
                                }
                            }));
                }));
    }

    @Test
    public void testBoardGetCopyEquals() throws CoinsException {
        final IBoard expected = new BoardFactory().generateBoard(3, 4);
        final IBoard actual = expected.getCopy();
        if (expected == actual) {
            fail();
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testBoardGetCopyLinks1() throws CoinsException {
        final IBoard expected = new BoardFactory().generateBoard(3, 4);
        final IBoard actual = expected.getCopy();
        if (actual.getEdgeCells() == expected.getEdgeCells() ||
                actual.getPositionToCellMap() == expected.getPositionToCellMap()) {
            fail();
        }
    }

    @Test
    public void testBoardGetCopyLinks2() throws CoinsException {
        final IBoard expected = new BoardFactory().generateBoard(3, 4);
        final IBoard actual = expected.getCopy();
        expected.getPositionToCellMap().forEach((position, cell) ->
                actual.getPositionToCellMap().forEach((position1, cell1) -> {
                    if (position == position1 || cell == cell1) {
                        fail();
                    }
                }));
        expected.getEdgeCells().forEach(cell ->
                actual.getEdgeCells().forEach(cell1 -> {
                    if (cell == cell1) {
                        fail();
                    }
                }));
    }

    @Test
    public void testPlayerGetCopyEquals() throws CoinsException {
        final Player expected = getSomePlayer(GameInitializer.gameInit(3, 4, 2));
        final Player actual = expected.getCopy();
        if (expected == actual) {
            fail();
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testPlayerGetCopyLinks1() throws CoinsException {
        final Player expected = getSomePlayer(GameInitializer.gameInit(3, 4, 2));
        final Player actual = expected.getCopy();
        if (actual.getUnitStateToUnits() == expected.getUnitStateToUnits()) {
            fail();
        }
    }

    @Test
    public void testPlayerGetCopyLinks2() throws CoinsException {
        final Player expected = getSomePlayer(GameInitializer.gameInit(3, 4, 2));
        final Player actual = expected.getCopy();
        expected.getUnitStateToUnits().forEach((availabilityType, units) ->
                actual.getUnitStateToUnits().forEach((availabilityType1, units1) -> {
                    if (units == units1) {
                        fail();
                    }
                    units.forEach(unit ->
                            units1.forEach(unit1 -> {
                                if (unit == unit1) {
                                    fail();
                                }
                            }));
                }));
    }
}
