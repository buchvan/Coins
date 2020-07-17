package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGame {
    void incrementCurrentRound();

    IBoard getBoard();

    void setBoard(final Board board);

    int getCurrentRound();

    Map<Player, Set<Cell>> getFeudalToCells();

    Map<Player, List<Cell>> getOwnToCells();

    Map<Player, List<Cell>> getPlayerToTransitCells();

    Map<Player, Set<Cell>> getPlayerToAchievableCells();

    GameFeatures getGameFeatures();

    List<Race> getRacesPool();

    List<Player> getPlayers();
}
