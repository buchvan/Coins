package io.neolab.internship.coins.server.service;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.player.Player;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameLoopProcessorTests {
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
