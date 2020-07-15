package io.neolab.internship.coins.server.game.board;

import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.utils.IdGenerator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Cell {
    private final int id = IdGenerator.getCurrentId();
    private final CellType type;
    private final List<Unit> units = new LinkedList<>();
    private Player feudal = null;
    private Player own = null;
    private Race race;

    public Cell(final CellType cellType) {
        this(cellType, null);
    }

    public Cell(final CellType type, final Race race) {
        this.type = type;
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
        Collections.copy(this.units, units);
    }

    public Player getFeudal() {
        return feudal;
    }

    public void setFeudal(final Player feudal) {
        this.feudal = feudal;
    }

    public Player getOwn() {
        return own;
    }

    public void setOwn(final Player own) {
        this.own = own;
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
                Objects.equals(getOwn(), cell.getOwn()) &&
                getRace() == cell.getRace();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "type=" + type +
                ", units=" + units +
                ", feudal=" + feudal +
                ", own=" + own +
                ", race=" + race +
                '}';
    }
}
