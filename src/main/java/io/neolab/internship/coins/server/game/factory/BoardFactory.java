package io.neolab.internship.coins.server.game.factory;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.Position;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoardFactory.class);
    /**
     * При создании доски соблюдается принцип сбалансированности территории:
     * 1 версия: Каждый тип клетки встречается по три раза
     * Координаты начинаются в левом верхнем углу
     * @param width ширина
     * @param height высота
     * @return new Board
     */
    public Board getBoard(final int width, final int height) {
        Board board = new Board();
        BidiMap<Position, Cell> positionToCellMap = new DualHashBidiMap<>();
        Position currentPosition;
        for(int i =0; i < width; i ++) {
            for(int j = 0; j < height; j++) {
                currentPosition = new Position(i,j);
                positionToCellMap.put(currentPosition, new Cell());
            }
        }
        return board;
    }
}
