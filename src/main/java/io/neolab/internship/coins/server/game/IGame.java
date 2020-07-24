package io.neolab.internship.coins.server.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.feature.GameFeatures;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Race;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Game.class, name = "Game"),
})
public interface IGame {
    IGame getCopy();

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
     * @return отображение игроков в списки их транзитных клеток (клеток, которые контролирует игрок,
     * но которые не приносят ему монет
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
