package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.server.game.board.Cell;

import java.util.*;

public class Player {
    private int id;
    private String nickname;
    private Race race;
    private final List<Unit> units;
    private List<Unit> availableUnits;
    private int coins;
    private final List<Cell> transitCells;

    public Player() {
        this(0, "Test");
    }

    public Player(final int id, final String nickname) {
        this(id, nickname, null, new LinkedList<>(), new LinkedList<>(), 0, new ArrayList<>());
    }

    public Player(final int id, final String nickname, final Race race, final List<Unit> units,
                  final List<Unit> availableUnits, final int coins, final List<Cell> transitCells) {
        this.id = id;
        this.nickname = nickname;
        this.race = race;
        this.units = units;
        this.availableUnits = availableUnits;
        this.coins = coins;
        this.transitCells = transitCells;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(final Race race) {
        this.race = race;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(final List<Unit> units) {
        Collections.copy(this.units, units);
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(final int coins) {
        this.coins = coins;
    }

    public List<Cell> getTransitCells() {
        return transitCells;
    }

    public void setTransitCells(final List<Cell> transitCells) {
        Collections.copy(this.transitCells, transitCells);
    }

    public List<Unit> getAvailableUnits() {
        return availableUnits;
    }

    public void setAvailableUnits(final List<Unit> availableUnits) {
        this.availableUnits = new LinkedList<>(availableUnits);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Player player = (Player) o;
        return id == player.id &&
                coins == player.coins &&
                Objects.equals(nickname, player.nickname) &&
                race == player.race &&
                Objects.equals(units, player.units) &&
                Objects.equals(availableUnits, player.availableUnits) &&
                Objects.equals(transitCells, player.transitCells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", race=" + race +
                ", units=" + units +
                ", availableUnits=" + availableUnits +
                ", coins=" + coins +
                ", transitCells=" + transitCells +
                '}';
    }
}
