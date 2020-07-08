package io.neolab.internship.coins.server.game.board;

import io.neolab.internship.coins.server.game.Player;
import io.neolab.internship.coins.server.game.Race;
import io.neolab.internship.coins.server.game.Unit;
import io.neolab.internship.coins.utils.IdGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cell {
    private int id;
    private CellType type;
    private List<Unit> units = new ArrayList<>();
    private Player own = null;
    private Race race = Race.NEUTRAL;

    public Cell() {
    }

    public Cell(CellType cellType) {
        this(cellType, new ArrayList<>(), null, Race.NEUTRAL);
    }

    public Cell(CellType type, List<Unit> units, Player own, Race race) {
        this.id = IdGenerator.getCurrentId();
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
        return Objects.hash(getId());
    }
}
