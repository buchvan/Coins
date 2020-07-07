package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Board;
import io.neolab.internship.coins.server.game.board.Cell;
import io.neolab.internship.coins.server.game.board.CellType;
import io.neolab.internship.coins.server.game.feature.Feature;
import io.neolab.internship.coins.utils.Pair;

import java.util.*;

public class Game implements IGame{
    private Board board;
    private int currentRound = 0;
    private Map<Player, List<Cell>> feudalToCells;
    private Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures;
    private List<Race> racesPool;
    private List<Player> players;

    public Game() {
    }

    public Game(Board board, int currentRound, Map<Player, List<Cell>> feudalToCells, Map<Pair<Race, CellType>,
            List<Feature>> raceCellTypeFeatures, List<Race> racesPool, List<Player> players) {
        this.board = board;
        this.currentRound = currentRound;
        this.feudalToCells = feudalToCells;
        this.raceCellTypeFeatures = raceCellTypeFeatures;
        this.racesPool = racesPool;
        this.players = players;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public Map<Player, List<Cell>> getFeudalToCells() {
        return feudalToCells;
    }

    public void setFeudalToCells(Map<Player, List<Cell>> feudalToCells) {
        this.feudalToCells = feudalToCells;
    }

    public Map<Pair<Race, CellType>, List<Feature>> getRaceCellTypeFeatures() {
        return raceCellTypeFeatures;
    }

    public void setRaceCellTypeFeatures(Map<Pair<Race, CellType>, List<Feature>> raceCellTypeFeatures) {
        this.raceCellTypeFeatures = raceCellTypeFeatures;
    }

    public List<Race> getRacesPool() {
        return racesPool;
    }

    public void setRacesPool(List<Race> racesPool) {
        Collections.copy(this.racesPool, racesPool);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        Collections.copy(this.players, players);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Game)) return false;
        Game game = (Game) o;
        return getCurrentRound() == game.getCurrentRound() &&
                Objects.equals(getBoard(), game.getBoard()) &&
                Objects.equals(getFeudalToCells(), game.getFeudalToCells()) &&
                Objects.equals(getRaceCellTypeFeatures(), game.getRaceCellTypeFeatures()) &&
                Objects.equals(getRacesPool(), game.getRacesPool());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBoard(), getCurrentRound(), getFeudalToCells(), getRaceCellTypeFeatures(), getRacesPool());
    }

    @Override
    public String toString() {
        return "Game{" +
                "board=" + board +
                ", currentRound=" + currentRound +
                ", feudalToCells=" + feudalToCells +
                ", raceCellTypeFeatures=" + raceCellTypeFeatures +
                ", racesPool=" + racesPool +
                '}';
    }
}
