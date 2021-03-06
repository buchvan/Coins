package io.neolab.internship.coins.server.game.player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.neolab.internship.coins.common.serialization.deserialize.AvailabilityTypeKeyDeserializer;
import io.neolab.internship.coins.common.serialization.serialize.AvailabilityTypeSerializer;
import io.neolab.internship.coins.utils.AvailabilityType;
import io.neolab.internship.coins.utils.IdGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class Player implements Serializable {
    @JsonProperty
    private final int id;

    @JsonProperty
    private final @NotNull String nickname;

    @JsonProperty
    private @Nullable Race race;

    @JsonProperty
    @JsonSerialize(keyUsing = AvailabilityTypeSerializer.class)
    @JsonDeserialize(keyUsing = AvailabilityTypeKeyDeserializer.class)
    private final @NotNull Map<AvailabilityType, List<Unit>> unitStateToUnits; // тип доступности -> список юнитов с этим типом

    @JsonProperty
    private int coins = 0;

    public Player(final @NotNull String nickname) {
        this.id = IdGenerator.getCurrentId();
        this.nickname = nickname;
        this.unitStateToUnits = new HashMap<>(AvailabilityType.values().length);
        for (final AvailabilityType availabilityType : AvailabilityType.values()) {
            this.unitStateToUnits.put(availabilityType, new LinkedList<>());
        }
    }

    @Contract(pure = true)
    @JsonCreator
    public Player(@JsonProperty("id") final int id,
                  @NotNull @JsonProperty("nickname") final String nickname,
                  @Nullable @JsonProperty("race") final Race race,
                  @NotNull @JsonProperty("unitStateToUnits") final Map<AvailabilityType, List<Unit>> unitStateToUnits,
                  @JsonProperty("coins") final int coins) {
        this.id = id;
        this.nickname = nickname;
        this.race = race;
        this.unitStateToUnits = new HashMap<>(AvailabilityType.values().length);
        for (final Map.Entry<AvailabilityType, List<Unit>> entry : unitStateToUnits.entrySet()) {
            final List<Unit> units = new LinkedList<>(entry.getValue());
            this.unitStateToUnits.put(entry.getKey(), units);
        }
        this.coins = coins;
    }

    @Contract(pure = true)
    @JsonIgnore
    public @NotNull Player getCopy() {
        final Map<AvailabilityType, List<Unit>> unitStateToUnits = new HashMap<>(this.unitStateToUnits.size());
        this.unitStateToUnits.forEach((availabilityType, units) ->
                unitStateToUnits.put(availabilityType, new LinkedList<>(units)));
        return new Player(this.id, this.nickname, this.race, unitStateToUnits, this.coins);
    }

    public int getId() {
        return id;
    }

    public @NotNull String getNickname() {
        return nickname;
    }

    public @Nullable Race getRace() {
        return race;
    }

    public void setRace(final @Nullable Race race) {
        this.race = race;
    }

    public @NotNull Map<AvailabilityType, List<Unit>> getUnitStateToUnits() {
        return unitStateToUnits;
    }

    public List<Unit> getUnitsByState(final @NotNull AvailabilityType availabilityType) {
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

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Player player = (Player) o;
        return id == player.id;
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
