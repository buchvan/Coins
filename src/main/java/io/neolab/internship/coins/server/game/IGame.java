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
import org.jetbrains.annotations.NotNull;

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
     * @return отображение игроков в списки их транзитных клеток (клеток, которые контролирует игрок,
     * но которые не приносят ему монет
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
