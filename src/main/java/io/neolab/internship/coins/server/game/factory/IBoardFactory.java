package io.neolab.internship.coins.server.game.factory;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.board.IBoard;

public interface IBoardFactory {
    IBoard generateBoard(int width, int height) throws CoinsException;
}
