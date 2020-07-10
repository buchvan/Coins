package io.neolab.internship.coins.server.game;

import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.IdGenerator;

import java.util.*;

public class Player {
    private int id;
    private String nickname;
    private Race race;
    private final Map<AvailabilityType, List<Unit>> unitStateToUnits; // тип доступности -> список юнитов с этим типом
    private int coins = 0;

    public Player(final String nickname) {
        this.id = IdGenerator.getCurrentId();
        this.nickname = nickname;
        this.unitStateToUnits = new HashMap<>(AvailabilityType.values().length);
        for (final AvailabilityType availabilityType : AvailabilityType.values()) {
            this.unitStateToUnits.put(availabilityType, new LinkedList<>());
        }
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

    public Map<AvailabilityType, List<Unit>> getUnitStateToUnits() {
        return unitStateToUnits;
    }

    public List<Unit> getUnitsByState(final AvailabilityType availabilityType) {
        return unitStateToUnits.get(availabilityType);
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(final int coins) {
        this.coins = coins;
    }

    public void increaseCoins(final int number) {
        this.coins += number;
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
                Objects.equals(unitStateToUnits, player.unitStateToUnits);
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
                ", unitStateToUnits=" + unitStateToUnits +
                ", coins=" + coins +
                '}';
    }
}
