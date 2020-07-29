package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.utils.IdGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Cell implements Serializable {
    @JsonProperty
    private final int id;

    @JsonProperty
    private final @NotNull CellType type;

    @JsonProperty
    private final @NotNull List<Unit> units = new LinkedList<>();

    @JsonProperty
    private @Nullable Player feudal = null; // игрок, который получает монеты с данной клетки

    @JsonProperty
    private @Nullable Race race;

    public Cell(final @NotNull CellType cellType) {
        this(cellType, null);
    }

    public Cell(final @NotNull CellType type, final @Nullable Race race) {
        this.id = IdGenerator.getCurrentId();
        this.type = type;
        this.race = race;
    }

    @JsonCreator
    public Cell(@JsonProperty("id") final int id,
                @JsonProperty("type") final @NotNull CellType type,
                @JsonProperty("units") final @NotNull List<Unit> units,
                @JsonProperty("feudal") final @Nullable Player feudal,
                @JsonProperty("race") final @Nullable Race race) {
        this.id = id;
        this.type = type;
        this.units.addAll(units);
        this.feudal = feudal;
        this.race = race;
    }

    @Contract(pure = true)
    @JsonIgnore
    public @NotNull  Cell getCopy() {
        final List<Unit> units = new LinkedList<>();
        this.units.forEach(unit -> units.add(unit.getCopy()));
        return new Cell(this.id, this.type, units, null, this.race);
    }

    public int getId() {
        return id;
    }

    public @NotNull CellType getType() {
        return type;
    }

    public @NotNull List<Unit> getUnits() {
        return units;
    }

    public @Nullable Player getFeudal() {
        return feudal;
    }

    public void setFeudal(final @Nullable Player feudal) {
        this.feudal = feudal;
    }

    public @Nullable Race getRace() {
        return race;
    }

    public void setRace(final @Nullable Race race) {
        this.race = race;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Cell cell = (Cell) o;
        return id == cell.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "id=" + id +
                ", type=" + type +
                ", units=" + units +
                ", feudal=" + feudal +
                ", race=" + race +
                '}';
    }
}
