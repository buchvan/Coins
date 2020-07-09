package io.neolab.internship.coins.server.game.board;

import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.utils.IdGenerator;

import java.util.*;

public class Cell {
    private final int id = IdGenerator.getCurrentId();
    private CellType type;
    private final List<Unit> units = new LinkedList<>();
    private Player own = null;
    private Race race = Race.NEUTRAL;

    public Cell() {
    }

    public Cell(final CellType cellType) {
        this(cellType, new LinkedList<>(), null, Race.NEUTRAL);
    }

    public Cell(final CellType type, final List<Unit> units, Player own, final Race race) {
        this.type = type;
        Collections.copy(this.units, units);
        this.own = own;
        this.race = race;
    }

    public int getId() {
        return id;
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

    public void setOwn(Player own) {
        this.own = own;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(final Race race) {
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
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Cell{" +
                "id=" + id +
                ", type=" + type +
                ", units=" + units +
                ", own=" + own +
                ", race=" + race +
                '}';
    }
}
