package io.neolab.internship.coins.server.board;

import io.neolab.internship.coins.server.player.Player;
import io.neolab.internship.coins.Unit;
import io.neolab.internship.coins.server.player.Race;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cell {
    private CellType type;
    private List<Unit> units;
    private Player own;
    private Race race = Race.NEUTRAL;

    public Cell() {
    }

    public Cell(CellType type, List<Unit> units, Player own, Race race) {
        this.type = type;
        this.units = units;
        this.own = own;
        this.race = race;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        Collections.copy(this.units, units);
    }

    public Player getOwn() {
        return own;
    }

    public void setOwn(Player own) {
        this.own = own;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
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
