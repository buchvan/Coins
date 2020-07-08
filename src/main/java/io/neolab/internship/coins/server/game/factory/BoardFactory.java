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
        LOGGER.info("Start generating board with width " + width + " height " + height);
        //TODO: add exceptions
        if(width < 2 || height < 2) {
            LOGGER.info("Board generation failed");
            return null;
        }
        final int cellAmount = width * height;
        final List<CellType> cellTypes = loadCellTypePool();
        StringBuilder logBoardString = new StringBuilder("\n");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int currentCellTypeIndex = getAllowedCellTypeIndex(cellTypes, cellAmount);
                CellType currentCellType = cellTypes.get(currentCellTypeIndex);
                if(currentCellType == CellType.MUSHROOM) {
                    logBoardString.append(currentCellType.getTitle().substring(0,1).toLowerCase());
                }
                else {
                    logBoardString.append(currentCellType.getTitle(), 0, 1);
                }
                logBoardString.append(" ");
                positionToCellMap.put(new Position(i, j), new Cell(currentCellType));
            }
            logBoardString.append("\n");
        }
        LOGGER.info(logBoardString.toString());
        return new Board(positionToCellMap);
    }

    private int getAllowedCellTypeIndex(final List<CellType> cellTypes,final int cellAmount) {
        Random random = new Random();
        final int cellTypesAmount = cellTypes.size();
        final int cellTypesAmountRange = cellAmount / cellTypesAmount;
        int randomCellTypeIndex = -1;
        boolean isCellTypeAvailable = false;
        while (!isCellTypeAvailable) {
            randomCellTypeIndex = random.nextInt(cellTypesAmount);
            CellType currentCellType = cellTypes.get(randomCellTypeIndex);
            isCellTypeAvailable = positionToCellMap
                    .values()
                    .stream()
                    .filter(type -> type.getType() == currentCellType)
                    .count() < cellTypesAmountRange;
        }
        return randomCellTypeIndex;
    }

    private List<CellType> loadCellTypePool() {
        List<CellType> cellTypes = new ArrayList<>();
        cellTypes.add(CellType.LAND);
        cellTypes.add(CellType.MOUNTAIN);
        cellTypes.add(CellType.MUSHROOM);
        cellTypes.add(CellType.WATER);
        return cellTypes;
    }
}
