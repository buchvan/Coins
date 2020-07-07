package io.neolab.internship.coins.server.game.factory;

import io.neolab.internship.coins.server.game.board.Board;

public interface IBoardFactory {
    Board getBoard(int width, int height);
}
