package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGame {
    /**
     * Увеличить номер текущего раунда
     */
    void incrementCurrentRound();

    /**
     * @return борду
     */
    IBoard getBoard();

    /**
     * Сеттер борды
     *
     * @param board - новая борда
     */
    void setBoard(final Board board);

    /**
     * @return номер текущего раунда
     */
    int getCurrentRound();

    /**
     * @return отображение игроков в множества клеток, приносящих им монеты
     */
    Map<Player, Set<Cell>> getFeudalToCells();

    /**
     * @return отображение игроков в списки клеток, подконтрольных им
     */
    Map<Player, List<Cell>> getOwnToCells();

    /**
     * @return отображение игроков в списки их транзитных клеток
     */
    Map<Player, List<Cell>> getPlayerToTransitCells();

    /**
     * @return отображение игроков в множества клеток, достижимых ими за один ход
     */
    Map<Player, Set<Cell>> getPlayerToAchievableCells();

    /**
     * @return игровые особенности
     */
    GameFeatures getGameFeatures();

    /**
     * @return пул (список) рас
     */
    List<Race> getRacesPool();

    /**
     * @return список игроков
     */
    List<Player> getPlayers();
}
