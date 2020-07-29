package io.neolab.internship.coins.utils;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListProcessorTest {
    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testChooseItemFromListNull() {
        ListProcessor.removeFirstN(1, null);
    }

    @Test
    public void testChooseItemFromList() {
        final List<Cell> cellList = new LinkedList<>();
        cellList.add(new Cell(CellType.LAND));
        cellList.add(new Cell(CellType.MUSHROOM));
        final Cell expectedCell1 = new Cell(CellType.WATER);
        cellList.add(expectedCell1);
        final Cell expectedCell2 = new Cell(CellType.MOUNTAIN);
        cellList.add(expectedCell2);
        final Cell expectedCell3 = new Cell(CellType.LAND);
        cellList.add(expectedCell3);
        ListProcessor.removeFirstN(2, cellList);

        final List<Cell> expected = new LinkedList<>();
        expected.add(expectedCell1);
        expected.add(expectedCell2);
        expected.add(expectedCell3);
        assertEquals(expected, cellList);
    }
}
