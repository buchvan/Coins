package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.IBoard;

import java.util.*;

public class Game implements IGame {
    private IBoard board;
    private int currentRound = 0;
    public static final int ROUNDS_COUNT = 10;

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

    public Game() {
        this(new Board(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new GameFeatures(),
                new LinkedList<>(), new LinkedList<>());
    }

    public Game(final IBoard board, final Map<Player, Set<Cell>> feudalToCells,
                final Map<Player, List<Cell>> ownToCells, final Map<Player, List<Cell>> playerToTransitCells,
                final GameFeatures gameFeatures, final List<Race> racesPool,
                final List<Player> players) {
        this.board = board;
        this.feudalToCells = feudalToCells;
        this.ownToCells = ownToCells;
        this.playerToTransitCells = playerToTransitCells;
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
    public void setCurrentRound(final int currentRound) {
        this.currentRound = currentRound;
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
                Objects.equals(gameFeatures, game.gameFeatures) &&
                Objects.equals(racesPool, game.racesPool) &&
                Objects.equals(players, game.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentRound, feudalToCells, ownToCells, playerToTransitCells,
                gameFeatures, racesPool, players);
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
                '}';
    }
}
