package io.neolab.internship.coins.server.game.factory;


import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.CellType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

//TODO: add cases with exceptions, wrong params etc.
public class BoardFactoryTest {

    @Test
    public void generateBoardCorrectlySizeTest() {
        assertEquals(12, generateBoard(3,4).getPositionToCellMap().size());
    }

    @Test
    public void generateBoardBalancedTest() {
        Board board = generateBoard(3,4);
        List<CellType> cellTypes = fillCellTypes();
        for(CellType cellType: cellTypes) {
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
    public void generateBoardWrongSizesTest() {
        assertNull(generateBoard(1, 1));
    }

    private Board generateBoard(final int wight,final int height) {
        BoardFactory boardFactory = new BoardFactory();
        return  boardFactory.generateBoard(wight, height);
    }

    private List<CellType> fillCellTypes() {
        List<CellType> cellTypes = new ArrayList<>();
        cellTypes.add(CellType.WATER);
        cellTypes.add(CellType.LAND);
        cellTypes.add(CellType.MOUNTAIN);
        cellTypes.add(CellType.MUSHROOM);
        return  cellTypes;
    }

}
