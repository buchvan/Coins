package io.neolab.internship.coins.server.game.player;

import io.neolab.internship.coins.Unit;
import io.neolab.internship.coins.server.game.board.Cell;

import java.util.*;

public class Player {
    private int id;
    private String nickname;
    private Race race;
    private List<Unit> units;
    private List<Unit> availableUnits;
    private int coins = 0;
    private List<Cell> transitCells;

    public Player() {
    }

    public Player(int id, String nickname, Race race, List<Unit> units, List<Unit> availableUnits, int coins, List<Cell> transitCells) {
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

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        Collections.copy(this.units, units);
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public List<Cell> getTransitCells() {
        return transitCells;
    }

    public void setTransitCells(List<Cell> transitCells) {
        Collections.copy(this.transitCells, transitCells);
    }

    public List<Unit> getAvailableUnits() {
        return availableUnits;
    }

    public void setAvailableUnits(List<Unit> availableUnits) {
        Collections.copy(this.availableUnits, this.availableUnits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return getId() == player.getId() &&
                getCoins() == player.getCoins() &&
                Objects.equals(getNickname(), player.getNickname()) &&
                getRace() == player.getRace() &&
                Objects.equals(getUnits(), player.getUnits());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getNickname(), getRace(), getUnits(), getCoins());
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", race=" + race +
                ", units=" + units +
                ", coins=" + coins +
                '}';
    }
}
