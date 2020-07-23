package io.neolab.internship.coins.server.game.board;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neolab.internship.coins.server.game.player.Player;
import io.neolab.internship.coins.server.game.player.Unit;
import io.neolab.internship.coins.server.game.player.Race;
import io.neolab.internship.coins.utils.IdGenerator;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Cell implements Serializable {
    @JsonProperty
    private final int id;

    @JsonProperty
    private final CellType type;

    @JsonProperty
    private final List<Unit> units = new LinkedList<>();

    @JsonProperty
    private Player feudal = null;

    @JsonProperty
    private Race race;

    public Cell(final CellType cellType) {
        this(cellType, null);
    }

    public Cell(final CellType type, final Race race) {
        this.id = IdGenerator.getCurrentId();
        this.type = type;
        this.race = race;
    }

    @JsonCreator
    public Cell(@JsonProperty("id") final int id,
                @JsonProperty("type") final CellType type,
                @JsonProperty("units") final List<Unit> units,
                @JsonProperty("feudal") final Player feudal,
                @JsonProperty("race") final Race race) {
        this.id = id;
        this.type = type;
        this.units.addAll(units);
        this.feudal = feudal;
        this.race = race;
    }

    public int getId() {
        return id;
    }

    public CellType getType() {
        return type;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(final List<Unit> units) {
        this.units.clear();
        this.units.addAll(units);
    }

    public Player getFeudal() {
        return feudal;
    }

    public void setFeudal(final Player feudal) {
        this.feudal = feudal;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(final Race race) {
        this.race = race;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        final Cell cell = (Cell) o;
        return getType() == cell.getType() &&
                Objects.equals(getUnits(), cell.getUnits()) &&
                getRace() == cell.getRace();
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
