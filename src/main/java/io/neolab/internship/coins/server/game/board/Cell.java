package io.neolab.internship.coins.server.game.board;

import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.server.game.Race;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Cell {
    private CellType type;
    private List<Unit> units;
    private Player own;
    private Race race = Race.NEUTRAL;

    public Cell() {
    }

    public Cell(final CellType type) {
        this(type, new LinkedList<>(type.getDefaultCatchUnit()), null, Race.NEUTRAL);
    }

    public Cell(final CellType type, final List<Unit> units, final Player own, final Race race) {
        this.type = type;
        this.units = units;
        this.own = own;
        this.race = race;
    }

    public CellType getType() {
        return type;
    }

    public void setType(final CellType type) {
        this.type = type;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(final List<Unit> units) {
        Collections.copy(this.units, units);
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
        return Objects.hash(getType(), getUnits(), getOwn(), getRace());
    }

    @Override
    public String toString() {
        return "Cell{" +
                "type=" + type +
                ", units=" + units +
                ", own=" + own +
                ", race=" + race +
                '}';
    }
}
