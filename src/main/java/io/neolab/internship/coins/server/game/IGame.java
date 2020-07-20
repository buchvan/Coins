package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;
import org.jetbrains.annotations.NotNull;

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
    @NotNull IBoard getBoard();

    /**
     * Сеттер борды
     *
     * @param board - новая борда
     */
    void setBoard(final @NotNull IBoard board);

    /**
     * @return номер текущего раунда
     */
    int getCurrentRound();

    /**
     * @return отображение игроков в множества клеток, приносящих им монеты
     */
    @NotNull Map<Player, Set<Cell>> getFeudalToCells();

    /**
     * @return отображение игроков в списки клеток, подконтрольных им
     */
    @NotNull Map<Player, List<Cell>> getOwnToCells();

    /**
     * @return отображение игроков в списки их транзитных клеток
     */
    @NotNull Map<Player, List<Cell>> getPlayerToTransitCells();

    /**
     * @return отображение игроков в множества клеток, достижимых ими за один ход
     */
    @NotNull Map<Player, Set<Cell>> getPlayerToAchievableCells();

    /**
     * @return игровые особенности
     */
    @NotNull GameFeatures getGameFeatures();

    /**
     * @return пул (список) рас
     */
    @NotNull List<Race> getRacesPool();

    /**
     * @return список игроков
     */
    @NotNull List<Player> getPlayers();
}
