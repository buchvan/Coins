package io.neolab.internship.coins.server.game.factory;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.Position;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoardFactory implements IBoardFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardFactory.class);

    /**
     * При создании доски соблюдается принцип сбалансированности территории:
     * 1 версия: Каждый тип клетки встречается по три раза
     * Координаты начинаются в левом верхнем углу
     *
     * @param width  ширина
     * @param height высота
     * @return new Board
     */
    @Override
    public Board getBoard(final int width, final int height) {
        Random random = new Random();
        int cellAmount = width * height;
        List<CellType> cellTypes = loadCellTypePool(cellAmount);
        BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int randomCellTypeIndex = random.nextInt(cellTypes.size());
                //TODO: control cell amount of each cell types in board
                CellType currentCellType = cellTypes.remove(randomCellTypeIndex);
                positionToCellMap.put(new Position(i, j), new Cell(currentCellType));
            }
        }
        return new Board(positionToCellMap);
    }

    private List<CellType> loadCellTypePool(final int cellAmount) {
        List<CellType> cellTypes = new ArrayList<>(cellAmount);
        for (int i = 0; i < cellAmount; i++) {
            cellTypes.add(CellType.LAND);
            cellTypes.add(CellType.MOUNTAIN);
            cellTypes.add(CellType.MUSHROOM);
            cellTypes.add(CellType.WATER);
        }
        return cellTypes;
    }

}
