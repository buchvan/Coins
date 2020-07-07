package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.utils.Pair;

import java.util.*;

public class Game implements IGame {
    private Board board;
    private int currentRound;
    private Map<Player, List<Cell>> feudalToCells;
    private Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures;
    private final List<Race> racesPool;
    private final List<Player> players;
    private final Player neutralPlayer;

    public Game() {
        this(new Board(), 0, new HashMap<>(), new HashMap<>(), new LinkedList<>(), new LinkedList<>(),
                new Player(0, "neutral"));
    }

    public Game(final Board board, final int currentRound, final Map<Player, List<Cell>> feudalToCells,
                final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures, final List<Race> racesPool,
                final List<Player> players, final Player neutralPlayer) {
        this.board = board;
        this.currentRound = currentRound;
        this.feudalToCells = feudalToCells;
        this.raceCellTypeFeatures = raceCellTypeFeatures;
        this.racesPool = racesPool;
        this.players = players;
        this.neutralPlayer = neutralPlayer;
    }

    public Board getBoard() {
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

    public Map<Player, List<Cell>> getFeudalToCells() {
        return feudalToCells;
    }

    public void setFeudalToCells(final Map<Player, List<Cell>> feudalToCells) {
        this.feudalToCells = feudalToCells;
    }

    public Map<Pair<Race, CellType>, List<Feature>> getRaceCellTypeFeatures() {
        return raceCellTypeFeatures;
    }

    public void setRaceCellTypeFeatures(final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures) {
        this.raceCellTypeFeatures = raceCellTypeFeatures;
    }

    public List<Race> getRacesPool() {
        return racesPool;
    }

    public void setRacesPool(final List<Race> racesPool) {
        Collections.copy(this.racesPool, racesPool);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(final List<Player> players) {
        Collections.copy(this.players, players);
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
                Objects.equals(raceCellTypeFeatures, game.raceCellTypeFeatures) &&
                Objects.equals(racesPool, game.racesPool) &&
                Objects.equals(players, game.players) &&
                Objects.equals(neutralPlayer, game.neutralPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentRound, feudalToCells, raceCellTypeFeatures, racesPool, players, neutralPlayer);
    }

    @Override
    public String toString() {
        return "Game{" +
                "board=" + board +
                ", currentRound=" + currentRound +
                ", feudalToCells=" + feudalToCells +
                ", raceCellTypeFeatures=" + raceCellTypeFeatures +
                ", racesPool=" + racesPool +
                ", players=" + players +
                ", neutralPlayer=" + neutralPlayer +
                '}';
    }
}
