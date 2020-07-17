package io.neolab.internship.coins.server.game.factory;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.exceptions.ErrorCode;
import io.neolab.internship.coins.server.game.board.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoardFactory implements IBoardFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardFactory.class);
    private final BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();

    /**
     * При создании доски соблюдается принцип сбалансированности территории:
     * 1 версия: Каждый тип клетки встречается по три раза
     * Координаты начинаются в левом верхнем углу
     *
     * @param boardSizeX высота
     * @param boardSizeY ширина
     * @return new Board
     */
    @Override
    public IBoard generateBoard(final int boardSizeX, final int boardSizeY) throws CoinsException {
        LOGGER.debug("Start generating board with width {} and height {}", boardSizeY, boardSizeX);
        if (boardSizeX < 2 || boardSizeY < 2) {
            LOGGER.error("Board generation with width {} and height {} failed", boardSizeY, boardSizeX);
            throw new CoinsException(ErrorCode.WRONG_BOARD_SIZES);
        }
        final int cellAmount = boardSizeX * boardSizeY;
        final List<CellType> cellTypes = loadCellTypePool();
        final StringBuilder logBoardString = new StringBuilder("\n");
        for (int i = 0; i < boardSizeX; i++) {
            for (int j = 0; j < boardSizeY; j++) {
                final int currentCellTypeIndex = getAllowedCellTypeIndex(cellTypes, cellAmount);
                final CellType currentCellType = cellTypes.get(currentCellTypeIndex);
                positionToCellMap.put(new Position(i, j), new Cell(currentCellType));
                logBoardString.append(currentCellType.getView()).append(" ");
            }
            logBoardString.append("\n");
        }
        /*
        * Вывод лога о результате генерации игровой доски
        * Обозначение: m - грибы, M - горы, L - земля, W - вода
        */
        LOGGER.info(logBoardString.toString());
        return new Board(boardSizeX, boardSizeY, positionToCellMap);
    }

    /**
     * При взятии очередного типа клетки по индексу проверяем,
     * не нарушается ли принцип сбалансированности территории:
     * считаем количество клеток текущего выбранного типа
     * и сравниваем с максимально возможным количеством клеток данного типа
     *
     * @param cellAmount количество клеток на доске
     * @param cellTypes  лист с доступными типами клеток
     * @return допустимый индекс, по которому в листе можно взять тип клетки
     */
    private int getAllowedCellTypeIndex(final List<CellType> cellTypes, final int cellAmount) {
        final Random random = new Random();
        final int cellTypesAmount = cellTypes.size();
        /*Взятие остатка для случая нечетного количества клеток*/
        final int cellTypesAmountRange = cellAmount / cellTypesAmount + cellAmount % cellTypesAmount;
        int randomCellTypeIndex = -1;
        boolean isCellTypeAvailable = false;
        while (!isCellTypeAvailable) {
            randomCellTypeIndex = random.nextInt(cellTypesAmount);
            final CellType currentCellType = cellTypes.get(randomCellTypeIndex);
            isCellTypeAvailable = positionToCellMap
                    .values()
                    .stream()
                    .filter(type -> type.getType() == currentCellType)
                    .count() < cellTypesAmountRange;
        }
        return randomCellTypeIndex;
    }

    private List<CellType> loadCellTypePool() {
        final List<CellType> cellTypes = new ArrayList<>(CellType.values().length);
        cellTypes.add(CellType.LAND);
        cellTypes.add(CellType.MOUNTAIN);
        cellTypes.add(CellType.MUSHROOM);
        cellTypes.add(CellType.WATER);
        return cellTypes;
    }
}
