package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.board.IBoard;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.utils.Pair;

import java.util.*;

public class Game implements IGame {
    private IBoard board;
    private int currentRound;

    private final Map<Player, Set<Cell>> feudalToCells;
    private final Map<Player, List<Cell>> ownToCells;
    private final Map<Player, List<Cell>> playerToTransitCells;

    private final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures;
    private final List<Race> racesPool;

    private final List<Player> players;
    private final Player neutralPlayer;

    public Game() {
        this(new Board(), 0, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
                new LinkedList<>(), new LinkedList<>(), new Player("neutral"));
    }

    public Game(final Board board, final int currentRound, final Map<Player, Set<Cell>> feudalToCells,
                final Map<Player, List<Cell>> ownToCells, final Map<Player, List<Cell>> playerToTransitCells,
                final Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures, final List<Race> racesPool,
                final List<Player> players, final Player neutralPlayer) {
        this.board = board;
        this.currentRound = currentRound;
        this.feudalToCells = feudalToCells;
        this.ownToCells = ownToCells;
        this.playerToTransitCells = playerToTransitCells;
        this.raceCellTypeFeatures = raceCellTypeFeatures;
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

    public Map<Pair<Race, CellType>, List<Feature>> getRaceCellTypeFeatures() {
        return raceCellTypeFeatures;
    }

    public List<Feature> getFeaturesByRaceAndCellType(final Race race, final CellType cellType) {
        final List<Feature> features = getRaceCellTypeFeatures().get(new Pair<>(race, cellType));
        if (features == null) {
            return new LinkedList<>();
        }
        return features;
    }

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
                Objects.equals(raceCellTypeFeatures, game.raceCellTypeFeatures) &&
                Objects.equals(racesPool, game.racesPool) &&
                Objects.equals(players, game.players) &&
                Objects.equals(neutralPlayer, game.neutralPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentRound, feudalToCells, ownToCells, playerToTransitCells, raceCellTypeFeatures, racesPool, players, neutralPlayer);
    }

    @Override
    public String toString() {
        return "Game{" +
                "board=" + board +
                ", currentRound=" + currentRound +
                ", feudalToCells=" + feudalToCells +
                ", ownToCells=" + ownToCells +
                ", playerToTransitCells=" + playerToTransitCells +
                ", raceCellTypeFeatures=" + raceCellTypeFeatures +
                ", racesPool=" + racesPool +
                ", players=" + players +
                ", neutralPlayer=" + neutralPlayer +
                '}';
    }
}
