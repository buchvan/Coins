package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.TestUtils;
import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.board.factory.BoardFactory;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.utils.AvailabilityType;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class GameLoopProcessorTests extends TestUtils {
    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testPlayerRoundBeginUpdateNull() {
        GameLoopProcessor.playerRoundBeginUpdate(null);
    }

    @Test
    public void testPlayerRoundBeginUpdate() {
        final Player player = new Player("F1");
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(new Unit());
        final Unit unit1 = new Unit();
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(unit1);
        final Unit unit2 = new Unit();
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(unit2);

        GameLoopProcessor.playerRoundBeginUpdate(player);

        assertTrue(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).isEmpty());
        assertTrue(player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit1));
        assertTrue(player.getUnitsByState(AvailabilityType.AVAILABLE).contains(unit2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testPlayerRoundEndUpdateNull() {
        GameLoopProcessor.playerRoundEndUpdate(null);
    }

    @Test
    public void testPlayerRoundEndUpdate() {
        final Player player = new Player("F1");
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(new Unit());
        player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).add(new Unit());
        final Unit unit1 = new Unit();
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(unit1);
        final Unit unit2 = new Unit();
        player.getUnitsByState(AvailabilityType.AVAILABLE).add(unit2);

        GameLoopProcessor.playerRoundEndUpdate(player);

        assertTrue(player.getUnitsByState(AvailabilityType.AVAILABLE).isEmpty());
        assertTrue(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).contains(unit1));
        assertTrue(player.getUnitsByState(AvailabilityType.NOT_AVAILABLE).contains(unit2));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull1() throws CoinsException {
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        GameLoopProcessor.updateAchievableCells(null, board, new HashSet<>(), new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull2() {
        GameLoopProcessor.updateAchievableCells(new Player("F1"), null, new HashSet<>(), new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull3() throws CoinsException {
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        GameLoopProcessor.updateAchievableCells(new Player("F1"), board, null, new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateAchievableCellsNull4() throws CoinsException {
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        GameLoopProcessor.updateAchievableCells(new Player("F1"), board, new HashSet<>(), null);
    }

    @Test
    public void testUpdateAchievableCellsBoardEdge() throws CoinsException {
        final Player player = new Player("F1");
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        final Set<Cell> achievableCells = new HashSet<>();
        final List<Cell> controlledCells = new LinkedList<>();
        final Cell someCell = getCellFromBoardByCellType(CellType.LAND, board);
        controlledCells.add(someCell);
        GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells);
        assertNotNull(board.getNeighboringCells(someCell));
        assertTrue(achievableCells.containsAll(Objects.requireNonNull(board.getNeighboringCells(someCell))));
        assertEquals(
                Objects.requireNonNull(board.getNeighboringCells(someCell)).size() + 1, achievableCells.size());
    }

    @Test
    public void testUpdateAchievableCellsNotBoardEdge() throws CoinsException {
        final Player player = new Player("F1");
        final IBoard board = new BoardFactory().generateBoard(3, 3);
        final Set<Cell> achievableCells = new HashSet<>();
        final List<Cell> controlledCells = new LinkedList<>();
        GameLoopProcessor.updateAchievableCells(player, board, achievableCells, controlledCells);
        assertTrue(achievableCells.containsAll(board.getEdgeCells()));
        assertEquals(board.getEdgeCells().size(), achievableCells.size());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testFreeTransitCellsNull1() {
        GameLoopProcessor.freeTransitCells(null, new LinkedList<>(), new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testFreeTransitCellsNull2() {
        GameLoopProcessor.freeTransitCells(new Player("F1"), null, new LinkedList<>());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testFreeTransitCellsNull3() {
        GameLoopProcessor.freeTransitCells(new Player("F1"), new LinkedList<>(), null);
    }

    @Test
    public void testFreeTransitCells() {
        final Player player = new Player("F1");
        final List<Cell> transitCells = new LinkedList<>();
        transitCells.add(new Cell(CellType.MUSHROOM));
        transitCells.add(new Cell(CellType.LAND));
        transitCells.add(new Cell(CellType.MOUNTAIN));

        final List<Cell> controlledCells = new LinkedList<>(transitCells);
        final Cell cell1 = new Cell(CellType.WATER);
        controlledCells.add(cell1);
        final Cell cell2 = new Cell(CellType.LAND);
        controlledCells.add(cell2);
        GameLoopProcessor.freeTransitCells(player, transitCells, controlledCells);
        assertTrue(transitCells.isEmpty());
        final List<Cell> expected = new LinkedList<>();
        expected.add(cell1);
        expected.add(cell2);
        assertEquals(expected, controlledCells);
    }
}
