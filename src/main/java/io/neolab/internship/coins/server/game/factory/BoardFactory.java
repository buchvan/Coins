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
    //TODO: use logger
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardFactory.class);
    private BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();
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
    public Board generateBoard(final int width, final int height) {
        int cellAmount = width * height;
        List<CellType> cellTypes = loadCellTypePool(cellAmount);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int currentCellTypeIndex = getAllowedCellTypeIndex(cellTypes);
                CellType currentCellType = cellTypes.get(currentCellTypeIndex);
                Position position = new Position(i, j);
                System.out.println("ADDED POSITION: "+  position);
                Cell cell = new Cell(currentCellType);
                System.out.println("ADDED CELL: "+  cell);
                //FIXME: add id to cell for intial different cell with same cellType
                positionToCellMap.put(position, new Cell(currentCellType));
                System.out.println("CURRENT MAP SIZE: " + positionToCellMap.size());
            }
        }
        return new Board(positionToCellMap);
    }

    private int getAllowedCellTypeIndex(List<CellType> cellTypes){
        Random random = new Random();
        int cellTypesAmount = cellTypes.size();
        int randomCellTypeIndex = -1;
        boolean isCellTypeAvailable = false;
        while (!isCellTypeAvailable) {
            randomCellTypeIndex = random.nextInt(cellTypes.size());
            CellType currentCellType = cellTypes.get(randomCellTypeIndex);
            isCellTypeAvailable = positionToCellMap
                    .values()
                    .stream()
                    .filter(type -> type.getType() == currentCellType)
                    .count() <=  cellTypesAmount;
        }
        return randomCellTypeIndex;
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
