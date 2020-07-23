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
