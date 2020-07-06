package io.neolab.internship.coins.server.player;

import io.neolab.internship.coins.Unit;

import java.util.List;
import java.util.Objects;

public class Player {
    private int id;
    private String nickname;
    private Race race;
    private List<Unit> units;
    private int coins = 0;

    public Player() {
    }

    public Player(int id, String nickname, Race race, List<Unit> units, int coins) {
        this.id = id;
        this.nickname = nickname;
        this.race = race;
        this.units = units;
        this.coins = coins;
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
        this.units = units;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
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
