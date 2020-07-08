package io.neolab.internship.coins.server.game.board;

import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Cell {
    private final int id;
    private final CellType type;
    private final List<Unit> units;
    private Player own;
    private Race race;

    public Cell() {
        this(0, CellType.LAND);
    }

    public Cell(final int id, final CellType type) {
        this(id, type, new LinkedList<>(type.getDefaultCatchUnit()), null, Race.NEUTRAL);
    }

    public Cell(final int id, final CellType type, final List<Unit> units, final Player own, final Race race) {
        this.id = id;
        this.type = type;
        this.units = units;
        this.own = own;
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
        if (o == null || getClass() != o.getClass()) return false;
        final Cell cell = (Cell) o;
        return id == cell.id &&
                type == cell.type &&
                Objects.equals(units, cell.units) &&
                Objects.equals(own, cell.own) &&
                race == cell.race;
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
                ", own=" + own +
                ", race=" + race +
                '}';
    }
}
