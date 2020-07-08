package io.neolab.internship.coins.server.game.factory;


import io.neolab.internship.coins.server.game.board.Board;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BoardFactoryTest {

    @Test
    public void generateBoardTest() {
        BoardFactory boardFactory = new BoardFactory();
        Board board = boardFactory.generateBoard(3,4);
        //assertEquals(12, board.getPositionToCellMap().size());
    }

}
