package io.neolab.internship.coins.server.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.deserialize.BoardDeserializer;
import io.neolab.internship.coins.common.deserialize.PlayerKeyDeserializer;
import io.neolab.internship.coins.common.serialize.PlayerSerializer;
import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.utils.Pair;

import java.io.Serializable;
import java.util.*;

public class Game implements IGame, Serializable {

    @JsonDeserialize(using = BoardDeserializer.class)
    private IBoard board;

    private int currentRound;

    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final Map<Player, Set<Cell>> feudalToCells; // игрок > множество клеток, приносящих ему монет

    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final Map<Player, List<Cell>> ownToCells; // игрок -> список клеток, которые он контролирует

    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final Map<Player, List<Cell>> playerToTransitCells; // игрок -> список клеток, которые он контролирует,
    // но которые не приносят ему монет

    /* Так можно найти список транзитных клетки одного игрока: */
//        final List<Cell> transitCells = new LinkedList<>(ownToCells.get(player));
//        transitCells.removeIf(feudalToCells.get(player)::contains);


    @JsonSerialize(keyUsing = PlayerSerializer.class)
    @JsonDeserialize(keyUsing = PlayerKeyDeserializer.class)
    private final Map<Player, Pair<Boolean, List<Cell>>> playerAchievableCells; // игрок ->
    // (актуальность списка достижимых клеток, список достижимых клеток)

    private final GameFeatures gameFeatures;
    private final List<Race> racesPool;

    private final List<Player> players;

    public Game() {
        this(new Board(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new GameFeatures(),
                new LinkedList<>(), new LinkedList<>());
    }

    public Game(final IBoard board, final Map<Player, Set<Cell>> feudalToCells,
                final Map<Player, List<Cell>> ownToCells, final Map<Player, List<Cell>> playerToTransitCells,
                final Map<Player, Pair<Boolean, List<Cell>>> playerAchievableCells,
                final GameFeatures gameFeatures, final List<Race> racesPool, final List<Player> players) {

        this(board, 0, feudalToCells, ownToCells, playerToTransitCells, playerAchievableCells,
                gameFeatures, racesPool, players);
    }

    @JsonCreator
    public Game(@JsonProperty("board") final IBoard board,
                @JsonProperty("currentRound") final int currentRound,
                @JsonProperty("feudalToCells") final Map<Player, Set<Cell>> feudalToCells,
                @JsonProperty("ownToCells") final Map<Player, List<Cell>> ownToCells,
                @JsonProperty("playerToTransitCells") final Map<Player, List<Cell>> playerToTransitCells,
                @JsonProperty("playerAchievableCells") final Map<Player, Pair<Boolean, List<Cell>>>
                        playerAchievableCells,
                @JsonProperty("gameFeatures") final GameFeatures gameFeatures,
                @JsonProperty("racesPool") final List<Race> racesPool,
                @JsonProperty("players") final List<Player> players) {
        this.board = board;
        this.currentRound = currentRound;
        this.feudalToCells = feudalToCells;
        this.ownToCells = ownToCells;
        this.playerToTransitCells = playerToTransitCells;
        this.playerAchievableCells = playerAchievableCells;
        this.gameFeatures = gameFeatures;
        this.racesPool = racesPool;
        this.players = players;
    }

    @Override
    public void incrementCurrentRound() {
        currentRound++;
    }

    @Override
    public IBoard getBoard() {
        return board;
    }

    @Override
    public void setBoard(final Board board) {
        this.board = board;
    }

    @Override
    public int getCurrentRound() {
        return currentRound;
    }

    @Override
    public Map<Player, Set<Cell>> getFeudalToCells() {
        return feudalToCells;
    }

    @Override
    public Map<Player, List<Cell>> getOwnToCells() {
        return ownToCells;
    }

    @Override
    public Map<Player, List<Cell>> getPlayerToTransitCells() {
        return playerToTransitCells;
    }

    @Override
    public Map<Player, Pair<Boolean, List<Cell>>> getPlayerAchievableCells() {
        return playerAchievableCells;
    }

    @Override
    public GameFeatures getGameFeatures() {
        return gameFeatures;
    }

    @Override
    public List<Race> getRacesPool() {
        return racesPool;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Game game = (Game) o;
        return currentRound == game.currentRound &&
                Objects.equals(board, game.board) &&
                Objects.equals(feudalToCells, game.feudalToCells) &&
                Objects.equals(ownToCells, game.ownToCells) &&
                Objects.equals(playerToTransitCells, game.playerToTransitCells) &&
                Objects.equals(playerAchievableCells, game.playerAchievableCells) &&
                Objects.equals(gameFeatures, game.gameFeatures) &&
                Objects.equals(racesPool, game.racesPool) &&
                Objects.equals(players, game.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentRound, feudalToCells, ownToCells, playerToTransitCells,
                playerAchievableCells, gameFeatures, racesPool, players);
    }

    @Override
    public String toString() {
        return "Game{" +
                "board=" + board +
                ", currentRound=" + currentRound +
                ", feudalToCells=" + feudalToCells +
                ", ownToCells=" + ownToCells +
                ", playerToTransitCells=" + playerToTransitCells +
                ", playerAchievableCells=" + playerAchievableCells +
                ", gameFeatures=" + gameFeatures +
                ", racesPool=" + racesPool +
                ", players=" + players +
                '}';
    }
}
