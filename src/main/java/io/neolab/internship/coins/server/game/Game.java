package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;

import java.util.*;

public class Game implements IGame {
    private IBoard board;
    private int currentRound = 0;

    private final Map<Player, Set<Cell>> feudalToCells; // игрок > множество клеток, приносящих ему монет
    private final Map<Player, List<Cell>> ownToCells; // игрок -> список клеток, которые он контролирует
    private final Map<Player, List<Cell>> playerToTransitCells; // игрок -> список клеток, которые он контролирует,
    // но которые не приносят ему монет

    /* Так можно найти список транзитных клетки одного игрока: */
//        final List<Cell> transitCells = new LinkedList<>(ownToCells.get(player));
//        transitCells.removeIf(feudalToCells.get(player)::contains);

    private final GameFeatures gameFeatures;
    private final List<Race> racesPool;

    private final List<Player> players;
    private final Player neutralPlayer;

    public Game() {
        this(new Board(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new GameFeatures(),
                new LinkedList<>(), new LinkedList<>(), new Player("neutral"));
    }

    public Game(final Board board, final Map<Player, Set<Cell>> feudalToCells,
                final Map<Player, List<Cell>> ownToCells, final Map<Player, List<Cell>> playerToTransitCells,
                final GameFeatures gameFeatures, final List<Race> racesPool,
                final List<Player> players, final Player neutralPlayer) {
        this.board = board;
        this.feudalToCells = feudalToCells;
        this.ownToCells = ownToCells;
        this.playerToTransitCells = playerToTransitCells;
        this.gameFeatures = gameFeatures;
        this.racesPool = racesPool;
        this.players = players;
        this.neutralPlayer = neutralPlayer;
    }

    public void incrementCurrentRound() {
        currentRound++;
    }

    public IBoard getBoard() {
        return board;
    }

    public void setBoard(final Board board) {
        this.board = board;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(final int currentRound) {
        this.currentRound = currentRound;
    }

    public Map<Player, Set<Cell>> getFeudalToCells() {
        return feudalToCells;
    }

    public Map<Player, List<Cell>> getOwnToCells() {
        return ownToCells;
    }

    public Map<Player, List<Cell>> getPlayerToTransitCells() {
        return playerToTransitCells;
    }

    public GameFeatures getGameFeatures() {
        return gameFeatures;
    }

    @Override
    public List<Race> getRacesPool() {
        return racesPool;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getNeutralPlayer() {
        return neutralPlayer;
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
                Objects.equals(gameFeatures, game.gameFeatures) &&
                Objects.equals(racesPool, game.racesPool) &&
                Objects.equals(players, game.players) &&
                Objects.equals(neutralPlayer, game.neutralPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentRound, feudalToCells, ownToCells, playerToTransitCells,
                gameFeatures, racesPool, players, neutralPlayer);
    }

    @Override
    public String toString() {
        return "Game{" +
                "board=" + board +
                ", currentRound=" + currentRound +
                ", feudalToCells=" + feudalToCells +
                ", ownToCells=" + ownToCells +
                ", playerToTransitCells=" + playerToTransitCells +
                ", raceCellTypeFeatures=" + gameFeatures +
                ", racesPool=" + racesPool +
                ", players=" + players +
                ", neutralPlayer=" + neutralPlayer +
                '}';
    }
}
