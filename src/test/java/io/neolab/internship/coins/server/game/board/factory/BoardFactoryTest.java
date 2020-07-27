package io.neolab.internship.coins.server.game.board.factory;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BoardFactoryTest {

    @Test
    public void generateBoardCorrectlySize1Test() throws CoinsException {
        assertEquals(12, generateBoard(3, 4).getPositionToCellMap().size());
    }

    @Test
    public void generateBoardBalanced1Test() throws CoinsException {
        final IBoard board = generateBoard(3, 4);
        final List<CellType> cellTypes = fillCellTypes();
        for (final CellType cellType : cellTypes) {
            assertEquals(3,
                    board
                            .getPositionToCellMap()
                            .values()
                            .stream()
                            .filter(cell -> cell.getType() == cellType)
                            .count());
        }

    }

    @Test
    public void generateBoardCorrectlySize2Test() throws CoinsException {
        assertEquals(25, generateBoard(5, 5).getPositionToCellMap().size());
    }

    @Test
    public void generateBoardBalanced2Test() throws CoinsException {
        final int width = 5;
        final int height = 5;
        final int cellsAmount = width * height;
        final IBoard board = generateBoard(width, height);
        final List<CellType> cellTypes = fillCellTypes();
        final int cellTypesAmount = cellTypes.size();
        final int bound = cellsAmount / cellTypesAmount + cellsAmount % cellTypesAmount;
        for (final CellType cellType : cellTypes) {
            assertTrue(
                    board
                            .getPositionToCellMap()
                            .values()
                            .stream()
                            .filter(cell -> cell.getType() == cellType)
                            .count() <= bound);
        }

    }

    @Test(expected = CoinsException.class)
    public void generateBoardWrongSizesTest() throws CoinsException {
        generateBoard(1, 1);
    }

    private @NotNull IBoard generateBoard(final int wight, final int height) throws CoinsException {
        final BoardFactory boardFactory = new BoardFactory();
        return boardFactory.generateBoard(wight, height);
    }

    private @NotNull List<CellType> fillCellTypes() {
        final List<CellType> cellTypes = new ArrayList<>();
        cellTypes.add(CellType.WATER);
        cellTypes.add(CellType.LAND);
        cellTypes.add(CellType.MOUNTAIN);
        cellTypes.add(CellType.MUSHROOM);
        return cellTypes;
    }

}
