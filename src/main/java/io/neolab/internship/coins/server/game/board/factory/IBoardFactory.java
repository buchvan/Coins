package io.neolab.internship.coins.server.game.board.factory;

import io.neolab.internship.coins.exceptions.CoinsException;
import io.neolab.internship.coins.server.game.board.IBoard;
import org.jetbrains.annotations.NotNull;

public interface IBoardFactory {
    @NotNull IBoard generateBoard(int width, int height) throws CoinsException;
}
