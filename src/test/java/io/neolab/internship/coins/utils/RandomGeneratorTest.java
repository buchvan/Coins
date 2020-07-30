package io.neolab.internship.coins.utils;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class RandomGeneratorTest {

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testChooseItemFromListNull() {
        RandomGenerator.chooseItemFromList(null);
    }

    @Test
    public void testChooseItemFromList() {
        final List<Cell> cellList = new LinkedList<>();
        cellList.add(new Cell(CellType.MUSHROOM));
        cellList.add(new Cell(CellType.LAND));
        cellList.add(new Cell(CellType.MOUNTAIN));
        assertTrue(cellList.contains(RandomGenerator.chooseItemFromList(cellList)));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testChooseItemFromSetNull() {
        RandomGenerator.chooseItemFromSet(null);
    }

    @Test
    public void testChooseItemFromSet() {
        final Set<Cell> cellSet = new HashSet<>();
        cellSet.add(new Cell(CellType.MUSHROOM));
        cellSet.add(new Cell(CellType.MUSHROOM));
        cellSet.add(new Cell(CellType.LAND));
        cellSet.add(new Cell(CellType.MOUNTAIN));
        cellSet.add(new Cell(CellType.WATER));
        cellSet.add(new Cell(CellType.LAND));
        assertTrue(cellSet.contains(RandomGenerator.chooseItemFromSet(cellSet)));
    }
}
